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

package org.apache.spot.netflow

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spot.SuspiciousConnects.SuspiciousConnectsAnalysisResults
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.netflow.FlowSuspiciousConnectsAnalysis.InSchema
import org.apache.spot.netflow.model.FlowSuspiciousConnectsModel
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities.FloatPointPrecisionUtility32
import org.scalatest.Matchers



class FlowSuspiciousConnectsAnalysisTest extends TestingSparkContextFlatSpec with Matchers {


  val emTestConfig = SuspiciousConnectsConfig(analysis = "flow",
    inputPath = "",
    feedbackFile = "",
    duplicationFactor = 1,
    topicCount = 20,
    hdfsScoredConnect = "",
    threshold = 1.0d,
    maxResults = -1,
    outputDelimiter = "\t",
    ldaPRGSeed = None,
    ldaMaxiterations = 20,
    ldaAlpha = 1.02,
    ldaBeta = 1.001,
    ldaOptimizer = "em")

  val onlineTestConfig = SuspiciousConnectsConfig(analysis = "flow",
    inputPath = "",
    feedbackFile = "",
    duplicationFactor = 1,
    topicCount = 20,
    hdfsScoredConnect = "",
    threshold = 1.0d,
    maxResults = 1000,
    outputDelimiter = "\t",
    ldaPRGSeed = None,
    ldaMaxiterations = 200,
    ldaAlpha = 0.0009,
    ldaBeta = 0.00001,
    ldaOptimizer = "online")

  val testingConfigFloatConversion = SuspiciousConnectsConfig(analysis = "flow",
    inputPath = "",
    feedbackFile = "",
    duplicationFactor = 1,
    topicCount = 20,
    hdfsScoredConnect = "",
    threshold = 1.0d,
    maxResults = 1000,
    outputDelimiter = "\t",
    ldaPRGSeed = None,
    ldaMaxiterations = 20,
    ldaAlpha = 1.02,
    ldaBeta = 1.001,
    precisionUtility = FloatPointPrecisionUtility32)

