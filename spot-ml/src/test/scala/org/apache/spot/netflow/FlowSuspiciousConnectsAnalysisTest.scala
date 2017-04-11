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
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

case class FlowRecord(treceived: String,
                      tryear: Int,
                      trmonth: Int,
                      trday: Int,
                      trhour: Int,
                      trminute: Int,
                      trsec: Int,
                      tdur: Float,
                      sip: String,
                      dip: String,
                      sport: Int,
                      dport: Int,
                      proto: String,
                      ipkt: Int,
                      ibyt: Int,
                      opkt: Int,
                      obyt: Int)

class FlowSuspiciousConnectsAnalysisTest extends TestingSparkContextFlatSpec with Matchers {


  val testConfig = SuspiciousConnectsConfig(analysis = "flow",
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

  "netflow suspicious connects" should "correctly identify time-of-day anomalies" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.OFF)

    val anomalousRecord = FlowRecord("2016-05-05 00:11:01", 2016, 5, 5, 0, 0, 1, 0.972f, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39, 12522, 0, 0)
    val typicalRecord = FlowRecord("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972f, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39, 12522, 0, 0)


    val data = sqlContext.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord,
      typicalRecord, typicalRecord, typicalRecord, typicalRecord))


    val scoredData: DataFrame = FlowSuspiciousConnectsAnalysis.detectFlowAnomalies(data,
      testConfig,
      sparkContext,
      sqlContext,
      logger)



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
  "filterAndSelectCleanFlowRecords" should "return data set without garbage" in {

    val cleanedFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterAndSelectCleanFlowRecords(testFlowRecords.inputFlowRecordsDF)

    cleanedFlowRecords.count should be(5)
    cleanedFlowRecords.schema.size should be(17)
  }

  "filterAndSelectInvalidFlowRecords" should "return invalid records" in {

    val invalidFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterAndSelectInvalidFlowRecords(testFlowRecords.inputFlowRecordsDF)

    invalidFlowRecords.count should be(7)
    invalidFlowRecords.schema.size should be(17)
  }

  "filterScoredFlowRecords" should "return records with score less or equal to threshold" in {

    val threshold = 10e-5

    val scoredFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterScoredFlowRecords(testFlowRecords.scoredFlowRecordsDF, threshold)

    scoredFlowRecords.count should be(2)
  }

  "filterAndSelectCorruptFlowRecords" should "return records where Score is equal to -1" in {

    val corruptFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterAndSelectCorruptFlowRecords(testFlowRecords.scoredFlowRecordsDF)

    corruptFlowRecords.count should be(1)
    corruptFlowRecords.schema.size should be(18)
  }

  def testFlowRecords = new {
    val sqlContext = new SQLContext(sparkContext)

    val inputFlowRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 24, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 60, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 60, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq(null, 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, null, "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", null, 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", null, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, null, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", null, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, null, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, null, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, null),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0))
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

    val inputFlowRecordsDF = sqlContext.createDataFrame(inputFlowRecordsRDD, inputFlowRecordsSchema)

    val scoredFlowRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, -1d),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, 1d),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, 0.0000005),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, 0.05),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, 0.0001))
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

    val scoredFlowRecordsDF = sqlContext.createDataFrame(scoredFlowRecordsRDD, scoredFlowRecordsSchema)
  }
}
