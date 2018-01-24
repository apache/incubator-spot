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

import org.apache.spark.mllib.linalg.{Matrices, Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spot.lda.SpotLDAWrapperSchema.TopicProbabilityMix
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities.{FloatPointPrecisionUtility32, FloatPointPrecisionUtility64}
import org.scalatest.Matchers

/**
  * Created by rabarona on 7/17/17.
  */
class SpotLDAHelperTest extends TestingSparkContextFlatSpec with Matchers {

  "formatSparkLDAInput" should "return input in RDD[(Long, Vector)] (collected as Array for testing) format. The index " +
    "is the docID, values are the vectors of word occurrences in that doc" in {


    val documentWordData = sparkSession.sparkContext.parallelize(Seq(SpotLDAInput("192.168.1.1", "333333_7.0_0.0_1.0", 8),
      SpotLDAInput("10.10.98.123", "1111111_6.0_3.0_5.0", 4),
      SpotLDAInput("66.23.45.11", "-1_43_7.0_2.0_6.0", 2),
      SpotLDAInput("192.168.1.1", "-1_80_6.0_1.0_1.0", 5)))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(documentWordData, FloatPointPrecisionUtility64, sparkSession)

    val sparkLDAInput: RDD[(Long, Vector)] = spotLDAHelper.formattedCorpus
    val sparkLDAInArr: Array[(Long, Vector)] = sparkLDAInput.collect()

    sparkLDAInArr shouldBe Array((0, Vectors.sparse(4, Array(0, 3), Array(5.0, 8.0))), (2, Vectors.sparse(4, Array
    (1), Array(2.0))), (1, Vectors.sparse(4, Array(2), Array(4.0))))
  }

  "formatSparkLDADocTopicOutput" should "return RDD[(String,Array(Double))] after converting doc results from vector " +
    "using PrecisionUtilityDouble: convert docID back to string, convert vector of probabilities to array" in {

    val documentWordData = sparkSession.sparkContext.parallelize(Seq(SpotLDAInput("192.168.1.1", "333333_7.0_0.0_1.0", 8),
      SpotLDAInput("10.10.98.123", "1111111_6.0_3.0_5.0", 4),
      SpotLDAInput("66.23.45.11", "-1_43_7.0_2.0_6.0", 2),
      SpotLDAInput("192.168.1.1", "-1_80_6.0_1.0_1.0", 5)))

    val docTopicDist: RDD[(Long, Vector)] = sparkSession.sparkContext.parallelize(
      Array((0.toLong, Vectors.dense(0.15, 0.3, 0.5, 0.05)),
        (1.toLong, Vectors.dense(0.25, 0.15, 0.4, 0.2)),
        (2.toLong, Vectors.dense(0.4, 0.1, 0.3, 0.2))))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(documentWordData, FloatPointPrecisionUtility64, sparkSession)

    val sparkDocRes: DataFrame = spotLDAHelper.formatDocumentDistribution(docTopicDist)

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

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(documentWordData, FloatPointPrecisionUtility32, sparkSession)

    val docTopicDist: RDD[(Long, Vector)] = sparkSession.sparkContext.parallelize(
      Array((0.toLong, Vectors.dense(0.15, 0.3, 0.5, 0.05)),
        (1.toLong, Vectors.dense(0.25, 0.15, 0.4, 0.2)),
        (2.toLong, Vectors.dense(0.4, 0.1, 0.3, 0.2))))

    val sparkDocRes: DataFrame = spotLDAHelper.formatDocumentDistribution(docTopicDist)

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

    val documentWordData = sparkSession.sparkContext.parallelize(Seq(SpotLDAInput("192.168.1.1", "23.0_7.0_7.0_4.0", 8),
      SpotLDAInput("10.10.98.123", "80.0_7.0_7.0_4.0", 4),
      SpotLDAInput("66.23.45.11", "333333.0_7.0_7.0_4.0", 2),
      SpotLDAInput("192.168.1.2", "-1_23.0_7.0_7.0_4.0", 5)))

    val spotLDAHelper: SpotLDAHelper = SpotLDAHelper(documentWordData, FloatPointPrecisionUtility64, sparkSession)

    val sparkWordRes = spotLDAHelper.formatTopicDistributions(testMat)

    sparkWordRes should contain key ("23.0_7.0_7.0_4.0")
    sparkWordRes should contain key ("80.0_7.0_7.0_4.0")
    sparkWordRes should contain key ("333333.0_7.0_7.0_4.0")
    sparkWordRes should contain key ("-1_23.0_7.0_7.0_4.0")
  }
}
