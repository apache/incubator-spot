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

package org.apache.spot.netflow.model

import org.apache.log4j.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.lda.SpotLDAWrapper
import org.apache.spot.lda.SpotLDAWrapper.{SpotLDAInput, SpotLDAOutput}
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.netflow.FlowWordCreator
import org.apache.spot.utilities.FloatPointPrecisionUtility
import org.apache.spot.utilities.data.validation.InvalidDataHandler

/**
  * A probabilistic model of the netflow traffic observed in a network.
  *
  * The model uses a topic-modelling approach that:
  * 1. Simplifies netflow records into words, one word at the source IP and another (possibly different) at the
  * destination IP.
  * 2. The netflow words about each IP are treated as collections of thes words.
  * 3. A topic modelling approach is used to infer a collection of "topics" that represent common profiles
  * of network traffic. These "topics" are probability distributions on words.
  * 4. Each IP has a mix of topics corresponding to its behavior.
  * 5. The probability of a word appearing in the traffic about an IP is estimated by simplifying its netflow record
  * into a word, and then combining the word probabilities per topic using the topic mix of the particular IP.
  *
  * Create these models using the  factory in the companion object.
  *
  * @param topicCount         Number of topics (profiles of common traffic patterns) used in the topic modelling routine.
  * @param ipToTopicMix       DataFrame assigning a distribution on topics to each document or IP.
  * @param wordToPerTopicProb Map assigning to each word it's per-topic probabilities.
  *                           Ie. Prob [word | t ] for t = 0 to topicCount -1
  */

class FlowSuspiciousConnectsModel(topicCount: Int,
                                  ipToTopicMix: DataFrame,
                                  wordToPerTopicProb: Map[String, Array[Double]]) {

  def score(sparkSession: SparkSession, flowRecords: DataFrame, precisionUtility: FloatPointPrecisionUtility): DataFrame = {

    val wordToPerTopicProbBC = sparkSession.sparkContext.broadcast(wordToPerTopicProb)


    /** A left outer join (below) takes rows from the left DF for which the join expression is not
      * satisfied (for any entry in the right DF), and fills in 'null' values (for the additional columns).
      */
    val dataWithSrcTopicMix = {

      val recordsWithSrcIPTopicMixes = flowRecords.join(org.apache.spark.sql.functions.broadcast(ipToTopicMix),
        flowRecords(SourceIP) === ipToTopicMix(DocumentName), "left_outer")
      val schemaWithSrcTopicMix = flowRecords.schema.fieldNames :+ TopicProbabilityMix
      val dataWithSrcIpProb: DataFrame = recordsWithSrcIPTopicMixes.selectExpr(schemaWithSrcTopicMix: _*)
        .withColumnRenamed(TopicProbabilityMix, SrcIpTopicMix)

      val recordsWithIPTopicMixes = dataWithSrcIpProb.join(org.apache.spark.sql.functions.broadcast(ipToTopicMix),
        dataWithSrcIpProb(DestinationIP) === ipToTopicMix(DocumentName), "left_outer")
      val schema = dataWithSrcIpProb.schema.fieldNames :+ TopicProbabilityMix
      recordsWithIPTopicMixes.selectExpr(schema: _*).withColumnRenamed(TopicProbabilityMix, DstIpTopicMix)
    }


    val scoreFunction = new FlowScoreFunction(topicCount, wordToPerTopicProbBC)

    import org.apache.spark.sql.functions.udf

    val scoringUDF = udf((hour: Int,
                          srcIP: String,
                          dstIP: String,
                          srcPort: Int,
                          dstPort: Int,
                          protocol: String,
                          ibyt: Long,
                          ipkt: Long,
                          srcIpTopicMix: Seq[precisionUtility.TargetType],
                          dstIpTopicMix: Seq[precisionUtility.TargetType]) =>
      scoreFunction.score(precisionUtility)(hour,
        srcIP,
        dstIP,
        srcPort,
        dstPort,
        protocol,
        ibyt,
        ipkt,
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
    SourceIPField,
    DestinationIPField,
    SourcePortField,
    DestinationPortField,
    ProtocolField,
    IbytField,
    IpktField))

  val ModelColumns = ModelSchema.fieldNames.toList.map(col)


  def trainModel(sparkSession: SparkSession,
                 logger: Logger,
                 config: SuspiciousConnectsConfig,
                 inputRecords: DataFrame): FlowSuspiciousConnectsModel = {


    logger.info("Training netflow suspicious connects model from " + config.inputPath)

    val selectedRecords = inputRecords.select(ModelColumns: _*)


    val totalRecords = selectedRecords.unionAll(FlowFeedback.loadFeedbackDF(sparkSession,
      config.feedbackFile,
      config.duplicationFactor))


    // simplify netflow log entries into "words"

    val dataWithWords = totalRecords.withColumn(SourceWord, FlowWordCreator.srcWordUDF(ModelColumns: _*))
      .withColumn(DestinationWord, FlowWordCreator.dstWordUDF(ModelColumns: _*))

    import sparkSession.implicits._

    // Aggregate per-word counts at each IP
    val srcWordCounts = dataWithWords
      .filter(dataWithWords(SourceWord).notEqual(InvalidDataHandler.WordError))
      .select(SourceIP, SourceWord)
      .map({ case Row(sourceIp: String, sourceWord: String) => (sourceIp, sourceWord) -> 1 })
      .rdd
      .reduceByKey(_ + _)

    val dstWordCounts = dataWithWords
      .filter(dataWithWords(DestinationWord).notEqual(InvalidDataHandler.WordError))
      .select(DestinationIP, DestinationWord)
      .map({ case Row(destinationIp: String, destinationWord: String) => (destinationIp, destinationWord) -> 1 })
      .rdd
      .reduceByKey(_ + _)

    val ipWordCounts =
      sparkSession.sparkContext.union(srcWordCounts, dstWordCounts)
        .reduceByKey(_ + _)
        .map({ case ((ip, word), count) => SpotLDAInput(ip, word, count) })


    val SpotLDAOutput(ipToTopicMix, wordToPerTopicProb) = SpotLDAWrapper.runLDA(sparkSession,
      ipWordCounts,
      config.topicCount,
      logger,
      config.ldaPRGSeed,
      config.ldaAlpha,
      config.ldaBeta,
      config.ldaOptimizer,
      config.ldaMaxiterations,
      config.precisionUtility)

    new FlowSuspiciousConnectsModel(config.topicCount,
      ipToTopicMix,
      wordToPerTopicProb)
  }
}