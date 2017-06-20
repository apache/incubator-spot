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

package org.apache.spot.proxy

import org.apache.log4j.Logger
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.SuspiciousConnectsScoreFunction
import org.apache.spot.lda.SpotLDAWrapper
import org.apache.spot.lda.SpotLDAWrapper.{SpotLDAInput, SpotLDAOutput}
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.proxy.ProxySchema._
import org.apache.spot.utilities._
import org.apache.spot.utilities.data.validation.InvalidDataHandler

/**
  * Encapsulation of a proxy suspicious connections model.
  *
  * @param topicCount         Number of "topics" used to cluster IPs and proxy "words" in the topic modelling analysis.
  * @param ipToTopicMIx       Maps each IP to a vector measuring Prob[ topic | this IP] for each topic.
  * @param wordToPerTopicProb Maps each word to a vector measuring Prob[word | topic] for each topic.
  */
class ProxySuspiciousConnectsModel(topicCount: Int,
                                   ipToTopicMIx: DataFrame,
                                   wordToPerTopicProb: Map[String, Array[Double]]) {

  /**
    * Calculate suspicious connection scores for an incoming dataframe using this proxy suspicious connects model.
    *
    * @param sparkSession Spark Session.
    * @param dataFrame    Dataframe with columns Host, Time, ReqMethod, FullURI, ResponseContentType, UserAgent, RespCode
    *                     (as defined in ProxySchema object).
    * @return Dataframe with Score column added.
    */
  def score(sparkSession: SparkSession, dataFrame: DataFrame, precisionUtility: FloatPointPrecisionUtility): DataFrame = {

    val topDomains: Broadcast[Set[String]] = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)

    val agentToCount: Map[String, Long] =
      dataFrame.select(UserAgent).rdd.map({ case Row(ua: String) => (ua, 1L) }).reduceByKey(_ + _).collect().toMap

    val agentToCountBC = sparkSession.sparkContext.broadcast(agentToCount)

    val udfWordCreation =
      ProxyWordCreation.udfWordCreation(topDomains, agentToCountBC)

    val wordedDataFrame = dataFrame.withColumn(Word,
      udfWordCreation(dataFrame(Host),
        dataFrame(Time),
        dataFrame(ReqMethod),
        dataFrame(FullURI),
        dataFrame(ResponseContentType),
        dataFrame(UserAgent),
        dataFrame(RespCode)))

    val wordToPerTopicProbBC = sparkSession.sparkContext.broadcast(wordToPerTopicProb)

    val scoreFunction = new SuspiciousConnectsScoreFunction(topicCount, wordToPerTopicProbBC)

    def udfScoreFunction = udf((documentTopicMix: Seq[precisionUtility.TargetType], word: String) =>
      scoreFunction.score(precisionUtility)(documentTopicMix, word))

    wordedDataFrame
      .join(org.apache.spark.sql.functions.broadcast(ipToTopicMIx), dataFrame(ClientIP) === ipToTopicMIx(DocumentName), "left_outer")
      .selectExpr(wordedDataFrame.schema.fieldNames :+ TopicProbabilityMix: _*)
      .withColumn(Score, udfScoreFunction(col(TopicProbabilityMix), col(Word)))
      .drop(TopicProbabilityMix)
  }
}

/**
  * Contains model creation and training routines.
  */
object ProxySuspiciousConnectsModel {

  // These buckets are optimized to datasets used for training. Last bucket is of infinite size to ensure fit.
  // The maximum value of entropy is given by log k where k is the number of distinct categories.
  // Given that the alphabet and number of characters is finite the maximum value for entropy is upper bounded.
  // Bucket number and size can be changed to provide less/more granularity
  val EntropyCuts = Array(0.0, 0.3, 0.6, 0.9, 1.2,
    1.5, 1.8, 2.1, 2.4, 2.7,
    3.0, 3.3, 3.6, 3.9, 4.2,
    4.5, 4.8, 5.1, 5.4, Double.PositiveInfinity)

