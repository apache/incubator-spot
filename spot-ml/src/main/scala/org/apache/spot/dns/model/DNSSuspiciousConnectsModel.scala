package org.apache.spot.dns.model

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSchema._
import org.apache.spot.dns.DNSWordCreation
import org.apache.spot.lda.SpotLDAWrapper
import org.apache.spot.lda.SpotLDAWrapper.{SpotLDAInput, SpotLDAOutput}
import org.apache.spot.utilities.DomainProcessor.DomainInfo
import org.apache.spot.utilities.data.validation.InvalidDataHandler
import org.apache.spot.utilities.{CountryCodes, DomainProcessor, Quantiles, TopDomains}

import scala.util.{Failure, Success, Try}


/**
  * A probabilistic model of the DNS queries issued by each client IP.
  *
  * The model uses a topic-modelling approach that:
  * 1. Simplifies DNS log entries into words.
  * 2. Treats the DNS queries of each client into a collection of words.
  * 3. Decomposes common query behaviors using a collection of "topics" that represent common profiles
  * of query behavior. These "topics" are probability distributions on words.
  * 4. Each client IP has a mix of topics corresponding to its behavior.
  * 5. Query probability at IP is estimated by simplifying query into word, and then
  * combining the word probabilities per topic using the topic mix of the particular IP.
  *
  * Create these models using the  factory in the companion object.
  *
  * @param inTopicCount          Number of topics to use in the topic model.
  * @param inIpToTopicMix        Per-IP topic mix.
  * @param inWordToPerTopicProb  Per-word,  an array of probability of word given topic per topic.
  * @param inTimeCuts            Quantile cut-offs for discretizing the time of day in word construction.
  * @param inFrameLengthCuts     Quantile cut-offs for discretizing the frame length in word construction.
  * @param inSubdomainLengthCuts Quantile cut-offs for discretizing subdomain length in word construction.
  * @param inNumberPeriodsCuts   Quantile cut-offs for discretizing domain number-of-periods count in word construction.
  * @param inEntropyCuts         Quantile cut-offs for discretizing the subdomain entropy in word construction.
  */
class DNSSuspiciousConnectsModel(inTopicCount: Int,
                                 inIpToTopicMix: Map[String, Array[Double]],
                                 inWordToPerTopicProb: Map[String, Array[Double]],
                                 inTimeCuts: Array[Double],
                                 inFrameLengthCuts: Array[Double],
                                 inSubdomainLengthCuts: Array[Double],
                                 inNumberPeriodsCuts: Array[Double],
                                 inEntropyCuts: Array[Double]) {

  val topicCount = inTopicCount
  val ipToTopicMix = inIpToTopicMix
  val wordToPerTopicProb = inWordToPerTopicProb
  val timeCuts = inTimeCuts
  val frameLengthCuts = inFrameLengthCuts
  val subdomainLengthCuts = inSubdomainLengthCuts
  val numberPeriodsCuts = inNumberPeriodsCuts
  val entropyCuts = inEntropyCuts

  /**
    * Use a suspicious connects model to assign estimated probabilities to a dataframe of
    * DNS log events.
    *
    * @param sc         Spark Context
    * @param sqlContext Spark SQL context
    * @param inDF       Dataframe of DNS log events, containing at least the columns of [[DNSSuspiciousConnectsModel.ModelSchema]]
    * @param userDomain Domain associated to network data (ex: 'intel')
    * @return Dataframe with a column named [[org.apache.spot.dns.DNSSchema.Score]] that contains the
    *         probability estimated for the network event at that row
    */
  def score(sc: SparkContext, sqlContext: SQLContext, inDF: DataFrame, userDomain: String): DataFrame = {

    val countryCodesBC = sc.broadcast(CountryCodes.CountryCodes)
    val topDomainsBC = sc.broadcast(TopDomains.TopDomains)
    val ipToTopicMixBC = sc.broadcast(ipToTopicMix)
    val wordToPerTopicProbBC = sc.broadcast(wordToPerTopicProb)


    val scoreFunction =
      new DNSScoreFunction(frameLengthCuts,
        timeCuts,
        subdomainLengthCuts,
        entropyCuts,
        numberPeriodsCuts,
        topicCount,
        ipToTopicMixBC,
        wordToPerTopicProbBC,
        topDomainsBC,
        userDomain)


    val scoringUDF = udf((timeStamp: String,
                          unixTimeStamp: Long,
                          frameLength: Int,
                          clientIP: String,
                          queryName: String,
                          queryClass: String,
                          queryType: Int,
                          queryResponseCode: Int) =>
      scoreFunction.score(timeStamp,
        unixTimeStamp,
        frameLength,
        clientIP,
        queryName,
        queryClass,
        queryType,
        queryResponseCode))

    inDF.withColumn(Score, scoringUDF(DNSSuspiciousConnectsModel.modelColumns: _*))
  }
}

