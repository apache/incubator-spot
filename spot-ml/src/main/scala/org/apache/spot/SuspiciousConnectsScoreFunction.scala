package org.apache.spot

import org.apache.spark.broadcast.Broadcast
import org.apache.spot.utilities.data.validation.InvalidDataHandler

/**
  * Base class for scoring suspicious connects models.
  * Assumes that distribution of words is independent of the IP when conditioned on the topic
  * and performs a simple sum over a partition of the space by topic.
  *
  * @param topicCount Number of topics produced by the topic modelling analysis.
  * @param ipToTopicMixBC Broadcast of map assigning IPs to topic mixes.
  * @param wordToPerTopicProbBC Broadcast of map assigning words to per-topic conditional probability.
  */
class SuspiciousConnectsScoreFunction(topicCount: Int,
                                      ipToTopicMixBC: Broadcast[Map[String, Array[Double]]],
                                      wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]]) extends Serializable {

  def score(ip: String, word: String): Double = {

    val zeroProb = Array.fill(topicCount) { 0d }

    if(word == InvalidDataHandler.WordError){
      InvalidDataHandler.ScoreError
    } else {
      // If either the ip or the word key value cannot be found it means that it was not seen in training.
      val topicGivenDocProbs = ipToTopicMixBC.value.getOrElse(ip, zeroProb)
      val wordGivenTopicProbs = wordToPerTopicProbBC.value.getOrElse(word, zeroProb)

      topicGivenDocProbs.zip(wordGivenTopicProbs)
        .map({ case (pWordGivenTopic, pTopicGivenDoc) => pWordGivenTopic * pTopicGivenDoc })
        .sum
    }
  }
}