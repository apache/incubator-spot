package org.apache.spot.dns.model

import org.apache.spark.broadcast.Broadcast
import org.apache.spot.SuspiciousConnectsScoreFunction
import org.apache.spot.dns.DNSWordCreation


/**
  * Estimate the probabilities of network events using a [[DNSSuspiciousConnectsModel]]
  *
  * @param frameLengthCuts
  * @param timeCuts
  * @param subdomainLengthCuts
  * @param entropyCuts
  * @param numberPeriodsCuts
  * @param topicCount
  * @param ipToTopicMixBC
  * @param wordToPerTopicProbBC
  * @param topDomainsBC
  */
class DNSScoreFunction(frameLengthCuts: Array[Double],
                       timeCuts: Array[Double],
                       subdomainLengthCuts: Array[Double],
                       entropyCuts: Array[Double],
                       numberPeriodsCuts: Array[Double],
                       topicCount: Int,
                       ipToTopicMixBC: Broadcast[Map[String, Array[Double]]],
                       wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]],
                       topDomainsBC: Broadcast[Set[String]]) extends Serializable {


  val suspiciousConnectsScoreFunction =
    new SuspiciousConnectsScoreFunction(topicCount, ipToTopicMixBC, wordToPerTopicProbBC)

  val dnsWordCreator = new DNSWordCreation(frameLengthCuts, timeCuts, subdomainLengthCuts, entropyCuts, numberPeriodsCuts, topDomainsBC)

  def score(timeStamp: String,
            unixTimeStamp: Long,
            frameLength: Int,
            clientIP: String,
            queryName: String,
            queryClass: String,
            queryType: Int,
            queryResponseCode: Int): Double = {

    val word = dnsWordCreator.dnsWord(timeStamp,
      unixTimeStamp,
      frameLength,
      clientIP,
      queryName,
      queryClass,
      queryType,
      queryResponseCode)

    suspiciousConnectsScoreFunction.score(clientIP, word)
  }
}