/**
  * Contains dataframe schema information as well as the train-from-dataframe routine
  * (which is a kind of factory routine) for [[DNSSuspiciousConnectsModel]] instances.
  *
  */
object DNSSuspiciousConnectsModel {

  val ModelSchema = StructType(List(TimestampField,
    UnixTimestampField,
    FrameLengthField,
    ClientIPField,
    QueryNameField,
    QueryClassField,
    QueryTypeField,
    QueryResponseCodeField))

  val modelColumns = ModelSchema.fieldNames.toList.map(col)

  val DomainStatsSchema = StructType(List(TopDomainField, SubdomainLengthField, SubdomainEntropyField, NumPeriodsField))

  /**
    * Create a new DNS Suspicious Connects model by training it on a data frame and a feedback file.
    *
    * @param sparkContext
    * @param sqlContext
    * @param logger
    * @param config     Analysis configuration object containing CLI parameters.
    *                   Contains the path to the feedback file in config.scoresFile
    * @param inputRecords       Data used to train the model.
    * @param topicCount Number of topics (traffic profiles) used to build the model.
    * @return A new [[DNSSuspiciousConnectsModel]] instance trained on the dataframe and feedback file.
    */
  def trainNewModel(sparkContext: SparkContext,
                    sqlContext: SQLContext,
                    logger: Logger,
                    config: SuspiciousConnectsConfig,
                    inputRecords: DataFrame,
                    topicCount: Int): DNSSuspiciousConnectsModel = {

    logger.info("Training DNS suspicious connects model from " + config.inputPath)

    val selectedRecords = inputRecords.select(modelColumns: _*)

    val totalRecords = selectedRecords.unionAll(DNSFeedback.loadFeedbackDF(sparkContext,
      sqlContext,
      config.feedbackFile,
      config.duplicationFactor))

    val countryCodesBC = sparkContext.broadcast(CountryCodes.CountryCodes)
    val topDomainsBC = sparkContext.broadcast(TopDomains.TopDomains)
    val userDomain = config.userDomain

    // create quantile cut-offs

    val timeCuts =
      Quantiles.computeDeciles(totalRecords
        .select(UnixTimestamp)
        .rdd
        .flatMap({ case Row(unixTimeStamp: Long) => {
          Try {unixTimeStamp.toDouble} match {
              case Failure(_) => Seq()
              case Success(timestamp) => Seq(timestamp)
            }
          }
        }))

    val frameLengthCuts =
      Quantiles.computeDeciles(totalRecords
        .select(FrameLength)
        .rdd
        .flatMap({case Row(frameLen: Int) => {
            Try{frameLen.toDouble} match{
              case Failure(_) => Seq()
              case Success(frameLen) => Seq(frameLen)
            }
          }
        }))

    val domainStatsRecords = createDomainStatsDF(sparkContext, sqlContext, countryCodesBC, topDomainsBC, userDomain, totalRecords)

    val subdomainLengthCuts =
      Quantiles.computeQuintiles(domainStatsRecords
        .filter(domainStatsRecords(SubdomainLength).gt(0))
        .select(SubdomainLength)
        .rdd
        .flatMap({ case Row(subdomainLength: Int) => {
            Try{subdomainLength.toDouble} match {
              case Failure(_) => Seq()
              case Success(subdomainLength) => Seq(subdomainLength)
            }
          }
        }))

    val entropyCuts =
      Quantiles.computeQuintiles(domainStatsRecords
        .filter(domainStatsRecords(SubdomainEntropy).gt(0))
        .select(SubdomainEntropy)
        .rdd
        .flatMap({ case Row(subdomainEntropy: Double) => {
          Try{subdomainEntropy.toDouble} match {
            case Failure(_) => Seq()
            case Success(subdomainEntropy) => Seq(subdomainEntropy)
            }
          }
        }))

    val numberPeriodsCuts =
      Quantiles.computeQuintiles(domainStatsRecords
        .filter(domainStatsRecords(NumPeriods).gt(0))
        .select(NumPeriods)
        .rdd
        .flatMap({ case Row(numberPeriods: Int) => {
          Try {numberPeriods.toDouble} match {
            case Failure(_) => Seq()
            case Success(numberPeriods) => Seq(numberPeriods)
            }
          }
        }))

    // simplify DNS log entries into "words"

    val dnsWordCreator = new DNSWordCreation(frameLengthCuts,
                                             timeCuts,
                                             subdomainLengthCuts,
                                             entropyCuts,
                                             numberPeriodsCuts,
                                             topDomainsBC,
                                             userDomain)

    val dataWithWord = totalRecords.withColumn(Word, dnsWordCreator.wordCreationUDF(modelColumns: _*))

    // aggregate per-word counts at each IP
    val ipDstWordCounts =
      dataWithWord
        .select(ClientIP, Word)
        .filter(dataWithWord(Word).notEqual(InvalidDataHandler.WordError))
        .map({ case Row(destIP: String, word: String) => (destIP, word) -> 1 })
        .reduceByKey(_ + _)
        .map({ case ((ipDst, word), count) => SpotLDAInput(ipDst, word, count) })


    val SpotLDAOutput(ipToTopicMixDF, wordToPerTopicProb) = SpotLDAWrapper.runLDA(sparkContext,
      sqlContext,
      ipDstWordCounts,
      config.topicCount,
      logger,
      config.ldaPRGSeed,
      config.ldaAlpha,
      config.ldaBeta,
      config.ldaMaxiterations)

    // Since DNS is still broadcasting ip to topic mix, we need to convert data frame to Map[String, Array[Double]]
    val ipToTopicMix = ipToTopicMixDF
      .rdd
      .map({ case (ipToTopicMixRow: Row) => (ipToTopicMixRow.toSeq.toArray) })
      .map({
        case (ipToTopicMixSeq) => (ipToTopicMixSeq(0).asInstanceOf[String], ipToTopicMixSeq(1).asInstanceOf[Seq[Double]]
          .toArray)
      })
      .collectAsMap
      .toMap


    new DNSSuspiciousConnectsModel(topicCount,
      ipToTopicMix,
      wordToPerTopicProb,
      timeCuts,
      frameLengthCuts,
      subdomainLengthCuts,
      numberPeriodsCuts,
      entropyCuts)

  }

