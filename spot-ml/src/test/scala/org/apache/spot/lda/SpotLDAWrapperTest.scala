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

package org.apache.spot.lda

import org.apache.log4j.{Level, LogManager}
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities.{FloatPointPrecisionUtility32, FloatPointPrecisionUtility64}
import org.scalatest.Matchers

class SpotLDAWrapperTest extends TestingSparkContextFlatSpec with Matchers {

  "SparkLDA" should "handle an extremely unbalanced two word doc with EM optimizer" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha = 1.02
    val ldaBeta = 1.001
    val ldaMaxIterations = 20

    val optimizer = "em"

    val catFancy = SpotLDAInput("pets", "cat", 1)
    val dogWorld = SpotLDAInput("pets", "dog", 999)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(data, FloatPointPrecisionUtility64, sparkSession)
    val model: SpotLDAModel = SpotLDAWrapper.run(2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, spotLDAHelper)

    val results = model.predict(spotLDAHelper)

    val topicMixDF = results.documentToTopicMix

    val topicMix =
      topicMixDF.filter(topicMixDF(DocumentName) === "pets").select(TopicProbabilityMix).first().toSeq.head
        .asInstanceOf[Seq[Double]].toArray
    val catTopics = results.wordToTopicMix("cat")
    val dogTopics = results.wordToTopicMix("dog")

