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

import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities.Entropy
import org.scalatest.Matchers

class DNSWordCreationTest extends TestingSparkContextFlatSpec with Matchers {

    "entropy" should "return 2.807354922057603 with value abcdefg" in {
    val value = "abcdefg"

    val result = Entropy.stringEntropy(value)

    result shouldBe 2.807354922057604
  }

  "dnsWord" should "return correct word with a single example input" in {

    val topDomainsBC = sparkSession.sparkContext.broadcast(Set("google", "youtube"))
    val userDomain = "apache"

    val timeStamp = "May  12 2015 11:32:45.543421000 UTC"
    val unixTimeStamp = 1494326461L
    val frameLength = 213
    val clientIP = "122.52.206.191"
    val queryName = "abcdefg.apache.com"
    val queryClass = "0x00000001"
    val dnsQueryType = 1
    val dnsQueryRcode = 0

    val DWC = new DNSWordCreation(topDomainsBC, userDomain)

    val result = DWC.dnsWord(timeStamp,
      unixTimeStamp,
      frameLength,
      clientIP,
      queryName,
      queryClass,
      dnsQueryType,
      dnsQueryRcode)

    result shouldBe "2_7_11_2_10_1_1_0"

  }
}
