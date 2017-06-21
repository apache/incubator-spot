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
