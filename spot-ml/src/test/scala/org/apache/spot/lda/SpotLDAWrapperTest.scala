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
import org.apache.spark.mllib.linalg.{Matrices, Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spot.lda.SpotLDAWrapper._
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities.{FloatPointPrecisionUtility32, FloatPointPrecisionUtility64}
import org.scalatest.Matchers

import scala.collection.immutable.Map

class SpotLDAWrapperTest extends TestingSparkContextFlatSpec with Matchers {

  "SparkLDA" should "handle an extremely unbalanced two word doc with EM optimizer" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha =  1.02
    val ldaBeta = 1.001
    val ldaMaxIterations = 20

    val optimizer = "em"

    val catFancy = SpotLDAInput("pets", "cat", 1)
    val dogWorld = SpotLDAInput("pets", "dog", 999)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))
    val out = SpotLDAWrapper.runLDA(sparkSession, data, 2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer ,ldaMaxIterations, FloatPointPrecisionUtility64)

    val topicMixDF = out.docToTopicMix

    val topicMix =
      topicMixDF.filter(topicMixDF(DocumentName) === "pets").select(TopicProbabilityMix).first().toSeq(0)
        .asInstanceOf[Seq[Double]].toArray
    val catTopics = out.wordResults("cat")
    val dogTopics = out.wordResults("dog")

    Math.abs(topicMix(0) * catTopics(0) + topicMix(1) * catTopics(1)) should be < 0.01
    Math.abs(0.999 - (topicMix(0) * dogTopics(0) + topicMix(1) * dogTopics(1))) should be < 0.01
  }

  it should "handle distinct docs on distinct words with EM optimizer" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha =  1.2
    val ldaBeta = 1.001
    val ldaMaxIterations = 20

    val optimizer = "em"

    val catFancy = SpotLDAInput("cat fancy", "cat", 1)
    val dogWorld = SpotLDAInput("dog world", "dog", 1)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))
    val out = SpotLDAWrapper.runLDA(sparkSession, data, 2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer ,ldaMaxIterations, FloatPointPrecisionUtility64)

    val topicMixDF = out.docToTopicMix
    val dogTopicMix: Array[Double] =
      topicMixDF.filter(topicMixDF(DocumentName) === "dog world").select(TopicProbabilityMix).first()
        .toSeq(0).asInstanceOf[Seq[Double]].toArray

    val catTopicMix: Array[Double] =
      topicMixDF.filter(topicMixDF(DocumentName) === "cat fancy").select(TopicProbabilityMix).first()
        .toSeq(0).asInstanceOf[Seq[Double]].toArray

    val catTopics = out.wordResults("cat")
    val dogTopics = out.wordResults("dog")

    Math.abs(1 - (catTopicMix(0) * catTopics(0) + catTopicMix(1) * catTopics(1))) should be < 0.01
    Math.abs(1 - (dogTopicMix(0) * dogTopics(0) + dogTopicMix(1) * dogTopics(1))) should be < 0.01
  }

  it should "handle an extremely unbalanced two word doc with Online optimizer" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha =  0.0009
    val ldaBeta = 0.00001
    val ldaMaxIterations = 400

    val optimizer = "online"

    val catFancy = SpotLDAInput("pets", "cat", 1)
    val dogWorld = SpotLDAInput("pets", "dog", 999)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))
    val out = SpotLDAWrapper.runLDA(sparkSession, data, 2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, FloatPointPrecisionUtility64)

    val topicMixDF = out.docToTopicMix

    val topicMix =
      topicMixDF.filter(topicMixDF(DocumentName) === "pets").select(TopicProbabilityMix).first().toSeq(0)
        .asInstanceOf[Seq[Double]].toArray
    val catTopics = out.wordResults("cat")
    val dogTopics = out.wordResults("dog")

    Math.abs(topicMix(0) * catTopics(0) + topicMix(1) * catTopics(1)) should be < 0.01
    Math.abs(0.999 - (topicMix(0) * dogTopics(0) + topicMix(1) * dogTopics(1))) should be < 0.01
  }

  it should "handle distinct docs on distinct words with Online optimizer" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha =  0.0009
    val ldaBeta = 0.00001
    val ldaMaxIterations = 400
    val optimizer = "online"

    val catFancy = SpotLDAInput("cat fancy", "cat", 1)
    val dogWorld = SpotLDAInput("dog world", "dog", 1)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))
    val out = SpotLDAWrapper.runLDA(sparkSession, data, 2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, FloatPointPrecisionUtility64)

    val topicMixDF = out.docToTopicMix
    val dogTopicMix: Array[Double] =
      topicMixDF.filter(topicMixDF(DocumentName) === "dog world").select(TopicProbabilityMix).first()
        .toSeq(0).asInstanceOf[Seq[Double]].toArray

    val catTopicMix: Array[Double] =
      topicMixDF.filter(topicMixDF(DocumentName) === "cat fancy").select(TopicProbabilityMix).first()
        .toSeq(0).asInstanceOf[Seq[Double]].toArray

    val catTopics = out.wordResults("cat")
    val dogTopics = out.wordResults("dog")

    Math.abs(1 - (catTopicMix(0) * catTopics(0) + catTopicMix(1) * catTopics(1))) should be < 0.01
    Math.abs(1 - (dogTopicMix(0) * dogTopics(0) + dogTopicMix(1) * dogTopics(1))) should be < 0.01
  }

  it should "handle an extremely unbalanced two word doc with doc probabilities as Float" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha =  1.02
    val ldaBeta = 1.001
    val ldaMaxIterations = 20

    val optimizer = "em"

    val catFancy = SpotLDAInput("pets", "cat", 1)
    val dogWorld = SpotLDAInput("pets", "dog", 999)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))
    val out = SpotLDAWrapper.runLDA(sparkSession, data, 2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, FloatPointPrecisionUtility32)

    val topicMixDF = out.docToTopicMix

    val topicMix =
      topicMixDF.filter(topicMixDF(DocumentName) === "pets").select(TopicProbabilityMix).first().toSeq(0)
        .asInstanceOf[Seq[Float]].toArray
    val catTopics = out.wordResults("cat")
    val dogTopics = out.wordResults("dog")

    Math.abs(topicMix(0).toDouble * catTopics(0) + topicMix(1).toDouble * catTopics(1)) should be < 0.01
    Math.abs(0.999 - (topicMix(0).toDouble * dogTopics(0) + topicMix(1).toDouble * dogTopics(1))) should be < 0.01
  }

  it should "handle distinct docs on distinct words with doc probabilities as Float" in {
    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.WARN)

    val ldaAlpha =  1.02
    val ldaBeta = 1.001
    val ldaMaxIterations = 20

    val optimizer = "em"

    val catFancy = SpotLDAInput("cat fancy", "cat", 1)
    val dogWorld = SpotLDAInput("dog world", "dog", 1)

    val data = sparkSession.sparkContext.parallelize(Seq(catFancy, dogWorld))
    val out = SpotLDAWrapper.runLDA(sparkSession, data, 2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta,
      optimizer, ldaMaxIterations, FloatPointPrecisionUtility32)

    val topicMixDF = out.docToTopicMix
    val dogTopicMix: Array[Float] =
      topicMixDF.filter(topicMixDF(DocumentName) === "dog world").select(TopicProbabilityMix).first().toSeq(0)
        .asInstanceOf[Seq[Float]].toArray

    val catTopicMix: Array[Float] =
      topicMixDF.filter(topicMixDF(DocumentName) === "cat fancy").select(TopicProbabilityMix).first().toSeq(0)
        .asInstanceOf[Seq[Float]].toArray

    val catTopics = out.wordResults("cat")
    val dogTopics = out.wordResults("dog")

    Math.abs(1 - (catTopicMix(0) * catTopics(0) + catTopicMix(1) * catTopics(1))) should be < 0.01
    Math.abs(1 - (dogTopicMix(0) * dogTopics(0) + dogTopicMix(1) * dogTopics(1))) should be < 0.01
  }

  "formatSparkLDAInput" should "return input in RDD[(Long, Vector)] (collected as Array for testing) format. The index " +
    "is the docID, values are the vectors of word occurrences in that doc" in {


    val documentWordData = sparkSession.sparkContext.parallelize(Seq(SpotLDAInput("192.168.1.1", "333333_7.0_0.0_1.0", 8),
      SpotLDAInput("10.10.98.123", "1111111_6.0_3.0_5.0", 4),
      SpotLDAInput("66.23.45.11", "-1_43_7.0_2.0_6.0", 2),
      SpotLDAInput("192.168.1.1", "-1_80_6.0_1.0_1.0", 5)))

    val wordDictionary = Map("333333_7.0_0.0_1.0" -> 0, "1111111_6.0_3.0_5.0" -> 1, "-1_43_7.0_2.0_6.0" -> 2,
      "-1_80_6.0_1.0_1.0" -> 3)

    val documentDictionary: DataFrame = sparkSession.createDataFrame(documentWordData
      .map({ case SpotLDAInput(doc, word, count) => doc })
      .distinct
      .zipWithIndex.map({ case (d, c) => Row(d, c) }), StructType(List(DocumentNameField, DocumentNumberField)))


    val sparkLDAInput: RDD[(Long, Vector)] = SpotLDAWrapper.formatSparkLDAInput(documentWordData,
      documentDictionary, wordDictionary, sparkSession)
    val sparkLDAInArr: Array[(Long, Vector)] = sparkLDAInput.collect()

    sparkLDAInArr shouldBe Array((0, Vectors.sparse(4, Array(0, 3), Array(8.0, 5.0))), (2, Vectors.sparse(4, Array
    (2), Array(2.0))), (1, Vectors.sparse(4, Array(1), Array(4.0))))
  }

  "formatSparkLDADocTopicOutput" should "return RDD[(String,Array(Double))] after converting doc results from vector " +
    "using PrecisionUtilityDouble: convert docID back to string, convert vector of probabilities to array" in {

    val documentWordData = sparkSession.sparkContext.parallelize(Seq(SpotLDAInput("192.168.1.1", "333333_7.0_0.0_1.0", 8),
      SpotLDAInput("10.10.98.123", "1111111_6.0_3.0_5.0", 4),
      SpotLDAInput("66.23.45.11", "-1_43_7.0_2.0_6.0", 2),
      SpotLDAInput("192.168.1.1", "-1_80_6.0_1.0_1.0", 5)))

    val documentDictionary: DataFrame = sparkSession.createDataFrame(documentWordData
      .map({ case SpotLDAInput(doc, word, count) => doc })
      .distinct
      .zipWithIndex.map({ case (d, c) => Row(d, c) }), StructType(List(DocumentNameField, DocumentNumberField)))

    val docTopicDist: RDD[(Long, Vector)] = sparkSession.sparkContext.parallelize(
      Array((0.toLong, Vectors.dense(0.15, 0.3, 0.5, 0.05)),
        (1.toLong, Vectors.dense(0.25, 0.15, 0.4, 0.2)),
        (2.toLong, Vectors.dense(0.4, 0.1, 0.3, 0.2))))

    val sparkDocRes: DataFrame = formatSparkLDADocTopicOutput(docTopicDist, documentDictionary, sparkSession,
      FloatPointPrecisionUtility64)

    import testImplicits._
    val documents = sparkDocRes.map({ case Row(documentName: String, docProbabilities: Seq[Double]) => (documentName,
      docProbabilities)
    }).collect

    val documentProbabilities = sparkDocRes.select(TopicProbabilityMix).first.toSeq(0).asInstanceOf[Seq[Double]]

    documents should contain("192.168.1.1", Seq(0.15, 0.3, 0.5, 0.05))
    documents should contain("10.10.98.123", Seq(0.25, 0.15, 0.4, 0.2))
    documents should contain("66.23.45.11", Seq(0.4, 0.1, 0.3, 0.2))

    documentProbabilities(0) shouldBe a[java.lang.Double]

  }

  it should "return RDD[(String,Array(Float))] after converting doc results from vector " +
    "using PrecisionUtilityFloat: convert docID back to string, convert vector of probabilities to array" in {

    val documentWordData = sparkSession.sparkContext.parallelize(Seq(SpotLDAInput("192.168.1.1", "333333_7.0_0.0_1.0", 8),
      SpotLDAInput("10.10.98.123", "1111111_6.0_3.0_5.0", 4),
      SpotLDAInput("66.23.45.11", "-1_43_7.0_2.0_6.0", 2),
      SpotLDAInput("192.168.1.1", "-1_80_6.0_1.0_1.0", 5)))

    val documentDictionary: DataFrame = sparkSession.createDataFrame(documentWordData
      .map({ case SpotLDAInput(doc, word, count) => doc })
      .distinct
      .zipWithIndex.map({ case (d, c) => Row(d, c) }), StructType(List(DocumentNameField, DocumentNumberField)))

    val docTopicDist: RDD[(Long, Vector)] = sparkSession.sparkContext.parallelize(
      Array((0.toLong, Vectors.dense(0.15, 0.3, 0.5, 0.05)),
        (1.toLong, Vectors.dense(0.25, 0.15, 0.4, 0.2)),
        (2.toLong, Vectors.dense(0.4, 0.1, 0.3, 0.2))))

    val sparkDocRes: DataFrame = formatSparkLDADocTopicOutput(docTopicDist, documentDictionary, sparkSession,
      FloatPointPrecisionUtility32)

    import testImplicits._
    val documents = sparkDocRes.map({ case Row(documentName: String, docProbabilities: Seq[Float]) => (documentName,
      docProbabilities)
    }).collect

    val documentProbabilities = sparkDocRes.select(TopicProbabilityMix).first.toSeq(0).asInstanceOf[Seq[Float]]

    documents should contain("192.168.1.1", Seq(0.15f, 0.3f, 0.5f, 0.05f))
    documents should contain("10.10.98.123", Seq(0.25f, 0.15f, 0.4f, 0.2f))
    documents should contain("66.23.45.11", Seq(0.4f, 0.1f, 0.3f, 0.2f))

    documentProbabilities(0) shouldBe a[java.lang.Float]
  }

  "formatSparkLDAWordOutput" should "return Map[Int,String] after converting word matrix to sequence, wordIDs back " +
    "to strings, and sequence of probabilities to array" in {
    val testMat = Matrices.dense(4, 4, Array(0.5, 0.2, 0.05, 0.25, 0.25, 0.1, 0.15, 0.5, 0.1, 0.4, 0.25, 0.25, 0.7, 0.2, 0.02, 0.08))

    val wordDictionary = Map("-1_23.0_7.0_7.0_4.0" -> 3, "23.0_7.0_7.0_4.0" -> 0, "333333.0_7.0_7.0_4.0" -> 2, "80.0_7.0_7.0_4.0" -> 1)
    val revWordMap: Map[Int, String] = wordDictionary.map(_.swap)

    val sparkWordRes = formatSparkLDAWordOutput(testMat, revWordMap)

    sparkWordRes should contain key ("23.0_7.0_7.0_4.0")
    sparkWordRes should contain key ("80.0_7.0_7.0_4.0")
    sparkWordRes should contain key ("333333.0_7.0_7.0_4.0")
    sparkWordRes should contain key ("-1_23.0_7.0_7.0_4.0")
  }
}