  "netflow suspicious connects" should "correctly identify time-of-day anomalies using EMLDAOptimizer" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.OFF)

    val anomalousRecord = FlowRecord("2016-05-05 00:11:01", 2016, 5, 5, 0, 0, 1, 0.972d, "172.16.0.129", "10.0.2" +
      ".202", 1024, 80, "TCP", 39l, 12522l, 0l, 0l)
    val typicalRecord = FlowRecord("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2" +
      ".202", 1024, 80, "TCP", 39l, 12522l, 0l, 0l)

    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord,
      typicalRecord, typicalRecord, typicalRecord, typicalRecord))

    val SuspiciousConnectsAnalysisResults(scoredData, _) = FlowSuspiciousConnectsAnalysis.run(emTestConfig,
      sparkSession, logger, data)

    val anomalyScore = scoredData.filter(scoredData(Hour) === 0).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(Hour) === 13).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.1d) should be < 0.01
    typicalScores.length shouldBe 9
    Math.abs(typicalScores(0) - 0.9d) should be < 0.01
    Math.abs(typicalScores(1) - 0.9d) should be < 0.01
    Math.abs(typicalScores(2) - 0.9d) should be < 0.01
    Math.abs(typicalScores(3) - 0.9d) should be < 0.01
    Math.abs(typicalScores(4) - 0.9d) should be < 0.01
    Math.abs(typicalScores(5) - 0.9d) should be < 0.01
    Math.abs(typicalScores(6) - 0.9d) should be < 0.01
    Math.abs(typicalScores(7) - 0.9d) should be < 0.01
    Math.abs(typicalScores(8) - 0.9d) should be < 0.01

  }

  it should "correctly identify time-of-day anomalies using OnlineLDAOptimizer" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.OFF)

    val anomalousRecord = FlowRecord("2016-05-05 00:11:01", 2016, 5, 5, 0, 0, 1, 0.972f, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39, 12522, 0, 0)
    val typicalRecord = FlowRecord("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972f, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39, 12522, 0, 0)


    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord,
      typicalRecord, typicalRecord, typicalRecord, typicalRecord))

    val SuspiciousConnectsAnalysisResults(scoredData, _) = FlowSuspiciousConnectsAnalysis.run(onlineTestConfig,
      sparkSession, logger, data)

    val anomalyScore = scoredData.filter(scoredData(Hour) === 0).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(Hour) === 13).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.1d) should be < 0.01
    typicalScores.length shouldBe 9
    Math.abs(typicalScores(0) - 0.9d) should be < 0.01
    Math.abs(typicalScores(1) - 0.9d) should be < 0.01
    Math.abs(typicalScores(2) - 0.9d) should be < 0.01
    Math.abs(typicalScores(3) - 0.9d) should be < 0.01
    Math.abs(typicalScores(4) - 0.9d) should be < 0.01
    Math.abs(typicalScores(5) - 0.9d) should be < 0.01
    Math.abs(typicalScores(6) - 0.9d) should be < 0.01
    Math.abs(typicalScores(7) - 0.9d) should be < 0.01
    Math.abs(typicalScores(8) - 0.9d) should be < 0.01

  }

  it should "correctly identify time-of-day anomalies with testing config" in {

    val testConfig2 = SuspiciousConnectsConfig(analysis = "flow",
      inputPath = "",
      feedbackFile = "",
      duplicationFactor = 1,
      topicCount = 20,
      hdfsScoredConnect = "",
      threshold = 1.0d,
      maxResults = 1000,
      outputDelimiter = "\t",
      ldaPRGSeed = None,
      ldaMaxiterations = 20,
      ldaAlpha = 1.02,
      ldaBeta = 1.001)

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.INFO)

    val anomalousRecord = FlowRecord("2016-05-05 00:11:01", 2016, 5, 5, 0, 0, 1, 0.972d, "172.16.0.129", "10.0.2" +
      ".202", 1024, 80, "TCP", 39l, 12522l, 0l, 0l)
    val typicalRecord = FlowRecord("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2" +
      ".202", 1024, 80, "TCP", 39l, 12522l, 0l, 0l)


    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord,
      typicalRecord,
      typicalRecord, typicalRecord, typicalRecord, typicalRecord))


    val flows: DataFrame = FlowSuspiciousConnectsAnalysis.filterRecords(data).select(InSchema: _*)


    logger.info("Fitting probabilistic model to data")
    val model =
      FlowSuspiciousConnectsModel.trainModel(sparkSession, logger, testConfig2, flows)

    logger.info("Identifying outliers")
    val scoredData = model.score(sparkSession, flows, testConfig2.precisionUtility)

    val anomalyScore = scoredData.filter(scoredData(Hour) === 0).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(Hour) === 13).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.1d) should be < 0.01
    typicalScores.length shouldBe 9
    Math.abs(typicalScores(0) - 0.9d) should be < 0.01
    Math.abs(typicalScores(1) - 0.9d) should be < 0.01
    Math.abs(typicalScores(2) - 0.9d) should be < 0.01
    Math.abs(typicalScores(3) - 0.9d) should be < 0.01
    Math.abs(typicalScores(4) - 0.9d) should be < 0.01
    Math.abs(typicalScores(5) - 0.9d) should be < 0.01
    Math.abs(typicalScores(6) - 0.9d) should be < 0.01
    Math.abs(typicalScores(7) - 0.9d) should be < 0.01
    Math.abs(typicalScores(8) - 0.9d) should be < 0.01

  }

  it should "correctly identify time-of-day anomalies converting probabilities to Float for transportation and " +
    "converting back to Double" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.OFF)

    val anomalousRecord = FlowRecord("2016-05-05 00:11:01", 2016, 5, 5, 0, 0, 1, 0.972f, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39, 12522, 0, 0)
    val typicalRecord = FlowRecord("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972f, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39, 12522, 0, 0)


    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord,
      typicalRecord, typicalRecord, typicalRecord, typicalRecord))


    logger.info("Fitting probabilistic model to data")
    val model =
      FlowSuspiciousConnectsModel.trainModel(sparkSession, logger, testingConfigFloatConversion, data)

    logger.info("Identifying outliers")
    val scoredData = model.score(sparkSession, data, testingConfigFloatConversion.precisionUtility)

    val anomalyScore = scoredData.filter(scoredData(Hour) === 0).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(Hour) === 13).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.1d) should be < 0.01
    typicalScores.length shouldBe 9
    Math.abs(typicalScores(0) - 0.9d) should be < 0.01
    Math.abs(typicalScores(1) - 0.9d) should be < 0.01
    Math.abs(typicalScores(2) - 0.9d) should be < 0.01
    Math.abs(typicalScores(3) - 0.9d) should be < 0.01
    Math.abs(typicalScores(4) - 0.9d) should be < 0.01
    Math.abs(typicalScores(5) - 0.9d) should be < 0.01
    Math.abs(typicalScores(6) - 0.9d) should be < 0.01
    Math.abs(typicalScores(7) - 0.9d) should be < 0.01
    Math.abs(typicalScores(8) - 0.9d) should be < 0.01

  }

  "filterRecords" should "return data set without garbage" in {

    val cleanedFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterRecords(testFlowRecords.inputFlowRecordsDF)

    cleanedFlowRecords.count should be(5)
    cleanedFlowRecords.schema.size should be(17)
  }

  "filterInvalidRecords" should "return invalid records" in {

    val invalidFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterInvalidRecords(testFlowRecords.inputFlowRecordsDF)

    invalidFlowRecords.count should be(7)
    invalidFlowRecords.schema.size should be(17)
  }

  "filterScoredRecords" should "return records with score less or equal to threshold" in {

    val threshold = 10e-5

    val scoredFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterScoredRecords(testFlowRecords.scoredFlowRecordsDF, threshold)

    scoredFlowRecords.count should be(2)
  }

  def testFlowRecords = new {

    val inputFlowRecordsRDD = sparkSession.sparkContext.parallelize(wrapRefArray(Array(
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 24, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 60, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 60, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l),
      Seq(null, 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, null, "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0l,
        0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", null, 1024, 80, "TCP", 39l, 12522l,
        0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", null, 80, "TCP", 39l,
        12522l, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, null, "TCP",
        39l, 12522l, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", null,
        12522l, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        null, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, null, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, null),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l))
      .map(row => Row.fromSeq(row))))

    val inputFlowRecordsSchema = StructType(
      Array(TimeReceivedField,
        YearField,
        MonthField,
        DayField,
        HourField,
        MinuteField,
        SecondField,
        DurationField,
        SourceIPField,
        DestinationIPField,
        SourcePortField,
        DestinationPortField,
        ProtocolField,
        IpktField,
        IbytField,
        OpktField,
        ObytField))

    val inputFlowRecordsDF = sparkSession.createDataFrame(inputFlowRecordsRDD, inputFlowRecordsSchema)

    val scoredFlowRecordsRDD = sparkSession.sparkContext.parallelize(wrapRefArray(Array(
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l, -1d),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l, 1d),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l, 0.0000005d),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l, 0.05d),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972d, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l,
        12522l, 0l, 0l, 0.0001d))
      .map(row => Row.fromSeq(row))))

    val scoredFlowRecordsSchema = StructType(
      Array(TimeReceivedField,
        YearField,
        MonthField,
        DayField,
        HourField,
        MinuteField,
        SecondField,
        DurationField,
        SourceIPField,
        DestinationIPField,
        SourcePortField,
        DestinationPortField,
        ProtocolField,
        IpktField,
        IbytField,
        OpktField,
        ObytField,
        ScoreField))

    val scoredFlowRecordsDF = sparkSession.createDataFrame(scoredFlowRecordsRDD, scoredFlowRecordsSchema)
  }

}