  /**
    * Factory for ProxySuspiciousConnectsModel.
    * Trains the model from the incoming DataFrame using the specified number of topics
    * for clustering in the topic model.
    *
    * @param sparkSession Spark Session
    * @param logger       Logge object.
    * @param config       SuspiciousConnetsArgumnetParser.Config object containg CLI arguments.
    * @param inputRecords Dataframe for training data, with columns Host, Time, ReqMethod, FullURI, ResponseContentType,
    *                     UserAgent, RespCode (as defined in ProxySchema object).
    * @return ProxySuspiciousConnectsModel
    */
  def trainModel(sparkSession: SparkSession,
                 logger: Logger,
                 config: SuspiciousConnectsConfig,
                 inputRecords: DataFrame): ProxySuspiciousConnectsModel = {

    logger.info("training new proxy suspcious connects model")


    val selectedRecords =
      inputRecords.select(Date, Time, ClientIP, Host, ReqMethod, UserAgent, ResponseContentType, RespCode, FullURI)
        .unionAll(ProxyFeedback.loadFeedbackDF(sparkSession, config.feedbackFile, config.duplicationFactor))



    val agentToCount: Map[String, Long] =
      selectedRecords.select(UserAgent)
        .rdd
        .map({ case Row(agent: String) => (agent, 1L) })
        .reduceByKey(_ + _).collect()
        .toMap

    val agentToCountBC = sparkSession.sparkContext.broadcast(agentToCount)

    val docWordCount: RDD[SpotLDAInput] =
      getIPWordCounts(sparkSession, logger, selectedRecords, config.feedbackFile, config.duplicationFactor,
        agentToCount)

    val SpotLDAOutput(ipToTopicMixDF, wordResults) = SpotLDAWrapper.runLDA(sparkSession,
      docWordCount,
      config.topicCount,
      logger,
      config.ldaPRGSeed,
      config.ldaAlpha,
      config.ldaBeta,
      config.ldaOptimizer,
      config.ldaMaxiterations,
      config.precisionUtility)

    new ProxySuspiciousConnectsModel(config.topicCount, ipToTopicMixDF, wordResults)

  }

  /**
    * Transform proxy log events into summarized words and aggregate into IP-word counts.
    * Returned as [[SpotLDAInput]] objects.
    *
    * @return RDD of [[SpotLDAInput]] objects containing the aggregated IP-word counts.
    */
  def getIPWordCounts(sparkSession: SparkSession,
                      logger: Logger,
                      inputRecords: DataFrame,
                      feedbackFile: String,
                      duplicationFactor: Int,
                      agentToCount: Map[String, Long]): RDD[SpotLDAInput] = {


    logger.info("Read source data")
    val selectedRecords = inputRecords.select(Date, Time, ClientIP, Host, ReqMethod, UserAgent, ResponseContentType, RespCode, FullURI)

    val wc = ipWordCountFromDF(sparkSession, selectedRecords, agentToCount)
    logger.info("proxy pre LDA completed")

    wc
  }

  def ipWordCountFromDF(sparkSession: SparkSession,
                        dataFrame: DataFrame,
                        agentToCount: Map[String, Long]): RDD[SpotLDAInput] = {

    val topDomains: Broadcast[Set[String]] = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)

    val agentToCountBC = sparkSession.sparkContext.broadcast(agentToCount)
    val udfWordCreation = ProxyWordCreation.udfWordCreation(topDomains, agentToCountBC)

    val ipWord = dataFrame.withColumn(Word,
      udfWordCreation(dataFrame(Host),
        dataFrame(Time),
        dataFrame(ReqMethod),
        dataFrame(FullURI),
        dataFrame(ResponseContentType),
        dataFrame(UserAgent),
        dataFrame(RespCode)))
      .select(ClientIP, Word)

    ipWord
      .filter(ipWord(Word).notEqual(InvalidDataHandler.WordError))
      .rdd
      .map({ case Row(ip, word) => ((ip.asInstanceOf[String], word.asInstanceOf[String]), 1) })
      .reduceByKey(_ + _).map({ case ((ip, word), count) => SpotLDAInput(ip, word, count) })
  }
}