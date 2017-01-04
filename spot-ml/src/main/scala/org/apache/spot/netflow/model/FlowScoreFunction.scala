package org.apache.spot.netflow.model

import org.apache.spark.broadcast.Broadcast
import org.apache.spot.netflow.{FlowWordCreator, FlowWords}
import org.apache.spot.utilities.data.validation.InvalidDataHandler


/**
  * Estimate the probabilities of network events using a [[FlowSuspiciousConnectsModel]]
  *
  * @param timeCuts Quantile cut-offs for binning time-of-day values when forming words from netflow records.
  * @param ibytCuts Quantile cut-offs for binning ibyt values when forming words from netflow records.
  * @param ipktCuts Quantile cut-offs for binning ipkt values when forming words from netflow records.
  * @param topicCount Number of topics used in the topic modelling analysis.
  * @param wordToPerTopicProbBC Broadcast map assigning to each word it's per-topic probabilities.
  *                           Ie. Prob [word | t ] for t = 0 to topicCount -1
  */


class FlowScoreFunction(timeCuts: Array[Double],
                        ibytCuts: Array[Double],
                        ipktCuts: Array[Double],
                        topicCount: Int,
                        wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]]) extends Serializable {


  val flowWordCreator = new FlowWordCreator(timeCuts, ibytCuts, ipktCuts)

  /**
    * Estimate the probability of a netflow connection as distributed from the source IP and from the destination IP
    * and assign it the least of these two values.
    *
    * @param hour Hour of flow record.
    * @param minute Minute of flow record.
    * @param second Second of flow record.
    * @param srcIP Source IP of flow record.
    * @param dstIP Destination IP of flow record.
    * @param srcPort Source port of flow record.
    * @param dstPort Destination port of flow record.
    * @param ipkt ipkt entry of flow record
    * @param ibyt ibyt entry of flow record
    * @param srcTopicMix topic mix assigned of source IP
    * @param dstTopicMix topic mix assigned of destination IP
    * @return Minium of probability of this word from the source IP and probability of this word from the dest IP.
    */
  def score(hour: Int,
            minute: Int,
            second: Int,
            srcIP: String,
            dstIP: String,
            srcPort: Int,
            dstPort: Int,
            ipkt: Long,
            ibyt: Long,
            srcTopicMix: Seq[Double],
            dstTopicMix: Seq[Double]): Double = {


    val FlowWords(srcWord, dstWord) = flowWordCreator.flowWords(hour: Int, minute: Int, second: Int,
      srcPort: Int, dstPort: Int, ipkt: Long, ibyt: Long)

    val zeroProb = Array.fill(topicCount) {
      0.0
    }

    /** WordError indicates there was a problem creating a word and should not be used for scoring.
      * *
      * A null value for srcTopicMix or dstTopicMix indicated the ip (source or dest respectively)
      * were not seen in training.
      */
    if (srcWord == InvalidDataHandler.WordError || dstWord == InvalidDataHandler.WordError) {
      InvalidDataHandler.ScoreError
    } else if (srcTopicMix == null || dstTopicMix == null) {
      0.0
    } else {

      val scoreOfConnectionFromSrcIP = srcTopicMix.zip(wordToPerTopicProbBC.value.getOrElse(srcWord, zeroProb))
        .map({ case (pWordGivenTopic, pTopicGivenDoc) => pWordGivenTopic * pTopicGivenDoc })
        .sum

      val scoreOfConnectionsFromDstIP = dstTopicMix.zip(wordToPerTopicProbBC.value.getOrElse(dstWord, zeroProb))
        .map({ case (pWordGivenTopic, pTopicGivenDoc) => pWordGivenTopic * pTopicGivenDoc })
        .sum

      Math.min(scoreOfConnectionFromSrcIP, scoreOfConnectionsFromDstIP)

    }
  }
}
