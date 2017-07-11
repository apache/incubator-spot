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

import org.apache.spot.dns.model.DNSSuspiciousConnectsModel
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities.{CountryCodes, TopDomains}
import org.scalatest.Matchers

class DNSSuspiciousConnectsModelTest extends TestingSparkContextFlatSpec with Matchers {

  "createTempFields" should "create DNS stats based on an URL" in {
    val countryCodesBC = sparkSession.sparkContext.broadcast(CountryCodes.CountryCodes)
    val topDomainsBC = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)
    val userDomain = "intel.com"
    val url = "iot.intel.com.mx"


    val result = DNSSuspiciousConnectsModel.createTempFields(countryCodesBC, topDomainsBC, userDomain, url)

    result.subdomainEntropy shouldBe 1.5849625007211559
    result.subdomainLength shouldBe 3
    result.topDomainClass shouldBe 1
    result.numPeriods shouldBe 4

  }

  it should "create DNS stats based on an URL completely different to userDomain" in {
    val countryCodesBC = sparkSession.sparkContext.broadcast(CountryCodes.CountryCodes)
    val topDomainsBC = sparkSession.sparkContext.broadcast(TopDomains.TopDomains)
    val userDomain = "anothercompany.com"
    val url = "maps.company.com"

    val result = DNSSuspiciousConnectsModel.createTempFields(countryCodesBC, topDomainsBC, userDomain, url)

    result.subdomainEntropy shouldBe 2.0
    result.subdomainLength shouldBe 4
    result.topDomainClass shouldBe 1
    result.numPeriods shouldBe 3

  }
}
