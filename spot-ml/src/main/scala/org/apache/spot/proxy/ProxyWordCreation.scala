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

package org.apache.spot.proxy

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.functions._
import org.apache.spot.utilities.data.validation.InvalidDataHandler
import org.apache.spot.utilities.{DomainProcessor, Entropy, Quantiles, TimeUtilities}

import scala.util.{Success, Try}


object ProxyWordCreation {

  def udfWordCreation(topDomains : Broadcast[Set[String]],
                      agentCounts : Broadcast[Map[String, Long]],
                      timeCuts: Array[Double],
                      entropyCuts: Array[Double],
                      agentCuts: Array[Double]) =
    udf((host: String, time: String, reqMethod: String, uri: String, contentType: String, userAgent: String, responseCode: String) =>
      ProxyWordCreation.proxyWord(host,
        time,
        reqMethod,
        uri,
        contentType,
        userAgent,
        responseCode,
        topDomains,
        agentCounts,
        timeCuts,
        entropyCuts,
        agentCuts))


  def proxyWord(proxyHost: String,
                time: String,
                reqMethod: String,
                uri: String,
                contentType: String,
                userAgent: String,
                responseCode: String,
                topDomains: Broadcast[Set[String]],
                agentCounts: Broadcast[Map[String, Long]],
                timeCuts: Array[Double],
                entropyCuts: Array[Double],
                agentCuts: Array[Double]): String = {
    Try{
      List(topDomain(proxyHost, topDomains.value).toString,
        Quantiles.bin(TimeUtilities.getTimeAsDouble(time), timeCuts).toString,
        reqMethod,
        Quantiles.bin(Entropy.stringEntropy(uri), entropyCuts),
        if (contentType.split('/').length > 0) contentType.split('/')(0) else "unknown_content_type",
        // just the top level content type for now
        Quantiles.bin(agentCounts.value(userAgent), agentCuts),
        if (responseCode != null) responseCode(0) else "unknown_response_code").mkString("_")
    } match {
      case Success(proxyWord) => proxyWord
      case _ => InvalidDataHandler.WordError
    }
  }


  def topDomain(proxyHost: String, topDomains: Set[String]): Int = {

    val domain = DomainProcessor.extractDomain(proxyHost)

    if (domainBelongsToSafeList(domain)) {
      2
    } else if (topDomains.contains(domain)) {
      1
    } else {
      0
    }
  }

  def domainBelongsToSafeList(domain: String) = domain == "intel" // TBD parameterize this!

}
