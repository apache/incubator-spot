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
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.SuspiciousConnectsScoreFunction
import org.apache.spot.lda.SpotLDAWrapper
import org.apache.spot.lda.SpotLDAWrapper.{SpotLDAInput, SpotLDAOutput}
import org.apache.spot.proxy.ProxySchema._
import org.apache.spot.utilities._
import org.apache.spot.utilities.data.validation.InvalidDataHandler

import scala.util.{Failure, Success, Try}

/**
  * Encapsulation of a proxy suspicious connections model.
  *
  * @param topicCount         Number of "topics" used to cluster IPs and proxy "words" in the topic modelling analysis.
  * @param ipToTopicMIx       Maps each IP to a vector measuring Prob[ topic | this IP] for each topic.
  * @param wordToPerTopicProb Maps each word to a vector measuring Prob[word | topic] for each topic.
  * @param timeCuts           Decile cutoffs for time-of-day in seconds.
  * @param entropyCuts        Quintile cutoffs for measurement of full URI string entropy.
  * @param agentCuts          Quintiile cutoffs for frequency of user agent.
  */
class ProxySuspiciousConnectsModel(topicCount: Int,
                                   ipToTopicMIx: Map[String, Array[Double]],
                                   wordToPerTopicProb: Map[String, Array[Double]],
                                   timeCuts: Array[Double],
                                   entropyCuts: Array[Double],
                                   agentCuts: Array[Double]) {

  /**
    * Calculate suspicious connection scores for an incoming dataframe using this proxy suspicious connects model.
    *
    * @param sc        Spark context.
    * @param dataFrame Dataframe with columns Host, Time, ReqMethod, FullURI, ResponseContentType, UserAgent, RespCode
    *                  (as defined in ProxySchema object).
    * @return Dataframe with Score column added.
    */
  def score(sc: SparkContext, dataFrame: DataFrame): DataFrame = {

    val topDomains: Broadcast[Set[String]] = sc.broadcast(TopDomains.TopDomains)

    val agentToCount: Map[String, Long] =
      dataFrame.select(UserAgent).rdd.map({ case Row(ua: String) => (ua, 1L) }).reduceByKey(_ + _).collect().toMap

    val agentToCountBC = sc.broadcast(agentToCount)

    val udfWordCreation =
      ProxyWordCreation.udfWordCreation(topDomains, agentToCountBC, timeCuts, entropyCuts, agentCuts)

    val wordedDataFrame = dataFrame.withColumn(Word,
      udfWordCreation(dataFrame(Host),
        dataFrame(Time),
        dataFrame(ReqMethod),
        dataFrame(FullURI),
        dataFrame(ResponseContentType),
        dataFrame(UserAgent),
        dataFrame(RespCode)))

    val ipToTopicMixBC = sc.broadcast(ipToTopicMIx)
    val wordToPerTopicProbBC = sc.broadcast(wordToPerTopicProb)


    val scoreFunction = new SuspiciousConnectsScoreFunction(topicCount, ipToTopicMixBC, wordToPerTopicProbBC)


    def udfScoreFunction = udf((ip: String, word: String) => scoreFunction.score(ip, word))
    wordedDataFrame.withColumn(Score, udfScoreFunction(wordedDataFrame(ClientIP), wordedDataFrame(Word)))
  }
}

/**
  * Contains model creation and training routines.
  */
object ProxySuspiciousConnectsModel {

