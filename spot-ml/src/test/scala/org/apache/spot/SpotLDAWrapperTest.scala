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

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.mllib.linalg.{Matrices, Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spot.lda.SpotLDAWrapper
import org.apache.spot.lda.SpotLDAWrapper._
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

import scala.collection.immutable.Map

class SpotLDAWrapperTest extends TestingSparkContextFlatSpec with Matchers {

    val ldaAlpha = 1.02
    val ldaBeta = 1.001
    val ldaMaxiterations = 20

    "SparkLDA" should "handle an extremely unbalanced two word doc" in {
      val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
      logger.setLevel(Level.WARN)

      val catFancy = SpotLDAInput("pets", "cat", 1)
      val dogWorld = SpotLDAInput("pets", "dog", 999)

      val data = spark.sparkContext.parallelize(Seq(catFancy, dogWorld))
      val out = SpotLDAWrapper.runLDA(spark, data, 2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta, ldaMaxiterations)

      val topicMixDF = out.docToTopicMix

      var topicMix =
        topicMixDF.filter(topicMixDF(DocumentName) === "pets").select(TopicProbabilityMix).first().toSeq(0).asInstanceOf[Seq[Double]].toArray
      val catTopics = out.wordResults("cat")
      val dogTopics = out.wordResults("dog")

      Math.abs(topicMix(0) * catTopics(0) + topicMix(1) * catTopics(1)) should be < 0.01
      Math.abs(0.999 - (topicMix(0) * dogTopics(0) + topicMix(1) * dogTopics(1))) should be < 0.01
    }

    "SparkLDA" should "handle distinct docs on distinct words" in {
      val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
      logger.setLevel(Level.WARN)
      val catFancy = SpotLDAInput("cat fancy", "cat", 1)
      val dogWorld = SpotLDAInput("dog world", "dog", 1)

      val data = spark.sparkContext.parallelize(Seq(catFancy, dogWorld))
      val out = SpotLDAWrapper.runLDA(spark, data, 2, logger, Some(0xdeadbeef), ldaAlpha, ldaBeta, ldaMaxiterations)

      val topicMixDF = out.docToTopicMix
      var dogTopicMix: Array[Double] =
        topicMixDF.filter(topicMixDF(DocumentName) === "dog world").select(TopicProbabilityMix).first().toSeq(0).asInstanceOf[Seq[Double]].toArray

      val catTopicMix: Array[Double] =
        topicMixDF.filter(topicMixDF(DocumentName) === "cat fancy").select(TopicProbabilityMix).first().toSeq(0).asInstanceOf[Seq[Double]].toArray

      val catTopics = out.wordResults("cat")
      val dogTopics = out.wordResults("dog")

      Math.abs(1 - (catTopicMix(0) * catTopics(0) + catTopicMix(1) * catTopics(1))) should be < 0.01
      Math.abs(1 - (dogTopicMix(0) * dogTopics(0) + dogTopicMix(1) * dogTopics(1))) should be < 0.01
    }

    "formatSparkLDAInput" should "return input in RDD[(Long, Vector)] (collected as Array for testing) format. The index " +
      "is the docID, values are the vectors of word occurrences in that doc" in {


      val documentWordData = spark.sparkContext.parallelize(Seq(SpotLDAInput("192.168.1.1", "333333_7.0_0.0_1.0", 8),
        SpotLDAInput("10.10.98.123", "1111111_6.0_3.0_5.0", 4),
        SpotLDAInput("66.23.45.11", "-1_43_7.0_2.0_6.0", 2),
        SpotLDAInput("192.168.1.1", "-1_80_6.0_1.0_1.0", 5)))

      val wordDictionary = Map("333333_7.0_0.0_1.0" -> 0, "1111111_6.0_3.0_5.0" -> 1, "-1_43_7.0_2.0_6.0" -> 2, "-1_80_6.0_1.0_1.0" -> 3)

      val documentDictionary: DataFrame = spark.createDataFrame(documentWordData
          .map({ case SpotLDAInput(doc, word, count) => doc })
          .distinct
          .zipWithIndex.map({case (d,c) => Row(d,c)}), StructType(List(DocumentNameField, DocumentNumberField)))


      val sparkLDAInput: RDD[(Long, Vector)] = SpotLDAWrapper.formatSparkLDAInput(documentWordData, documentDictionary, wordDictionary, spark)
      val sparkLDAInArr: Array[(Long, Vector)] = sparkLDAInput.collect()

      sparkLDAInArr shouldBe Array((0, Vectors.sparse(4, Array(0, 3), Array(8.0, 5.0))), (2, Vectors.sparse(4, Array(2), Array(2.0))), (1, Vectors.sparse(4, Array(1), Array(4.0))))
    }

    "formatSparkLDADocTopicOuptut" should "return RDD[(String,Array(Double))] after converting doc results from vector: " +
      "convert docID back to string, convert vector of probabilities to array" in {


      val documentWordData = spark.sparkContext.parallelize(Seq(SpotLDAInput("192.168.1.1", "333333_7.0_0.0_1.0", 8),
        SpotLDAInput("10.10.98.123", "1111111_6.0_3.0_5.0", 4),
        SpotLDAInput("66.23.45.11", "-1_43_7.0_2.0_6.0", 2),
        SpotLDAInput("192.168.1.1", "-1_80_6.0_1.0_1.0", 5)))

      val documentDictionary: DataFrame = spark.createDataFrame(documentWordData
          .map({ case SpotLDAInput(doc, word, count) => doc })
          .distinct
          .zipWithIndex.map({case (d,c) => Row(d,c)}), StructType(List(DocumentNameField, DocumentNumberField)))

      val docTopicDist: RDD[(Long, Vector)] = spark.sparkContext.parallelize(Array((0.toLong, Vectors.dense(0.15,
        0.3, 0.5, 0.05)), (1.toLong,
        Vectors.dense(0.25, 0.15, 0.4, 0.2)), (2.toLong, Vectors.dense(0.4, 0.1, 0.3, 0.2))))

      val sparkDocRes: DataFrame = formatSparkLDADocTopicOutput(docTopicDist, documentDictionary, spark)

      import testImplicits._
      val documents = sparkDocRes.select(DocumentName).map(documentName => documentName.toString.replaceAll("\\[", "").replaceAll("\\]", "")).collect()

      documents(0) should be("66.23.45.11")
      documents(1) should be("192.168.1.1")
      documents(2) should be("10.10.98.123")
    }

    "formatSparkLDAWordOutput" should "return Map[Int,String] after converting word matrix to sequence, wordIDs back to strings, and sequence of probabilities to array" in {
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