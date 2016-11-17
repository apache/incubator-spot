package org.apache.spot.netflow.model

import org.apache.spark.broadcast.Broadcast
import org.apache.spot.SuspiciousConnectsScoreFunction
import org.apache.spot.netflow.{FlowWordCreator, FlowWords}


/**
  * Estimate the probabilities of network events using a [[FlowSuspiciousConnectsModel]]
  * @param timeCuts
  * @param ibytCuts
  * @param ipktCuts
  * @param topicCount
  * @param wordToPerTopicProbBC
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
    * @param hour
    * @param minute
    * @param second
    * @param srcIP
    * @param dstIP
    * @param srcPort
    * @param dstPort
    * @param ipkt
    * @param ibyt
    * @param srcTopicMix
    * @param dstTopicMix
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

    val zeroProb = Array.fill(topicCount) { 0.0 }

    /** A null value for srcTopicMix or dstTopicMix indicated the ip (source or dest respectively)
      * were not seen in training.
      */
    if (srcTopicMix == null || dstTopicMix == null) {
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
