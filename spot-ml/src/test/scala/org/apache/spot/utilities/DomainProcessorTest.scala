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

package org.apache.spot.utilities

import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities
import org.apache.spot.utilities.DomainProcessor._
import org.scalatest.Matchers


class DomainProcessorTest extends TestingSparkContextFlatSpec with Matchers {
  val countryCodesSet = utilities.CountryCodes.CountryCodes


  "extractDomain" should "return domain when provided a url with top-level domain and country code" in {
    val url = "fatosdesconhecidos.com.br"
    val result = DomainProcessor.extractDomain(url)
    result shouldEqual ("fatosdesconhecidos")
  }

  it should "return domain when provided a short url with no top-level domain but only a country code" in {
    val url = "panasonic.jp"
    val result = DomainProcessor.extractDomain(url)
    result shouldEqual ("panasonic")
  }

  it should "return domain when provided a long url with no top-level domain but only a country code" in {
    val url = "get.your.best.electronic.at.panasonic.jp"
    val result = DomainProcessor.extractDomain(url)
    result shouldEqual ("panasonic")
  }

  it should "return domain when provided a short url with a top-level domain no country code" in {
    val url = "forrealz.net"
    val result = DomainProcessor.extractDomain(url)
    result shouldEqual ("forrealz")
  }

  it should "return domain when provided a long url with a top-level domain no country code" in {
    val url = "wow.its.really.long.super.long.yeah.so.long.long.long.long.forrealz.net"
    val result = DomainProcessor.extractDomain(url)
    result shouldEqual ("forrealz")
  }

  it should "should return \"None\" when provided an address" in {
    val url = "123.103.104.10.in-addr.arpa"
    val result = DomainProcessor.extractDomain(url)
    result shouldEqual ("None")
  }

  it should "return \"None\" when provided a short url with a bad top-level domain / country code" in {
    val url = "panasonic.c"
    val result = DomainProcessor.extractDomain(url)
    result shouldEqual ("None")
  }

  "extractDomainInfo" should "handle an in-addr.arpa url" in {

    val url = "123.103.104.10.in-addr.arpa"

    val topDomains = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)

    val userDomain = "intel"

    // case class DerivedFields(topDomain: String, subdomainLength: Double, subdomainEntropy: Double, numPeriods: Double)
    val result = extractDomainInfo(url, topDomains, userDomain)

    result shouldBe DomainInfo(domain = "None", topDomain = 0, subdomain = "None", subdomainLength = 0, subdomainEntropy = 0, numPeriods = 6)
  }

  it should "handle an Alexa top 1M domain with a subdomain, top-level domain name and country code" in {

    val url = "services.amazon.com.mx"

    val topDomains = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)

    val userDomain = "intel"

    val result = extractDomainInfo(url, topDomains, userDomain)

    result shouldBe DomainInfo(domain = "amazon", topDomain = 1, subdomain = "services",
      subdomainLength = 8, subdomainEntropy = 2.5, numPeriods = 4)
  }

  it should "handle an Alexa top 1M domain with a top-level domain name and country code but no subdomain" in {

    val url = "amazon.com.mx"
    val countryCodes = sparkSession.sparkContext.broadcast(countryCodesSet)
    val topDomains = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)
    val userDomain = "intel"

    val result = extractDomainInfo(url, topDomains, userDomain)

    result shouldBe DomainInfo(domain = "amazon", subdomain = "None", topDomain = 1, subdomainLength = 0, subdomainEntropy = 0, numPeriods = 3)
  }

  it should "handle an Alexa top 1M domain with a subdomain and top-level domain name but no country code" in {

    val url = "services.amazon.com"
    val countryCodes = sparkSession.sparkContext.broadcast(countryCodesSet)
    val topDomains = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)
    val userDomain = "intel"

    val result = extractDomainInfo(url, topDomains, userDomain)

    result shouldBe DomainInfo(domain = "amazon", subdomain = "services", topDomain = 1, subdomainLength = 8, subdomainEntropy = 2.5, numPeriods = 3)
  }


  it should "handle an Alexa top 1M domain with no subdomain or country code" in {

    val url = "amazon.com"
    val countryCodes = sparkSession.sparkContext.broadcast(countryCodesSet)
    val topDomains = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)
    val userDomain = "intel"

    val result = extractDomainInfo(url, topDomains, userDomain)

    result shouldBe DomainInfo(domain = "amazon", subdomain = "None", topDomain = 1, subdomainLength = 0, subdomainEntropy = 0, numPeriods = 2)
  }
  it should "not identify the domain as the users domain when both are empty strings" in {
    val url = "ab..com"
    val countryCodes = sparkSession.sparkContext.broadcast(countryCodesSet)
    val topDomains = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)
    val userDomain = ""

    val result = extractDomainInfo(url, topDomains, userDomain)

    result shouldBe DomainInfo(domain = "", subdomain = "ab", topDomain = 0, subdomainLength = 2, subdomainEntropy = 1, numPeriods = 3)
  }
}
