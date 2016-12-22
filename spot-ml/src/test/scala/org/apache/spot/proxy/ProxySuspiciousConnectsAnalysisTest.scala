package org.apache.spot.proxy

import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types.StructType
import org.apache.spot.proxy.ProxySchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

/**
  * Created by rabarona on 12/15/16.
  */
class ProxySuspiciousConnectsAnalysisTest extends TestingSparkContextFlatSpec with Matchers{

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
