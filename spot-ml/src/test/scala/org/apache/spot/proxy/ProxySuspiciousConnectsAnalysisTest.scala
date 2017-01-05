package org.apache.spot.proxy

import org.apache.log4j.{Level, LogManager}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types.StructType
import org.apache.spot.proxy.ProxySchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

case class ProxyInput(p_date:String,
                      p_time:String,
                      clientip:String,
                      host:String,
                      reqmethod:String,
                      useragent:String,
                      resconttype:String,
                      duration:Int,
                      username:String,
                      webcat:String,
                      referer:String,
                      respcode:String,
                      uriport:Int,
                      uripath:String,
                      uriquery:String,
                      serverip:String,
                      scbytes:Int,
                      csbytes:Int,
                      fulluri:String)

class ProxySuspiciousConnectsAnalysisTest extends TestingSparkContextFlatSpec with Matchers {



  val testConfigProxy = SuspiciousConnectsConfig(analysis = "proxy",
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


  "proxy supicious connects analysis" should "estimate correct probabilities in toy data with top domain anomaly" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val anomalousRecord = ProxyInput("2016-10-03",	"04:57:36", "127.0.0.1",	"intel.com",	"PUT",
      "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36",
      "text/plain", 230,	"-", 	"Technology/Internet",	"http://www.spoonflower.com/tags/color",	"202",	80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",	"127.0.0.1",	338,	647,
      "maw.bronto.com/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle")

    val typicalRecord   = ProxyInput("2016-10-03",	"04:57:36", "127.0.0.1",	"maw.bronto.com",	"PUT",
      "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36",
      "text/plain", 230,	"-", 	"Technology/Internet",	"http://www.spoonflower.com/tags/color",	"202",	80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",	"127.0.0.1",	338,	647,
      "maw.bronto.com/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle")


    val data = sqlContext.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord,
      typicalRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord))

    val scoredData = ProxySuspiciousConnectsAnalysis.detectProxyAnomalies(data, testConfigProxy,
      sparkContext,
      sqlContext,
      logger)



    val anomalyScore = scoredData.filter(scoredData(Host) ===  "intel.com").first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(Host) === "maw.bronto.com").collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.1d)  should be <= 0.01d
    typicalScores.length shouldBe 9
    Math.abs(typicalScores(0) - 0.9d)  should be <= 0.01d
    Math.abs(typicalScores(1) - 0.9d)  should be <= 0.01d
    Math.abs(typicalScores(2) - 0.9d)  should be <= 0.01d
    Math.abs(typicalScores(3) - 0.9d)  should be <= 0.01d
    Math.abs(typicalScores(4) - 0.9d)  should be <= 0.01d
    Math.abs(typicalScores(5) - 0.9d)  should be <= 0.01d
    Math.abs(typicalScores(6) - 0.9d)  should be <= 0.01d
    Math.abs(typicalScores(7) - 0.9d)  should be <= 0.01d
    Math.abs(typicalScores(8) - 0.9d)  should be <= 0.01d
  }


  "filterAndSelectCleanProxyRecords" should "return data without garbage" in {

    val cleanedProxyRecords = ProxySuspiciousConnectsAnalysis
      .filterAndSelectCleanProxyRecords(testProxyRecords.inputProxyRecordsDF)

    cleanedProxyRecords.count should be(1)
    cleanedProxyRecords.schema.size should be(19)
  }

  "filterAndSelectInvalidProxyRecords" should "return invalir records" in {

    val invalidProxyRecords = ProxySuspiciousConnectsAnalysis
      .filterAndSelectInvalidProxyRecords(testProxyRecords.inputProxyRecordsDF)

    invalidProxyRecords.count should be(5)
    invalidProxyRecords.schema.size should be(19)

  }

  "filterScoredProxyRecords" should "return records with score less or equal to threshold" in {

    val threshold = 10e-5

    val scoredProxyRecords = ProxySuspiciousConnectsAnalysis
      .filterScoredProxyRecords(testProxyRecords.scoredProxyRecordsDF, threshold)

    scoredProxyRecords.count should be(2)

  }

  "filterAndSelectCorruptProxyRecords" should "return records where Score is equal to -1" in {

    val corruptProxyRecords = ProxySuspiciousConnectsAnalysis
      .filterAndSelectCorruptProxyRecords(testProxyRecords.scoredProxyRecordsDF)

    corruptProxyRecords.count should be(1)
    corruptProxyRecords.schema.size should be(21)
  }

  def testProxyRecords = new {

    val sqlContext = new SQLContext(sparkContext)

    val inputProxyRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
      Seq(null,"00:09:13","10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu..."),
      Seq("2016-10-03",null,"10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu..."),
      Seq("2016-10-03","00:09:13",null,"cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu..."),
      Seq("2016-10-03","00:09:13","10.239.160.152",null,"GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu..."),
      Seq("2016-10-03","00:09:13","10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,null),
      Seq("2016-10-03","00:09:13","10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu..."))
      .map(row => Row.fromSeq(row))))

    val inputProxyRecordsSchema = StructType(
      Array(DateField,
        TimeField,
        ClientIPField,
        HostField,
        ReqMethodField,
        UserAgentField,
        ResponseContentTypeField,
        DurationField,
        UserNameField,
        WebCatField,
        RefererField,
        RespCodeField,
        URIPortField,
        URIPathField,
        URIQueryField,
        ServerIPField,
        SCBytesField,
        CSBytesField,
        FullURIField))

    val inputProxyRecordsDF = sqlContext.createDataFrame(inputProxyRecordsRDD, inputProxyRecordsSchema)

    val scoredProxyRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
      Seq("2016-10-03","00:09:13","10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu...", "a word", -1d),
      Seq("2016-10-03","00:09:13","10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu...", "a word", 1d),
      Seq("2016-10-03","00:09:13","10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu...", "a word", 0.0000005),
      Seq("2016-10-03","00:09:13","10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu...", "a word", 0.05),
      Seq("2016-10-03","00:09:13","10.239.160.152","cn.archive.ubuntu...","GET","Debian APT-HTTP/...","text/html",448,"-",
        "-","-","404","80","/ubuntu/dists/tru...","-","10.239.4.160",2864,218,"cn.archive.ubuntu...", "a word", 0.0001)
    ).map(row => Row.fromSeq(row))))

    val scoredProxyRecordsSchema = StructType(
      Array(DateField,
        TimeField,
        ClientIPField,
        HostField,
        ReqMethodField,
        UserAgentField,
        ResponseContentTypeField,
        DurationField,
        UserNameField,
        WebCatField,
        RefererField,
        RespCodeField,
        URIPortField,
        URIPathField,
        URIQueryField,
        ServerIPField,
        SCBytesField,
        CSBytesField,
        FullURIField,
        WordField,
        ScoreField))

    val scoredProxyRecordsDF = sqlContext.createDataFrame(scoredProxyRecordsRDD, scoredProxyRecordsSchema)

  }

}