  /**
    * Add  domain statistics fields to a data frame.
    *
    * @param sparkContext   Spark context.
    * @param sqlContext     Spark SQL context.
    * @param countryCodesBC Broadcast of the country codes set.
    * @param topDomainsBC   Broadcast of the most-popular domains set.
    * @param userDomain     Domain associated to network data (ex: 'intel')
    * @param inDF           Incoming dataframe. Schema is expected to provide the field [[QueryName]]
    * @return A new dataframe with the new columns added. The new columns have the schema [[DomainStatsSchema]]
    */

  def createDomainStatsDF(sparkContext: SparkContext,
                          sqlContext: SQLContext,
                          countryCodesBC: Broadcast[Set[String]],
                          topDomainsBC: Broadcast[Set[String]],
                          userDomain: String,
                          inDF: DataFrame): DataFrame = {

    val queryNameIndex = inDF.schema.fieldNames.indexOf(QueryName)

    val domainStatsRDD: RDD[Row] = inDF.rdd.map(row =>
      Row.fromTuple(createTempFields(countryCodesBC, topDomainsBC, userDomain, row.getString(queryNameIndex))))

    sqlContext.createDataFrame(domainStatsRDD, DomainStatsSchema)
  }


  case class TempFields(topDomainClass: Int, subdomainLength: Integer, subdomainEntropy: Double, numPeriods: Integer)

  /**
    *
    * @param countryCodesBC Broadcast of the country codes set.
    * @param topDomainsBC   Broadcast of the most-popular domains set.
    * @param userDomain     Domain associated to network data (ex: 'intel')
    * @param url            URL string to anlayze for domain and subdomain information.
    * @return [[TempFields]]
    */
  def createTempFields(countryCodesBC: Broadcast[Set[String]],
                       topDomainsBC: Broadcast[Set[String]],
                       userDomain: String,
                       url: String): TempFields = {

    val DomainInfo(_, topDomainClass, subdomain, subdomainLength, subdomainEntropy, numPeriods) =
      DomainProcessor.extractDomainInfo(url, topDomainsBC, userDomain)


    TempFields(topDomainClass = topDomainClass,
      subdomainLength = subdomainLength,
      subdomainEntropy = subdomainEntropy,
      numPeriods = numPeriods)
  }
}