    Math.abs(topicMix(0) * catTopics(0) + topicMix(1) * catTopics(1)) should be < 0.01
    Math.abs(0.999 - (topicMix(0) * dogTopics(0) + topicMix(1) * dogTopics(1))) should be < 0.01
  }

  it should "handle distinct docs on distinct words with EM optimizer" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha = 1.02
    val ldaBeta = 1.001
    val ldaMaxIterations = 100

    val optimizer = "em"

    val catFancy = SpotLDAInput("cat fancy", "cat", 1)
    val dogWorld = SpotLDAInput("dog world", "dog", 1)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(data, FloatPointPrecisionUtility64, sparkSession)
    val model: SpotLDAModel = SpotLDAWrapper.run(2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, spotLDAHelper)

    val results = model.predict(spotLDAHelper)

    val topicMixDF = results.documentToTopicMix
    val dogTopicMix: Array[Double] =
      topicMixDF.filter(topicMixDF(DocumentName) === "dog world").select(TopicProbabilityMix).first()
        .toSeq.head.asInstanceOf[Seq[Double]].toArray

    val catTopicMix: Array[Double] =
      topicMixDF.filter(topicMixDF(DocumentName) === "cat fancy").select(TopicProbabilityMix).first()
        .toSeq.head.asInstanceOf[Seq[Double]].toArray

    val catTopics = results.wordToTopicMix("cat")
    val dogTopics = results.wordToTopicMix("dog")

    Math.abs(1 - (catTopicMix(0) * catTopics(0) + catTopicMix(1) * catTopics(1))) should be < 0.01
    Math.abs(1 - (dogTopicMix(0) * dogTopics(0) + dogTopicMix(1) * dogTopics(1))) should be < 0.01
  }

  it should "handle an extremely unbalanced two word doc with Online optimizer" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha = 0.0009
    val ldaBeta = 0.00001
    val ldaMaxIterations = 400

    val optimizer = "online"

    val catFancy = SpotLDAInput("pets", "cat", 1)
    val dogWorld = SpotLDAInput("pets", "dog", 999)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(data, FloatPointPrecisionUtility64, sparkSession)
    val model: SpotLDAModel = SpotLDAWrapper.run(2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, spotLDAHelper)

    val results = model.predict(spotLDAHelper)

    val topicMixDF = results.documentToTopicMix

    val topicMix =
      topicMixDF.filter(topicMixDF(DocumentName) === "pets").select(TopicProbabilityMix).first().toSeq.head
        .asInstanceOf[Seq[Double]].toArray
    val catTopics = results.wordToTopicMix("cat")
    val dogTopics = results.wordToTopicMix("dog")

    Math.abs(topicMix(0) * catTopics(0) + topicMix(1) * catTopics(1)) should be < 0.01
    Math.abs(0.999 - (topicMix(0) * dogTopics(0) + topicMix(1) * dogTopics(1))) should be < 0.01
  }

  it should "handle distinct docs on distinct words with Online optimizer" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha = 0.0009
    val ldaBeta = 0.00001
    val ldaMaxIterations = 400
    val optimizer = "online"

    val catFancy = SpotLDAInput("cat fancy", "cat", 1)
    val dogWorld = SpotLDAInput("dog world", "dog", 1)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(data, FloatPointPrecisionUtility64, sparkSession)
    val model: SpotLDAModel = SpotLDAWrapper.run(2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, spotLDAHelper)

    val results = model.predict(spotLDAHelper)

    val topicMixDF = results.documentToTopicMix

    val dogTopicMix: Array[Double] =
      topicMixDF.filter(topicMixDF(DocumentName) === "dog world").select(TopicProbabilityMix).first()
        .toSeq.head.asInstanceOf[Seq[Double]].toArray

    val catTopicMix: Array[Double] =
      topicMixDF.filter(topicMixDF(DocumentName) === "cat fancy").select(TopicProbabilityMix).first()
        .toSeq.head.asInstanceOf[Seq[Double]].toArray

    val catTopics = results.wordToTopicMix("cat")
    val dogTopics = results.wordToTopicMix("dog")

    Math.abs(1 - (catTopicMix(0) * catTopics(0) + catTopicMix(1) * catTopics(1))) should be < 0.01
    Math.abs(1 - (dogTopicMix(0) * dogTopics(0) + dogTopicMix(1) * dogTopics(1))) should be < 0.01
  }

  it should "handle an extremely unbalanced two word doc with doc probabilities as Float" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha = 1.02
    val ldaBeta = 1.001
    val ldaMaxIterations = 20

    val optimizer = "em"

    val catFancy = SpotLDAInput("pets", "cat", 1)
    val dogWorld = SpotLDAInput("pets", "dog", 999)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(data, FloatPointPrecisionUtility32, sparkSession)
    val model: SpotLDAModel = SpotLDAWrapper.run(2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, spotLDAHelper)

    val results = model.predict(spotLDAHelper)

    val topicMixDF = results.documentToTopicMix

    val topicMix =
      topicMixDF.filter(topicMixDF(DocumentName) === "pets").select(TopicProbabilityMix).first().toSeq.head
        .asInstanceOf[Seq[Float]].toArray
    val catTopics = results.wordToTopicMix("cat")
    val dogTopics = results.wordToTopicMix("dog")

    Math.abs(topicMix(0).toDouble * catTopics(0) + topicMix(1).toDouble * catTopics(1)) should be < 0.01
    Math.abs(0.999 - (topicMix(0).toDouble * dogTopics(0) + topicMix(1).toDouble * dogTopics(1))) should be < 0.01
  }

  it should "handle distinct docs on distinct words with doc probabilities as Float" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha = 1.02
    val ldaBeta = 1.001
    val ldaMaxIterations = 20

    val optimizer = "em"

    val catFancy = SpotLDAInput("cat fancy", "cat", 1)
    val dogWorld = SpotLDAInput("dog world", "dog", 1)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(data, FloatPointPrecisionUtility32, sparkSession)
    val model: SpotLDAModel = SpotLDAWrapper.run(2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, spotLDAHelper)

    val results = model.predict(spotLDAHelper)

    val topicMixDF = results.documentToTopicMix

    val dogTopicMix: Array[Float] =
      topicMixDF.filter(topicMixDF(DocumentName) === "dog world").select(TopicProbabilityMix).first().toSeq.head
        .asInstanceOf[Seq[Float]].toArray

    val catTopicMix: Array[Float] =
      topicMixDF.filter(topicMixDF(DocumentName) === "cat fancy").select(TopicProbabilityMix).first().toSeq.head
        .asInstanceOf[Seq[Float]].toArray

    val catTopics = results.wordToTopicMix("cat")
    val dogTopics = results.wordToTopicMix("dog")

    Math.abs(1 - (catTopicMix(0) * catTopics(0) + catTopicMix(1) * catTopics(1))) should be < 0.01
    Math.abs(1 - (dogTopicMix(0) * dogTopics(0) + dogTopicMix(1) * dogTopics(1))) should be < 0.01
  }

}