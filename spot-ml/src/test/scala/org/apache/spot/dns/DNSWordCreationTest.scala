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

}
