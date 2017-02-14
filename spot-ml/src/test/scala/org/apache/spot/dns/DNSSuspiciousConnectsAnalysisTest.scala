package org.apache.spot.dns


import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types.StructType
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

import org.apache.spot.dns.model.DNSSuspiciousConnectsModel


case class DNSInput(frame_time: String, unix_tstamp: Long, frame_len: Int, ip_dst: String, dns_qry_name: String, dns_qry_class: String, dns_qry_type: Int, dns_qry_rcode: Int)

class DNSSuspiciousConnectsAnalysisTest extends TestingSparkContextFlatSpec with Matchers {

  val testConfig = SuspiciousConnectsConfig(analysis = "dns",
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


  "dns supicious connects analysis" should "estimate correct probabilities in toy data with framelength anomaly" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT", 1463735425,168, "172.16.9.132", "turner.com.122.1.2.3.4.5.6.7.8.8.9.10.1.1.1.1.1.1.1.2o7.net", "0x00000001", 1 , 0)
    val typicalRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT", 1463735425, 168, "172.16.9.132", "turner.com.122.2o7.net","0x00000001",1,0)



    val data = sqlContext.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))


    val model = DNSSuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, testConfig, data, testConfig.topicCount)

    logger.info("Scoring")
    val scoredData = model.score(sparkContext, sqlContext, data)



    val anomalyScore = scoredData.filter(scoredData(QueryName) === "turner.com.122.1.2.3.4.5.6.7.8.8.9.10.1.1.1.1.1.1.1.2o7.net").first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(QueryName) === "turner.com.122.2o7.net").collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.2d) should be <= 0.01d
    typicalScores.length shouldBe 4
    Math.abs(typicalScores(0) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(1) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(2) - 0.8d) should be <= 0.01d
    Math.abs(typicalScores(3) - 0.8d) should be <= 0.01d
  }
}