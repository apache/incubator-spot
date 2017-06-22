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

  "proxy word creation" should "return the correct word given the set of rules to form the word" in {

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

    val noAlexaPutLoEntroTextRareAgentShortUri202 = ProxyInput("2016-10-03", "00:57:36", "127.0.0.1", "intel.com", "PUT",
      "Mozilla/5.0", "text/plain", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "202", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "ab")

    val AlexaPutMidEntroImagePopularAgentShortUri202 = ProxyInput("2016-10-03", "01:57:36", "127.0.0.1", "maw.bronto.com",
      "PUT", "Safari/537.36", "image", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "202", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "abc")

    val AlexaPutMidEntroImagePopularAgentShortUri304 = ProxyInput("2016-10-03", "02:57:36", "127.0.0.1", "maw.bronto.com",
      "PUT", "Safari/537.36", "image", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "304", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "abcd")

    val AlexaPutMidEntroBinaryPopularAgentShortUri304 = ProxyInput("2016-10-03", "03:57:36", "127.0.0.1", "maw.bronto.com",
      "PUT", "Safari/537.36", "binary", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "304", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "abcde")

    val AlexaPutMidEntroBinaryPopularAgentShortUri206 = ProxyInput("2016-10-03", "10:57:36", "127.0.0.1", "maw.bronto.com",
      "PUT", "Safari/537.36", "binary", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "206", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "abcdef")

    val AlexaGetHiEntroBinaryPopularAgentShortUri206 = ProxyInput("2016-10-03", "11:57:36", "127.0.0.1", "maw.bronto.com",
      "GET", "Safari/537.36", "binary", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "206", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "abcdefghijklmnopqrstuvwxyz")

    val AlexaGetZeroEntroTextPopularAgentShortUri200 = ProxyInput("2016-10-03", "13:57:36", "127.0.0.1", "maw.bronto.com",
      "GET", "Safari/537.36", "text/plain", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "200", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "aaa")

    val AlexaGetLoEntroTextPopularAgentShortUri200 = ProxyInput("2016-10-03", "14:57:36", "127.0.0.1", "maw.bronto.com",
      "GET", "Safari/537.36", "text/plain", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "200", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "aaabbb")

    val AlexaGetMidEntroTextPopularAgentMidUri302 = ProxyInput("2016-10-03", "22:57:36", "127.0.0.1", "maw.bronto.com",
      "GET", "Safari/537.36", "text/plain", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "302", 80,
      "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "aaaaaaaaaaabbbbbbbbbbbccccccccccc")

    val AlexaGetHiEntroTextPopularAgentLargeUri302 = ProxyInput("2016-10-03", "23:57:36", "127.0.0.1", "maw.bronto.com",
      "GET", "Safari/537.36", "text/plain", 230, "-", "Technology/Internet", "http://www.spoonflower.com/tags/color", "302",
      80, "/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle",
      "-", "127.0.0.1", 338, 647, "maw.bronto.com/sites/c37i4q22szvir8ga3m8mtxaft7gwnm5fio8hfxo35mu81absi1/carts" +
        "/4b3a313d-50f6-4117-8ffd-4e804fd354ef/fiddle")

    val data = sparkSession.createDataFrame(Seq(noAlexaPutLoEntroTextRareAgentShortUri202,
      AlexaPutMidEntroImagePopularAgentShortUri202,
      AlexaPutMidEntroImagePopularAgentShortUri304,
      AlexaPutMidEntroBinaryPopularAgentShortUri304,
      AlexaPutMidEntroBinaryPopularAgentShortUri206,
      AlexaGetHiEntroBinaryPopularAgentShortUri206,
      AlexaGetZeroEntroTextPopularAgentShortUri200,
      AlexaGetLoEntroTextPopularAgentShortUri200,
      AlexaGetMidEntroTextPopularAgentMidUri302,
      AlexaGetHiEntroTextPopularAgentLargeUri302))

    val model = ProxySuspiciousConnectsModel.trainModel(sparkSession, logger, testConfigProxy, data)

    val scoredData = model.score(sparkSession, data, testConfigProxy.precisionUtility)

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
