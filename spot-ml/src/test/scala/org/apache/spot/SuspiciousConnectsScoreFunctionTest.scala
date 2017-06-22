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

import org.apache.spark.broadcast.Broadcast
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.apache.spot.utilities.{FloatPointPrecisionUtility32, FloatPointPrecisionUtility64}
import org.scalatest.Matchers

/**
  * Created by rabarona on 5/17/17.
  */
class SuspiciousConnectsScoreFunctionTest extends TestingSparkContextFlatSpec with Matchers {

  "score" should "return score of type Double with document probabilities of type Double" in {

    val wordToPerTopicProb: Map[String, Array[Double]] = Map(("word_1" -> Array.fill(4)(0.05)))
    val wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]] = sparkSession.sparkContext.broadcast(wordToPerTopicProb)
    val topicCount = 4

    val precisionUtility = FloatPointPrecisionUtility64
    val documentProbabilities = Seq(0.05d, 0.05d, 0.05d, 0.05d)

    val scoreFunction = new SuspiciousConnectsScoreFunction(topicCount, wordToPerTopicProbBC)

    val score = scoreFunction.score(precisionUtility)(documentProbabilities, "word_1")

    score shouldBe 0.010000000000000002d

  }

  it should "return score of type Double with document probabilities of type Float" in {

    val wordToPerTopicProb: Map[String, Array[Double]] = Map(("word_1" -> Array.fill(4)(0.05)))
    val wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]] = sparkSession.sparkContext.broadcast(wordToPerTopicProb)
    val topicCount = 4

    val precisionUtility = FloatPointPrecisionUtility32
    val documentProbabilities = Seq(0.05f, 0.05f, 0.05f, 0.05f)

    val scoreFunction = new SuspiciousConnectsScoreFunction(topicCount, wordToPerTopicProbBC)

    val score = scoreFunction.score(precisionUtility)(documentProbabilities, "word_1")

    score shouldBe 0.010000000149011612d

  }

  it should "return score -1 when word doesn't exists" in {
    val wordToPerTopicProb: Map[String, Array[Double]] = Map(("word_1" -> Array.fill(4)(0.05)))
    val wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]] = sparkSession.sparkContext.broadcast(wordToPerTopicProb)
    val topicCount = 4

    val precisionUtility = FloatPointPrecisionUtility32
    val documentProbabilities = Seq(0.05f, 0.05f, 0.05f, 0.05f)

    val scoreFunction = new SuspiciousConnectsScoreFunction(topicCount, wordToPerTopicProbBC)

    val score = scoreFunction.score(precisionUtility)(documentProbabilities, "word_error")

    score shouldBe -1d
  }

}
