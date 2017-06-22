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

package org.apache.spot.dns


import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spot.SuspiciousConnects.SuspiciousConnectsAnalysisResults
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSchema._
import org.apache.spot.dns.model.DNSSuspiciousConnectsModel
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities.FloatPointPrecisionUtility32
import org.scalatest.Matchers


case class DNSInput(frame_time: String,
                    unix_tstamp: Long,
                    frame_len: Int,
                    ip_dst: String,
                    dns_qry_name: String,
                    dns_qry_class: String,
                    dns_qry_type: Int,
                    dns_qry_rcode: Int)

class DNSSuspiciousConnectsAnalysisTest extends TestingSparkContextFlatSpec with Matchers {

  val emTestConfig = SuspiciousConnectsConfig(analysis = "dns",
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
    ldaOptimizer = "em"
  )

  val onlineTestConfig = SuspiciousConnectsConfig(analysis = "dns",
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
    ldaOptimizer = "online"
  )

  val testConfigFloatConversion = SuspiciousConnectsConfig(analysis = "dns",
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


  "dns suspicious connects analysis" should "estimate correct probabilities in toy data with framelength anomaly using" +
    " EMLDAOptimizer" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT", 1463735425L, 1, "172.16.9.132", "122.2o7.turner.com", "0x00000001", 1, 0)
    val typicalRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT", 1463735425L, 168, "172.16.9.132", "122.2o7.turner.com", "0x00000001", 1, 0)
    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))

    val SuspiciousConnectsAnalysisResults(scoredData, _) =
      DNSSuspiciousConnectsAnalysis.run(emTestConfig, sparkSession, logger, data)

    val anomalyScore = scoredData.filter(scoredData(FrameLength) === 1).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(FrameLength) === 168).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.2d) should be <= 0.01d
    typicalScores.length shouldBe 4
    Math.abs(typicalScores(0) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(1) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(2) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(3) - 0.8d) should be <= 0.01d
  }

  it should "estimate correct probabilities in toy data with framelength anomaly using" +
    " OnlineLDAOptimizer" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT", 1463735425L, 1, "172.16.9.132", "122.2o7.turner.com", "0x00000001", 1, 0)
    val typicalRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT", 1463735425L, 168, "172.16.9.132", "122.2o7.turner.com", "0x00000001", 1, 0)
    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))

    val SuspiciousConnectsAnalysisResults(scoredData, _) = DNSSuspiciousConnectsAnalysis.run(onlineTestConfig,
      sparkSession, logger, data)

    val anomalyScore = scoredData.filter(scoredData(FrameLength) === 1).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(FrameLength) === 168).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.2d) should be <= 0.01d
    typicalScores.length shouldBe 4
    Math.abs(typicalScores(0) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(1) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(2) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(3) - 0.8d) should be <= 0.01d
  }

  it should "estimate correct probabilities in toy data with subdomain length anomaly " +
    "using EMLDAOptimizer" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT",
      1463735425L,
      168,
      "172.16.9.132",
      "1111111111111111111111111111111111111111111111111111111111111.tinker.turner.com",
      "0x00000001",
      1,
      0)
    val typicalRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT",
      1463735425L,
      168,
      "172.16.9.132",
      "tinker.turner.com",
      "0x00000001",
      1,
      0)
    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))
    val model = DNSSuspiciousConnectsModel.trainModel(sparkSession, logger, emTestConfig, data)
    val scoredData = model.score(sparkSession, data, emTestConfig.userDomain, emTestConfig.precisionUtility)
    val anomalyScore = scoredData.
      filter(scoredData(QueryName) === "1111111111111111111111111111111111111111111111111111111111111.tinker.turner.com").
      first().
      getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(QueryName) === "tinker.turner.com").collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.2d) should be <= 0.01d
    typicalScores.length shouldBe 4
    Math.abs(typicalScores(0) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(1) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(2) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(3) - 0.8d) should be <= 0.01d
  }

  it should "estimate correct probabilities in toy data with subdomain length anomaly " +
    "using OnlineLDAOptimizer" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT",
      1463735425L,
      168,
      "172.16.9.132",
      "1111111111111111111111111111111111111111111111111111111111111.tinker.turner.com",
      "0x00000001",
      1,
      0)
    val typicalRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT",
      1463735425L,
      168,
      "172.16.9.132",
      "tinker.turner.com",
      "0x00000001",
      1,
      0)

    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))
    val model = DNSSuspiciousConnectsModel.trainModel(sparkSession, logger, onlineTestConfig, data)
    val scoredData = model.score(sparkSession, data, onlineTestConfig.userDomain, onlineTestConfig.precisionUtility)

    val anomalyScore = scoredData.
      filter(scoredData(QueryName) === "1111111111111111111111111111111111111111111111111111111111111.tinker.turner.com").
      first().
      getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(QueryName) === "tinker.turner.com").collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.2d) should be <= 0.01d
    typicalScores.length shouldBe 4
    Math.abs(typicalScores(0) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(1) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(2) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(3) - 0.8d) should be <= 0.01d
  }

  it should "estimate correct probabilities in toy data with framelength anomaly converting probabilities to Float " +
    "for transportation and converting back to Double" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT", 1463735425L, 1, "172.16.9.132", "122.2o7.turner.com", "0x00000001", 1, 0)
    val typicalRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT", 1463735425L, 168, "172.16.9.132", "122.2o7.turner.com", "0x00000001", 1, 0)
    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))
    val model = DNSSuspiciousConnectsModel.trainModel(sparkSession, logger, testConfigFloatConversion, data)
    val scoredData = model.score(sparkSession, data, testConfigFloatConversion.userDomain,
      testConfigFloatConversion.precisionUtility)

    val anomalyScore = scoredData.filter(scoredData(FrameLength) === 1).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(FrameLength) === 168).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.2d) should be <= 0.01d
    typicalScores.length shouldBe 4
    Math.abs(typicalScores(0) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(1) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(2) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(3) - 0.8d) should be <= 0.01d
  }


  it should "estimate correct probabilities in toy data with subdomain length anomaly converting probabilities to " +
    "Float for transportation and converting back to Double" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT",
      1463735425L,
      168,
      "172.16.9.132",
      "1111111111111111111111111111111111111111111111111111111111111.tinker.turner.com",
      "0x00000001",
      1,
      0)
    val typicalRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT",
      1463735425L,
      168,
      "172.16.9.132",
      "tinker.turner.com",
      "0x00000001",
      1,
      0)
    val data = sparkSession.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))

    val model = DNSSuspiciousConnectsModel.trainModel(sparkSession, logger, testConfigFloatConversion, data)
    val scoredData = model.score(sparkSession, data, testConfigFloatConversion.userDomain,
      testConfigFloatConversion.precisionUtility)

    val anomalyScore = scoredData.
      filter(scoredData(QueryName) === "1111111111111111111111111111111111111111111111111111111111111.tinker.turner.com").
      first().
      getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(QueryName) === "tinker.turner.com").collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.2d) should be <= 0.01d
    typicalScores.length shouldBe 4
    Math.abs(typicalScores(0) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(1) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(2) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(3) - 0.8d) should be <= 0.01d
  }

  "filterRecords" should "return data set without garbage" in {

    val cleanedDNSRecords = DNSSuspiciousConnectsAnalysis.filterRecords(testDNSRecords.inputDNSRecordsDF)

    cleanedDNSRecords.count should be(8)
    cleanedDNSRecords.schema.size should be(8)
  }

  "filterInvalidRecords" should "return invalid records" in {

    val invalidDNSRecords = DNSSuspiciousConnectsAnalysis.filterInvalidRecords(testDNSRecords.inputDNSRecordsDF)

    invalidDNSRecords.count should be(15)
    invalidDNSRecords.schema.size should be(8)
  }

  "filterScoredRecords" should "return records with score less or equal to threshold" in {

    val threshold = 10e-5
    val scoredDNSRecords = DNSSuspiciousConnectsAnalysis
      .filterScoredRecords(testDNSRecords.scoredDNSRecordsDF, threshold)

    scoredDNSRecords.count should be(2)
  }

  def testDNSRecords = new {


    val inputDNSRecordsRDD = sparkSession.sparkContext.parallelize(wrapRefArray(Array(
      Seq(null, 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0),
      Seq("", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0),
      Seq("-", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", null, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, null, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", null, "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "-", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "(empty)", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, null, "turner.com.122.2o...", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "", "turner.com.122.2o...", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "-", "turner.com.122.2o...", "0x00000001", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", null, null, null),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "", null, null),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "-", null, null),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", null, 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "-", 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", null, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, null),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", null, 1, null),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", null, 1, 0),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", null, null))
      .map(row => Row.fromSeq(row))))

    val inputDNSRecordsSchema = StructType(
      Array(TimeStampField,
        UnixTimeStampField,
        FrameLengthField,
        ClientIPField,
        QueryNameField,
        QueryClassField,
        QueryTypeField,
        QueryResponseCodeField))

    val inputDNSRecordsDF = sparkSession.createDataFrame(inputDNSRecordsRDD, inputDNSRecordsSchema)

    val scoredDNSRecordsRDD = sparkSession.sparkContext.parallelize(wrapRefArray(Array(
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, 1d),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, 0.0000005),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, 0.05),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, -1d),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, 0.0001))
      .map(row => Row.fromSeq(row))))

    val scoredDNSRecordsSchema = StructType(
      Array(TimeStampField,
        UnixTimeStampField,
        FrameLengthField,
        ClientIPField,
        QueryNameField,
        QueryClassField,
        QueryTypeField,
        QueryResponseCodeField,
        ScoreField))

    val scoredDNSRecordsDF = sparkSession.createDataFrame(scoredDNSRecordsRDD, scoredDNSRecordsSchema)

  }
}
