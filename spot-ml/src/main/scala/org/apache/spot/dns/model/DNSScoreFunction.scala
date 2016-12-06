package org.apache.spot.dns.model

import org.apache.spark.broadcast.Broadcast
import org.apache.spot.SuspiciousConnectsScoreFunction
import org.apache.spot.dns.DNSWordCreation


/**
  * Estimate the probabilities of network events using a [[DNSSuspiciousConnectsModel]]
  *
  * @param frameLengthCuts Delimeters used to define binning for frame length field
  * @param timeCuts Delimeters used to define binning for time field
  * @param subdomainLengthCuts Delimeters used to define binning for subdomain length field
  * @param entropyCuts Delimeters used to define binning for entropy field
  * @param numberPeriodsCuts Delimeters used to define binning for number of periods of subdomain field
  * @param topicCount Number of topics used for the LDA model
  * @param ipToTopicMixBC Topic mixes learned by the LDA model for each IP in the data
  * @param wordToPerTopicProbBC Word mixes for each of the topics learned by the LDA model
  * @param topDomainsBC Alexa top one million list of domains.
  * @param userDomain Domain associated to network data (example: 'intel')
  */
class DNSScoreFunction(frameLengthCuts: Array[Double],
                       timeCuts: Array[Double],
                       subdomainLengthCuts: Array[Double],
                       entropyCuts: Array[Double],
                       numberPeriodsCuts: Array[Double],
                       topicCount: Int,
                       ipToTopicMixBC: Broadcast[Map[String, Array[Double]]],
                       wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]],
                       topDomainsBC: Broadcast[Set[String]],
                       userDomain: String) extends Serializable {


  val suspiciousConnectsScoreFunction =
    new SuspiciousConnectsScoreFunction(topicCount, ipToTopicMixBC, wordToPerTopicProbBC)

  val dnsWordCreator = new DNSWordCreation(frameLengthCuts,
                                           timeCuts,
                                           subdomainLengthCuts,
                                           entropyCuts,
                                           numberPeriodsCuts,
                                           topDomainsBC,
                                           userDomain)

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