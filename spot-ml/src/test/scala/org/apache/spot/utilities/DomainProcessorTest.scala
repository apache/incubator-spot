package org.apache.spot.utilities

import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.{FunSuite, Matchers}
import org.apache.spot.utilities.DomainProcessor._


class DomainProcessorTest extends TestingSparkContextFlatSpec with Matchers {
  val countryCodesSet = CountryCodes.CountryCodes


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

    val topDomains = sparkContext.broadcast(TopDomains.TopDomains)

    // case class DerivedFields(topDomain: String, subdomainLength: Double, subdomainEntropy: Double, numPeriods: Double)
    val result = extractDomainInfo(url, topDomains)

    result shouldBe DomainInfo(domain = "None", topDomain = 0, subdomain = "None", subdomainLength = 0, subdomainEntropy = 0, numPeriods = 6)
  }

  it should "handle an Alexa top 1M domain with a subdomain, top-level domain name and country code" in {

    val url = "services.amazon.com.mx"

    val topDomains = sparkContext.broadcast(TopDomains.TopDomains)

    val result = extractDomainInfo(url, topDomains)

    result shouldBe DomainInfo(domain = "amazon", topDomain = 1, subdomain = "services",
      subdomainLength = 8, subdomainEntropy = 2.5, numPeriods = 4)
  }

  it should "handle an Alexa top 1M domain with a top-level domain name and country code but no subdomain" in {

    val url = "amazon.com.mx"
    val countryCodes = sparkContext.broadcast(countryCodesSet)
    val topDomains = sparkContext.broadcast(TopDomains.TopDomains)

    val result = extractDomainInfo(url, topDomains)

    result shouldBe DomainInfo(domain = "amazon", subdomain = "None", topDomain = 1, subdomainLength = 0, subdomainEntropy = 0, numPeriods = 3)
  }

  it should "handle an Alexa top 1M domain with a subdomain and top-level domain name but no country code" in {

    val url = "services.amazon.com"
    val countryCodes = sparkContext.broadcast(countryCodesSet)
    val topDomains = sparkContext.broadcast(TopDomains.TopDomains)

    val result = extractDomainInfo(url, topDomains)

    result shouldBe DomainInfo(domain = "amazon", subdomain = "services", topDomain = 1, subdomainLength = 8, subdomainEntropy = 2.5, numPeriods = 3)
  }


  it should "handle an Alexa top 1M domain with no subdomain or country code" in {

    val url = "amazon.com"
    val countryCodes = sparkContext.broadcast(countryCodesSet)
    val topDomains = sparkContext.broadcast(TopDomains.TopDomains)
    val result = extractDomainInfo(url, topDomains)

    result shouldBe DomainInfo(domain = "amazon", subdomain = "None", topDomain = 1, subdomainLength = 0, subdomainEntropy = 0, numPeriods = 2)
  }
}
