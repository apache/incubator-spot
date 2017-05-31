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
import org.apache.spot.utilities.FloatPointPrecisionUtility
import org.apache.spot.utilities.data.validation.InvalidDataHandler

/**
  * Base class for scoring suspicious connects models.
  * Assumes that distribution of words is independent of the IP when conditioned on the topic
  * and performs a simple sum over a partition of the space by topic.
  *
  * @param topicCount           Number of topics produced by the topic modelling analysis.
  * @param wordToPerTopicProbBC Broadcast of map assigning words to per-topic conditional probability.
  */
class SuspiciousConnectsScoreFunction(topicCount: Int,
                                      wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]]) extends Serializable {

  def score[P <: FloatPointPrecisionUtility](precisionUtility: P)
                                            (documentTopicMix: Seq[precisionUtility.TargetType], word: String): Double = {

    val zeroProb = Array.fill(topicCount) {
      0d
    }

    if (word == InvalidDataHandler.WordError) {
      InvalidDataHandler.ScoreError
    } else {
      // If either the ip or the word key value cannot be found it means that it was not seen in training.
      val wordGivenTopicProbabilities = wordToPerTopicProbBC.value.getOrElse(word, zeroProb)
      val documentTopicMixDouble: Seq[Double] = precisionUtility.toDoubles(documentTopicMix)

      documentTopicMixDouble.zip(wordGivenTopicProbabilities)
        .map({ case (pWordGivenTopic, pTopicGivenDoc) => pWordGivenTopic * pTopicGivenDoc })
        .sum
    }
  }
}