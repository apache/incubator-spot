package org.apache.spot.netflow.model

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.WideUDFs.udf
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.lda.SpotLDAWrapper
import org.apache.spot.lda.SpotLDAWrapper.{SpotLDAInput, SpotLDAOutput}
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.netflow.FlowWordCreator
import org.apache.spot.utilities.Quantiles
import org.apache.spot.utilities.data.validation.InvalidDataHandler

import scala.util.{Failure, Success, Try}

/**
  * A probabilistic model of the netflow traffic observed in a network.
  *
  * The model uses a topic-modelling approach that:
  * 1. Simplifies netflow records into words, one word at the source IP and another (possibly different) at the
  *    destination IP.
  * 2. The netflow words about each IP are treated as collections of thes words.
  * 3. A topic modelling approach is used to infer a collection of "topics" that represent common profiles
  *    of network traffic. These "topics" are probability distributions on words.
  * 4. Each IP has a mix of topics corresponding to its behavior.
  * 5. The probability of a word appearing in the traffic about an IP is estimated by simplifying its netflow record
  *    into a word, and then combining the word probabilities per topic using the topic mix of the particular IP.
  *
  * Create these models using the  factory in the companion object.
  *
  * @param topicCount Number of topics (profiles of common traffic patterns) used in the topic modelling routine.
  * @param ipToTopicMix DataFrame assigning a distribution on topics to each document or IP.
  * @param wordToPerTopicProb Map assigning to each word it's per-topic probabilities.
  *                           Ie. Prob [word | t ] for t = 0 to topicCount -1
  * @param timeCuts Quantile cut-offs for binning time-of-day values when forming words from netflow records.
  * @param ibytCuts Quantile cut-offs for binning ibyt values when forming words from netflow records.
  * @param ipktCuts Quantile cut-offs for binning ipkt values when forming words from netflow records.
  */

class FlowSuspiciousConnectsModel(topicCount: Int,
                                  ipToTopicMix: DataFrame,
                                  wordToPerTopicProb: Map[String, Array[Double]],
                                  timeCuts: Array[Double],
                                  ibytCuts: Array[Double],
                                  ipktCuts: Array[Double]) {

  def score(sc: SparkContext, sqlContext: SQLContext, flowRecords: DataFrame): DataFrame = {

    val wordToPerTopicProbBC = sc.broadcast(wordToPerTopicProb)


    /** A left outer join (below) takes rows from the left DF for which the join expression is not
      * satisfied (for any entry in the right DF), and fills in 'null' values (for the additional columns).
      */
    val dataWithSrcTopicMix = {

      val recordsWithSrcIPTopicMixes = flowRecords.join(ipToTopicMix,
        flowRecords(SourceIP) === ipToTopicMix(DocumentName), "left_outer")
      val schemaWithSrcTopicMix = flowRecords.schema.fieldNames :+ TopicProbabilityMix
      val dataWithSrcIpProb: DataFrame = recordsWithSrcIPTopicMixes.selectExpr(schemaWithSrcTopicMix: _*)
        .withColumnRenamed(TopicProbabilityMix, SrcIpTopicMix)

      val recordsWithIPTopicMixes = dataWithSrcIpProb.join(ipToTopicMix,
        dataWithSrcIpProb(DestinationIP) === ipToTopicMix(DocumentName), "left_outer")
      val schema = dataWithSrcIpProb.schema.fieldNames :+  TopicProbabilityMix
        recordsWithIPTopicMixes.selectExpr(schema: _*).withColumnRenamed(TopicProbabilityMix, DstIpTopicMix)
    }

    val scoreFunction =  new FlowScoreFunction(timeCuts,
        ibytCuts,
        ipktCuts,
        topicCount,
        wordToPerTopicProbBC)


    val scoringUDF = udf((hour: Int,
                          minute: Int,
                          second: Int,
                          srcIP: String,
                          dstIP: String,
                          srcPort: Int,
                          dstPort: Int,
                          ipkt: Long,
                          ibyt: Long,
                          srcIpTopicMix: Seq[Double],
                          dstIpTopicMix: Seq[Double]) =>
      scoreFunction.score(hour,
        minute,
        second,
        srcIP,
        dstIP,
        srcPort,
        dstPort,
        ipkt,
        ibyt,
        srcIpTopicMix,
        dstIpTopicMix))


    dataWithSrcTopicMix.withColumn(Score,
      scoringUDF(FlowSuspiciousConnectsModel.ModelColumns :+ col(SrcIpTopicMix) :+ col(DstIpTopicMix): _*))

  }

}

