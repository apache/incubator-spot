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

package org.apache.spot

import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.scalatest.{FlatSpec, Matchers}

class SuspiciousConnectsArgumentParserTest extends FlatSpec with Matchers {

  "Argument parser" should "parse parameters correctly" in {

    val args = Array("--analysis", "dns",
      "--input", "user/spot-data",
      "--dupfactor", "1000",
      "--feedback", "/home/user/ml/dns/test/dns_scores.csv",
      "--ldatopiccount", "10",
      "--scored", "/user/user/dns/test/scored_results/scores",
      "--threshold", "1.1",
      "--maxresults", "20",
      "--ldamaxiterations", "20",
      "--ldaalpha", "0.0009",
      "--ldabeta", "0.00001",
      "--ldaoptimizer", "online",
      "--precision", "64",
      "--userdomain", "mycompany")

    val parser = SuspiciousConnectsArgumentParser.parser
    val config = parser.parse(args, SuspiciousConnectsConfig()) match {
      case Some(config) => config
      case None => SuspiciousConnectsConfig()
    }

    config.analysis shouldBe "dns"
    config.duplicationFactor shouldBe 1000
    config.topicCount shouldBe 10
    config.ldaAlpha shouldBe 0.0009
    config.ldaBeta shouldBe 0.00001
    config.ldaOptimizer shouldBe "online"

  }

  it should "parse parameters and take defaults when optional parameters are not provided" in {
    val args = Array("--analysis", "dns",
      "--input", "user/spot-data",
      "--feedback", "/home/user/ml/dns/test/dns_scores.csv",
      "--ldatopiccount", "10",
      "--scored", "/user/user/dns/test/scored_results/scores",
      "--threshold", "1.1",
      "--maxresults", "20",
      "--userdomain", "mycompany")

    val parser = SuspiciousConnectsArgumentParser.parser
    val config = parser.parse(args, SuspiciousConnectsConfig()) match {
      case Some(config) => config
      case None => SuspiciousConnectsConfig()
    }

    config.analysis shouldBe "dns"
    config.duplicationFactor shouldBe 1
    config.topicCount shouldBe 10
    config.ldaAlpha shouldBe 1.02
    config.ldaBeta shouldBe 1.001
    config.ldaOptimizer shouldBe "em"
    config.outputDelimiter shouldBe "\t"
  }
}
