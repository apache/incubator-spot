/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spot.dns.model

import org.apache.log4j.Logger
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSchema._
import org.apache.spot.dns.DNSWordCreation
import org.apache.spot.lda.SpotLDAWrapper
import org.apache.spot.lda.SpotLDAWrapper._
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.utilities.DomainProcessor.DomainInfo
import org.apache.spot.utilities._
import org.apache.spot.utilities.data.validation.InvalidDataHandler

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
  */
class DNSSuspiciousConnectsModel(inTopicCount: Int,
                                 inIpToTopicMix: DataFrame,
                                 inWordToPerTopicProb: Map[String, Array[Double]]) {

  val topicCount = inTopicCount
  val ipToTopicMix = inIpToTopicMix
  val wordToPerTopicProb = inWordToPerTopicProb

  /**
    * Use a suspicious connects model to assign estimated probabilities to a dataframe of
    * DNS log events.
    *
    * @param sparkSession Spark Session
    * @param inDF         Dataframe of DNS log events, containing at least the columns of [[DNSSuspiciousConnectsModel.ModelSchema]]
    * @param userDomain   Domain associated to network data (ex: 'intel')
    * @return Dataframe with a column named [[org.apache.spot.dns.DNSSchema.Score]] that contains the
    *         probability estimated for the network event at that row
    */
  def score(sparkSession: SparkSession, inDF: DataFrame, userDomain: String, precisionUtility: FloatPointPrecisionUtility): DataFrame = {

    val topDomainsBC = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)
    val wordToPerTopicProbBC = sparkSession.sparkContext.broadcast(wordToPerTopicProb)

    val scoreFunction =
      new DNSScoreFunction(topicCount,
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
                          queryResponseCode: Int,
                          documentTopicMix: Seq[precisionUtility.TargetType]) =>
      scoreFunction.score(precisionUtility)(timeStamp,
        unixTimeStamp,
        frameLength,
        clientIP,
        queryName,
        queryClass,
        queryType,
        queryResponseCode,
        documentTopicMix))

    inDF
      .join(org.apache.spark.sql.functions.broadcast(ipToTopicMix), inDF(ClientIP) === ipToTopicMix(DocumentName),
        "left_outer")
      .selectExpr(inDF.schema.fieldNames :+ TopicProbabilityMix: _*)
      .withColumn(Score, scoringUDF(DNSSuspiciousConnectsModel.modelColumns :+ col(TopicProbabilityMix): _*))
      .drop(TopicProbabilityMix)
  }
}

/**
  * Contains dataframe schema information as well as the train-from-dataframe routine
  * (which is a kind of factory routine) for [[DNSSuspiciousConnectsModel]] instances.
  *
  */
object DNSSuspiciousConnectsModel {

  val ModelSchema = StructType(List(TimeStampField,
    UnixTimeStampField,
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
    * @param sparkSession Spark Session
    * @param logger
    * @param config       Analysis configuration object containing CLI parameters.
    *                     Contains the path to the feedback file in config.scoresFile
    * @param inputRecords Data used to train the model.
    * @return A new [[DNSSuspiciousConnectsModel]] instance trained on the dataframe and feedback file.
    */
  def trainModel(sparkSession: SparkSession,
                 logger: Logger,
                 config: SuspiciousConnectsConfig,
                 inputRecords: DataFrame): DNSSuspiciousConnectsModel = {

    logger.info("Training DNS suspicious connects model from " + config.inputPath)

    val selectedRecords = inputRecords.select(modelColumns: _*)

    val totalRecords = selectedRecords.union(DNSFeedback.loadFeedbackDF(sparkSession,
      config.feedbackFile,
      config.duplicationFactor))

    val countryCodesBC = sparkSession.sparkContext.broadcast(CountryCodes.CountryCodes)
    val topDomainsBC = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)
    val userDomain = config.userDomain

    // simplify DNS log entries into "words"
    val dnsWordCreator = new DNSWordCreation(topDomainsBC, userDomain)
    val dataWithWord = totalRecords.withColumn(Word, dnsWordCreator.wordCreationUDF(modelColumns: _*))

    import sparkSession.implicits._

    // aggregate per-word counts at each IP
    val ipDstWordCounts =
      dataWithWord
        .select(ClientIP, Word)
        .filter(dataWithWord(Word).notEqual(InvalidDataHandler.WordError))
        .map({ case Row(destIP: String, word: String) => (destIP, word) -> 1 })
        .rdd
        .reduceByKey(_ + _)
        .map({ case ((ipDst, word), count) => SpotLDAInput(ipDst, word, count) })


    val SpotLDAOutput(ipToTopicMix, wordToPerTopicProb) = SpotLDAWrapper.runLDA(sparkSession,
      ipDstWordCounts,
      config.topicCount,
      logger,
      config.ldaPRGSeed,
      config.ldaAlpha,
      config.ldaBeta,
      config.ldaOptimizer,
      config.ldaMaxiterations,
      config.precisionUtility)

    new DNSSuspiciousConnectsModel(config.topicCount, ipToTopicMix, wordToPerTopicProb)

  }


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

  case class TempFields(topDomainClass: Int, subdomainLength: Integer, subdomainEntropy: Double, numPeriods: Integer)
}