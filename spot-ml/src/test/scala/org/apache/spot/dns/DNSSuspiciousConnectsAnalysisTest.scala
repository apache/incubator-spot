package org.apache.spot.dns


import org.apache.log4j.{Level, LogManager}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.Row
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
    logger.setLevel(Level.WARN)

    val anomalousRecord = DNSInput("May 20 2016 02:10:25.970987000 PDT",	1463735425L,	1,	"172.16.9.132",	"turner.com.122.2o7.net",	"0x00000001",	1,	0)
    val typicalRecord   = DNSInput("May 20 2016 02:10:25.970987000 PDT",	1463735425L,	168,	"172.16.9.132",	"turner.com.122.2o7.net",	"0x00000001",	1,	0)

    val data = sqlContext.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))

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


  "filterAndSelectCleanDNSRecords" should "return data set without garbage" in {

    val cleanedDNSRecords = DNSSuspiciousConnectsAnalysis.filterAndSelectCleanDNSRecords(testDNSRecords.inputDNSRecordsDF)

    cleanedDNSRecords.count should be(8)
    cleanedDNSRecords.schema.size should be(8)
  }

  "filterAndSelectInvalidDNSRecords" should "return invalid records" in {

    val invalidDNSRecords = DNSSuspiciousConnectsAnalysis.filterAndSelectInvalidDNSRecords(testDNSRecords.inputDNSRecordsDF)

    invalidDNSRecords.count should be(15)
    invalidDNSRecords.schema.size should be(8)
  }

  "filterScoredDNSRecords" should "return records with score less or equal to threshold" in {

    val threshold = 10e-5
    val scoredDNSRecords = DNSSuspiciousConnectsAnalysis
      .filterScoredDNSRecords(testDNSRecords.scoredDNSRecordsDF, threshold)

    scoredDNSRecords.count should be(2)
  }

  "filterAndSelectCorruptDNSRecords" should "return records where Score is equal to -1" in {

    val corruptDNSRecords = DNSSuspiciousConnectsAnalysis
      .filterAndSelectCorruptDNSRecords(testDNSRecords.scoredDNSRecordsDF)

    corruptDNSRecords.count should be(1)
    corruptDNSRecords.schema.size should be(9)
  }

  def testDNSRecords = new {

    val sqlContext = new SQLContext(sparkContext)

    val inputDNSRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
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
      Array(TimestampField,
        UnixTimestampField,
        FrameLengthField,
        ClientIPField,
        QueryNameField,
        QueryClassField,
        QueryTypeField,
        QueryResponseCodeField))

    val inputDNSRecordsDF = sqlContext.createDataFrame(inputDNSRecordsRDD, inputDNSRecordsSchema)

    val scoredDNSRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, 1d),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, 0.0000005),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, 0.05),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, -1d),
      Seq("May 20 2016 02:10:25.970987000 PDT", 1463735425l, 168, "172.16.9.132", "turner.com.122.2o...", "0x00000001", 1, 0, 0.0001))
      .map(row => Row.fromSeq(row))))

    val scoredDNSRecordsSchema = StructType(
      Array(TimestampField,
        UnixTimestampField,
        FrameLengthField,
        ClientIPField,
        QueryNameField,
        QueryClassField,
        QueryTypeField,
        QueryResponseCodeField,
        ScoreField))

    val scoredDNSRecordsDF = sqlContext.createDataFrame(scoredDNSRecordsRDD, scoredDNSRecordsSchema)

  }
}
