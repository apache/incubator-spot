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

package org.apache.spot.dns.model

import org.apache.spark.broadcast.Broadcast
import org.apache.spot.SuspiciousConnectsScoreFunction
import org.apache.spot.dns.DNSWordCreation
import org.apache.spot.utilities.transformation.ProbabilityConverter


/**
  * Estimate the probabilities of network events using a [[DNSSuspiciousConnectsModel]]
  *
  * @param frameLengthCuts      Delimeters used to define binning for frame length field
  * @param timeCuts             Delimeters used to define binning for time field
  * @param subdomainLengthCuts  Delimeters used to define binning for subdomain length field
  * @param entropyCuts          Delimeters used to define binning for entropy field
  * @param numberPeriodsCuts    Delimeters used to define binning for number of periods of subdomain field
  * @param topicCount           Number of topics used for the LDA model
  * @param wordToPerTopicProbBC Word mixes for each of the topics learned by the LDA model
  * @param topDomainsBC         Alexa top one million list of domains.
  * @param userDomain           Domain associated to network data (example: 'intel')
  */
class DNSScoreFunction(frameLengthCuts: Array[Double],
                       timeCuts: Array[Double],
                       subdomainLengthCuts: Array[Double],
                       entropyCuts: Array[Double],
                       numberPeriodsCuts: Array[Double],
                       topicCount: Int,
                       wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]],
                       topDomainsBC: Broadcast[Set[String]],
                       userDomain: String) extends Serializable {


  val suspiciousConnectsScoreFunction =
    new SuspiciousConnectsScoreFunction(topicCount, wordToPerTopicProbBC)

  val dnsWordCreator = new DNSWordCreation(frameLengthCuts,
    timeCuts,
    subdomainLengthCuts,
    entropyCuts,
    numberPeriodsCuts,
    topDomainsBC,
    userDomain)

  def score[P <: ProbabilityConverter](probabilityConversionOption: P)(timeStamp: String,
                                                                       unixTimeStamp: Long,
                                                                       frameLength: Int,
                                                                       clientIP: String,
                                                                       queryName: String,
                                                                       queryClass: String,
                                                                       queryType: Int,
                                                                       queryResponseCode: Int,
                                                                       documentTopicMix: Seq[probabilityConversionOption.ScalingType]): Double = {

    val word = dnsWordCreator.dnsWord(timeStamp,
      unixTimeStamp,
      frameLength,
      clientIP,
      queryName,
      queryClass,
      queryType,
      queryResponseCode)

    suspiciousConnectsScoreFunction.score(probabilityConversionOption)(documentTopicMix, word)
  }
}