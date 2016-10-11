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
  * @param ipToTopicMixBC
  * @param wordToPerTopicProbBC
  */

class FlowScoreFunction(timeCuts: Array[Double],
                        ibytCuts: Array[Double],
                        ipktCuts: Array[Double],
                       topicCount: Int,
                       ipToTopicMixBC: Broadcast[Map[String, Array[Double]]],
                       wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]]) extends Serializable {


  val suspiciousConnectsScoreFunction =
    new SuspiciousConnectsScoreFunction(topicCount, ipToTopicMixBC, wordToPerTopicProbBC)

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
            ibyt: Long): Double = {

    val FlowWords(srcWord, dstWord) = flowWordCreator.flowWords(hour: Int,
      minute: Int,
      second: Int,
      srcIP: String,
      dstIP: String,
      srcPort: Int,
      dstPort: Int,
      ipkt: Long,
      ibyt: Long)

    val srcScore = suspiciousConnectsScoreFunction.score(srcIP, srcWord)
    val dstScore = suspiciousConnectsScoreFunction.score(dstIP, dstWord)

    Math.min(srcScore,dstScore)
  }
}