  /**
    * Factory for ProxySuspiciousConnectsModel.
    * Trains the model from the incoming DataFrame using the specified number of topics
    * for clustering in the topic model.
    *
    * @param sparkContext Spark context.
    * @param sqlContext   SQL context.
    * @param logger       Logge object.
    * @param config       SuspiciousConnetsArgumnetParser.Config object containg CLI arguments.
    * @param inputRecords Dataframe for training data, with columns Host, Time, ReqMethod, FullURI, ResponseContentType,
    *                     UserAgent, RespCode (as defined in ProxySchema object).
    * @return ProxySuspiciousConnectsModel
    */
  def trainNewModel(sparkContext: SparkContext,
                    sqlContext: SQLContext,
                    logger: Logger,
                    config: SuspiciousConnectsConfig,
                    inputRecords: DataFrame): ProxySuspiciousConnectsModel = {

    logger.info("training new proxy suspcious connects model")


    val selectedRecords = inputRecords.select(Date, Time, ClientIP, Host, ReqMethod, UserAgent, ResponseContentType, RespCode, FullURI)
      .unionAll(ProxyFeedback.loadFeedbackDF(sparkContext, sqlContext, config.feedbackFile, config.duplicationFactor))

    val timeCuts =
      Quantiles.computeDeciles(selectedRecords
        .select(Time)
        .rdd
        .flatMap({ case Row(t: String) =>
          Try {
            TimeUtilities.getTimeAsDouble(t)
          } match {
            case Failure(_) => Seq()
            case Success(time) => Seq(time)

          }
        }))

    val entropyCuts = Quantiles.computeQuintiles(selectedRecords
      .select(FullURI)
      .rdd
      .flatMap({ case Row(uri: String) =>
        Try {
          Entropy.stringEntropy(uri)
        } match {
          case Failure(_) => Seq()
          case Success(entropy) => Seq(entropy)
        }

      }))

    val agentToCount: Map[String, Long] =
      selectedRecords.select(UserAgent)
        .rdd
        .map({ case Row(agent: String) => (agent, 1L) })
        .reduceByKey(_ + _).collect()
        .toMap

    val agentToCountBC = sparkContext.broadcast(agentToCount)

    val agentCuts =
      Quantiles.computeQuintiles(selectedRecords
        .select(UserAgent)
        .rdd
        .map({ case Row(agent: String) => agentToCountBC.value(agent) }))

    val docWordCount: RDD[SpotLDAInput] =
      getIPWordCounts(sparkContext, sqlContext, logger, selectedRecords, config.feedbackFile, config.duplicationFactor, agentToCount, timeCuts, entropyCuts, agentCuts)


    val SpotLDAOutput(ipToTopicMixDF, wordResults) = SpotLDAWrapper.runLDA(sparkContext,
      sqlContext,
      docWordCount,
      config.topicCount,
      logger,
      config.ldaPRGSeed,
      config.ldaAlpha,
      config.ldaBeta,
      config.ldaMaxiterations)


    // Since Proxy is still broadcasting ip to topic mix, we need to convert data frame to Map[String, Array[Double]]
    val ipToTopicMix = ipToTopicMixDF
      .rdd
      .map({ case (ipToTopicMixRow: Row) => ipToTopicMixRow.toSeq.toArray })
      .map({
        case (ipToTopicMixSeq) => (ipToTopicMixSeq(0).asInstanceOf[String], ipToTopicMixSeq(1).asInstanceOf[Seq[Double]]
          .toArray)
      })
      .collectAsMap
      .toMap


    new ProxySuspiciousConnectsModel(config.topicCount, ipToTopicMix, wordResults, timeCuts, entropyCuts, agentCuts)

  }

  /**
    * Transform proxy log events into summarized words and aggregate into IP-word counts.
    * Returned as [[SpotLDAInput]] objects.
    *
    * @return RDD of [[SpotLDAInput]] objects containing the aggregated IP-word counts.
    */
  def getIPWordCounts(sc: SparkContext,
                      sqlContext: SQLContext,
                      logger: Logger,
                      inputRecords: DataFrame,
                      feedbackFile: String,
                      duplicationFactor: Int,
                      agentToCount: Map[String, Long],
                      timeCuts: Array[Double],
                      entropyCuts: Array[Double],
                      agentCuts: Array[Double]): RDD[SpotLDAInput] = {


    logger.info("Read source data")
    val selectedRecords = inputRecords.select(Date, Time, ClientIP, Host, ReqMethod, UserAgent, ResponseContentType, RespCode, FullURI)

    val wc = ipWordCountFromDF(sc, selectedRecords, agentToCount, timeCuts, entropyCuts, agentCuts)
    logger.info("proxy pre LDA completed")

    wc
  }

  def ipWordCountFromDF(sc: SparkContext,
                        dataFrame: DataFrame,
                        agentToCount: Map[String, Long],
                        timeCuts: Array[Double],
                        entropyCuts: Array[Double],
                        agentCuts: Array[Double]): RDD[SpotLDAInput] = {

    val topDomains: Broadcast[Set[String]] = sc.broadcast(TopDomains.TopDomains)

    val agentToCountBC = sc.broadcast(agentToCount)
    val udfWordCreation = ProxyWordCreation.udfWordCreation(topDomains, agentToCountBC, timeCuts, entropyCuts, agentCuts)

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