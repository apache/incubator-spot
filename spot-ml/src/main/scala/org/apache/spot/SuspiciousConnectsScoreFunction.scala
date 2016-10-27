package org.apache.spot

import org.apache.spark.broadcast.Broadcast



class SuspiciousConnectsScoreFunction(topicCount: Int,
                                      ipToTopicMixBC: Broadcast[Map[String, Array[Double]]],
                                      wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]]) extends Serializable {

  def score(ip: String, word: String): Double = {

    val zeroProb = Array.fill(topicCount) { 0d }

    val topicGivenDocProbs = ipToTopicMixBC.value.getOrElse(ip, zeroProb)
    val wordGivenTopicProbs = wordToPerTopicProbBC.value.getOrElse(word, zeroProb)

    topicGivenDocProbs.zip(wordGivenTopicProbs)
      .map({ case (pWordGivenTopic, pTopicGivenDoc) => pWordGivenTopic * pTopicGivenDoc })
      .sum
  }

}
