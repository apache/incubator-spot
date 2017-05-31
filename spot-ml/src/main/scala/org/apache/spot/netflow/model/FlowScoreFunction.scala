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

package org.apache.spot.netflow.model

import org.apache.spark.broadcast.Broadcast
import org.apache.spot.SuspiciousConnectsScoreFunction
import org.apache.spot.netflow.{FlowWordCreator, FlowWords}
import org.apache.spot.utilities.FloatPointPrecisionUtility


/**
  * Estimate the probabilities of network events using a [[FlowSuspiciousConnectsModel]]
  *
  * @param topicCount Number of topics used in the topic modelling analysis.
  * @param wordToPerTopicProbBC Broadcast map assigning to each word it's per-topic probabilities.
  *                           Ie. Prob [word | t ] for t = 0 to topicCount -1
  */


class FlowScoreFunction(topicCount: Int,
                        wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]]) extends Serializable {

  val suspiciousConnectsScoreFunction =
    new SuspiciousConnectsScoreFunction(topicCount, wordToPerTopicProbBC)

  /**
    * Estimate the probability of a netflow connection as distributed from the source IP and from the destination IP
    * and assign it the least of these two values.
    *
    * @param hour Hour of flow record.
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
  def score[P <: FloatPointPrecisionUtility](precisionUtility: P)(hour: Int,
                                                                  srcIP: String,
                                                                  dstIP: String,
                                                                  srcPort: Int,
                                                                  dstPort: Int,
                                                                  protocol: String,
                                                                  ibyt: Long,
                                                                  ipkt: Long,
                                                                  srcTopicMix: Seq[precisionUtility.TargetType],
                                                                  dstTopicMix: Seq[precisionUtility.TargetType]): Double = {

    val FlowWords(srcWord, dstWord) = FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyt, ipkt)

    val scoreOfConnectionFromSrcIP = suspiciousConnectsScoreFunction.score(precisionUtility)(srcTopicMix, srcWord)
    val scoreOfConnectionsFromDstIP = suspiciousConnectsScoreFunction.score(precisionUtility)(dstTopicMix, dstWord)

    Math.min(scoreOfConnectionFromSrcIP, scoreOfConnectionsFromDstIP)
  }
}