/**
  * Contains dataframe schema information as well as the train-from-dataframe routine
  * (which is a kind of factory routine) for [[FlowSuspiciousConnectsModel]] instances.
  *
  */
object FlowSuspiciousConnectsModel {

  val ModelSchema = StructType(List(HourField,
    MinuteField,
    SecondField,
    SourceIPField,
    DestinationIPField,
    SourcePortField,
    DestinationPortField,
    IpktField,
    IbytField))

  val ModelColumns = ModelSchema.fieldNames.toList.map(col)

  def trainNewModel(sparkContext: SparkContext,
                    sqlContext: SQLContext,
                    logger: Logger,
                    config: SuspiciousConnectsConfig,
                    inputRecords: DataFrame,
                    topicCount: Int): FlowSuspiciousConnectsModel = {

    logger.info("Training netflow suspicious connects model from " + config.inputPath)

    val selectedRecords = inputRecords.select(ModelColumns: _*)


    val totalRecords = selectedRecords.unionAll(FlowFeedback.loadFeedbackDF(sparkContext,
      sqlContext,
      config.feedbackFile,
      config.duplicationFactor))

    // create quantile cut-offs

    val timeCuts = Quantiles.computeDeciles(totalRecords
      .select(Hour, Minute, Second)
      .rdd
      .flatMap({ case Row(hours: Int, minutes: Int, seconds: Int) => {
          Try {  (3600 * hours + 60 * minutes + seconds).toDouble } match{
            case Failure(_) => Seq()
            case Success(time) => Seq(time)
          }
        }
      }))

    logger.info(timeCuts.mkString(","))

    logger.info("calculating byte cuts ...")

    val ibytCuts = Quantiles.computeDeciles(totalRecords
      .select(Ibyt)
      .rdd
      .flatMap({ case Row(ibyt: Long) => {
          Try {  ibyt.toDouble } match{
            case Failure(_) => Seq()
            case Success(ibyt) => Seq(ibyt)
          }
        }
      }))

    logger.info(ibytCuts.mkString(","))

    logger.info("calculating pkt cuts")

    val ipktCuts = Quantiles.computeQuintiles(totalRecords
      .select(Ipkt)
      .rdd
      .flatMap({ case Row(ipkt: Long) => {
          Try { ipkt.toDouble } match {
            case Failure(_) => Seq()
            case Success(ipkt) => Seq(ipkt)
          }
        }
      }))

    logger.info(ipktCuts.mkString(","))

    // simplify DNS log entries into "words"

    val flowWordCreator = new FlowWordCreator(timeCuts, ibytCuts, ipktCuts)

    val dataWithWords = totalRecords.withColumn(SourceWord, flowWordCreator.srcWordUDF(ModelColumns: _*))
      .withColumn(DestinationWord, flowWordCreator.dstWordUDF(ModelColumns: _*))

    // Aggregate per-word counts at each IP
    val srcWordCounts = dataWithWords
      .filter(dataWithWords(SourceWord).notEqual(InvalidDataHandler.WordError))
      .select(SourceIP, SourceWord)
      .map({ case Row(sourceIp: String, sourceWord: String) => (sourceIp, sourceWord) -> 1 })
      .reduceByKey(_ + _)

    val dstWordCounts = dataWithWords
      .filter(dataWithWords(DestinationWord).notEqual(InvalidDataHandler.WordError))
      .select(DestinationIP, DestinationWord)
      .map({ case Row(destinationIp: String, destinationWord: String) => (destinationIp, destinationWord) -> 1 })
      .reduceByKey(_ + _)

    val ipWordCounts =
      sparkContext.union(srcWordCounts, dstWordCounts)
        .reduceByKey(_ + _)
        .map({ case ((ip, word), count) => SpotLDAInput(ip, word, count) })


    val SpotLDAOutput(ipToTopicMix, wordToPerTopicProb) = SpotLDAWrapper.runLDA(sparkContext,
      sqlContext,
      ipWordCounts,
      config.topicCount,
      logger,
      config.ldaPRGSeed,
      config.ldaAlpha,
      config.ldaBeta,
      config.ldaMaxiterations)

    new FlowSuspiciousConnectsModel(topicCount,
      ipToTopicMix,
      wordToPerTopicProb,
      timeCuts,
      ibytCuts,
      ipktCuts)
  }

}