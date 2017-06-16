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
import org.apache.spot.utilities.FloatPointPrecisionUtility


/**
  * Estimate the probabilities of network events using a [[DNSSuspiciousConnectsModel]]
  *
  * @param topicCount           Number of topics used for the LDA model
  * @param wordToPerTopicProbBC Word mixes for each of the topics learned by the LDA model
  * @param topDomainsBC         Alexa top one million list of domains.
  * @param userDomain           Domain associated to network data (example: 'intel')
  */
class DNSScoreFunction(topicCount: Int,
                       wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]],
                       topDomainsBC: Broadcast[Set[String]],
                       userDomain: String) extends Serializable {


  val suspiciousConnectsScoreFunction =
    new SuspiciousConnectsScoreFunction(topicCount, wordToPerTopicProbBC)

  val dnsWordCreator = new DNSWordCreation(topDomainsBC, userDomain)

  def score[P <: FloatPointPrecisionUtility](precisionUtility: P)(timeStamp: String,
                                                                  unixTimeStamp: Long,
                                                                  frameLength: Int,
                                                                  clientIP: String,
                                                                  queryName: String,
                                                                  queryClass: String,
                                                                  queryType: Int,
                                                                  queryResponseCode: Int,
                                                                  documentTopicMix: Seq[precisionUtility.TargetType]): Double = {

    val word = dnsWordCreator.dnsWord(timeStamp,
      unixTimeStamp,
      frameLength,
      clientIP,
      queryName,
      queryClass,
      queryType,
      queryResponseCode)

    suspiciousConnectsScoreFunction.score(precisionUtility)(documentTopicMix, word)
  }
}