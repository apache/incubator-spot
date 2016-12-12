package org.apache.spot.dns

import org.apache.log4j.{Level, LogManager}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

case class DNSInput(frame_time:String, unix_tstamp:Long, frame_len:Int, ip_dst: String, dns_qry_name:String, dns_qry_class:String, dns_qry_type: Int, dns_qry_rcode: Int)

class DNSSuspiciousConnectsAnalysisTest  extends TestingSparkContextFlatSpec with Matchers {

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
    logger.setLevel(Level.INFO)
    val testSqlContext = new org.apache.spark.sql.SQLContext(sparkContext)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT",	1463735425L,	1,	"172.16.9.132",	"turner.com.122.2o7.net",	"0x00000001",	1,	0)
    val typicalRecord   = DNSInput("May 20 2016 02:10:25.970987000 PDT",	1463735425L,	168,	"172.16.9.132",	"turner.com.122.2o7.net",	"0x00000001",	1,	0)

    import testSqlContext.implicits._

    val data = sparkContext.parallelize(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord)).toDF

    val scoredData = DNSSuspiciousConnectsAnalysis.detectDNSAnomalies(data, testConfig,
      sparkContext,
      sqlContext,
      logger)


    val anomalyScore = scoredData.filter(scoredData(FrameLength) === 1).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(FrameLength) === 168).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.2d)  should be <= 0.01d
    typicalScores.length shouldBe 4
    Math.abs(typicalScores(0) - 0.8d)  should be <= 0.01d
    Math.abs(typicalScores(1) - 0.8d)  should be <= 0.01d
    Math.abs(typicalScores(2) - 0.8d)  should be <= 0.01d
    Math.abs(typicalScores(3) - 0.8d)  should be <= 0.01d
  }


}
