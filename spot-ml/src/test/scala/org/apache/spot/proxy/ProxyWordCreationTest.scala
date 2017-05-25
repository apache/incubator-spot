package org.apache.spot.proxy


import org.apache.log4j.{Level, LogManager}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.proxy.ProxySchema.Word
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers


class ProxyWordCreationTest extends TestingSparkContextFlatSpec with Matchers {

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

  "proxy word creation" should "return the correct word given the rules to form the word" in {

    val logger = LogManager.getLogger("ProxyWordCreation")
    logger.setLevel(Level.WARN)

    // case class ProxyInput has the form:
    // 1  p_date:String,
    // 2  p_time:String,      <- currently used for feature creation
    // 3  clientip:String,
    // 4  host:String,        <- currently used for feature creation
    // 5  reqmethod:String,   <- currently used for feature creation
    // 6  useragent:String,   <- currently used for feature creation
    // 7  resconttype:String, <- currently used for feature creation
    // 8  duration:Int,
    // 9  username:String,
    // 10 webcat:String,
    // 11 referer:String,
    // 12 respcode:String,    <- currently used for feature creation
    // 13 uriport:Int,
    // 14 uripath:String,
    // 15 uriquery:String,
    // 16 serverip:String,
    // 17 scbytes:Int,
    // 18 csbytes:Int,
    // 19 fulluri:String)     <- currently used for feature creation

    val record1 = ProxyInput(
      "2016-10-03",
      "00:57:36",
      "127.0.0.1",
      "intel.com",
      "PUT",
      "Mozilla/5.0",
      "text/plain",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "202",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "ab") //entropy = 1.0

    val record2 = ProxyInput(
      "2016-10-03",
      "01:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "PUT",
      "Safari/537.36",
      "image",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "202",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "abc") //entropy = 1.5849625007211559

    val record3 = ProxyInput(
      "2016-10-03",
      "02:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "PUT",
      "Safari/537.36",
      "image",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "304",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "abcd") //entropy = 2.0

    val record4 = ProxyInput(
      "2016-10-03",
      "03:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "PUT",
      "Safari/537.36",
      "binary",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "304",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "abcde") // entropy = 2.321928094887362

    val record5 = ProxyInput(
      "2016-10-03",
      "10:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "PUT",
      "Safari/537.36",
      "binary",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "206",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "abcdef") //entropy = 2.584962500721155

    val record6 = ProxyInput(
      "2016-10-03",
      "11:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "GET",
      "Safari/537.36",
      "binary",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "206",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "abcdefghijklmnopqrstuvwxyz") //4.70043971814109

    val record7 = ProxyInput(
      "2016-10-03",
      "13:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "GET",
      "Safari/537.36",
      "text/plain",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "200",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "aaa") //entropy = 0.0

    val record8 = ProxyInput(
      "2016-10-03",
      "14:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "GET",
      "Safari/537.36",
      "text/plain",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "200",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "aaabbb") //entropy 1.0

    val record9 = ProxyInput(
      "2016-10-03",
      "22:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "GET",
      "Safari/537.36",
      "text/plain",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "302",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "aaaaaaaaaaabbbbbbbbbbbccccccccccc") //entropy = 1.5849625007211559

    val record10 = ProxyInput(
      "2016-10-03",
      "23:57:36",
      "127.0.0.1",
      "maw.bronto.com",
      "GET",
      "Safari/537.36",
      "text/plain",
      230,
      "-",
      "Technology/Internet",
      "http://www.spoonflower.com/tags/color",
      "302",
      80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-",
      "127.0.0.1",
      338,
      647,
      "maw.bronto.com/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd" +
        "-4e804fd354ef/fiddle") // entropy = 4.832854333602748


    val data = sqlContext.createDataFrame(Seq(record1, record2, record3, record4, record5, record6, record7, record8,
      record9, record10))

    val scoredData = ProxySuspiciousConnectsAnalysis.detectProxyAnomalies(data, testConfigProxy,
      sparkContext,
      sqlContext,
      logger)

    val words = scoredData.collect().map(_.getAs[String](Word))

    words(0) shouldBe "2_0_PUT_4_text_0_1_202"
    words(1) shouldBe "1_1_PUT_6_image_3_1_202"
    words(2) shouldBe "1_2_PUT_7_image_3_2_304"
    words(3) shouldBe "1_3_PUT_8_binary_3_2_304"
    words(4) shouldBe "1_10_PUT_9_binary_3_2_206"
    words(5) shouldBe "1_11_GET_16_binary_3_4_206"
    words(6) shouldBe "1_13_GET_0_text_3_1_200"
    words(7) shouldBe "1_14_GET_4_text_3_2_200"
    words(8) shouldBe "1_22_GET_6_text_3_5_302"
    words(9) shouldBe "1_23_GET_17_text_3_6_302"

  }
}
