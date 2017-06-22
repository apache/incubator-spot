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
import org.apache.spot.proxy.ProxySuspiciousConnectsModel.EntropyCuts
import org.apache.spot.utilities._
import org.apache.spot.utilities.data.validation.InvalidDataHandler

import scala.util.{Success, Try}

/**
  * Convert Proxy log entries into "words" for topic modelling analyses.
  */
object ProxyWordCreation {

  /**
    * UDF for word creation
    *
    * @param topDomains  List of most popular top level domain names (provided)
    * @param agentCounts List of user agent values in the data set and its count
    * @return
    */
  def udfWordCreation(topDomains: Broadcast[Set[String]],
                      agentCounts: Broadcast[Map[String, Long]]) =
    udf((host: String, time: String, reqMethod: String, uri: String, contentType: String, userAgent: String, responseCode: String) =>
      ProxyWordCreation.proxyWord(host,
        time,
        reqMethod,
        uri,
        contentType,
        userAgent,
        responseCode,
        topDomains,
        agentCounts))

  /**
    * Creates a word based on values of Proxy record
    *
    * @param proxyHost    Host name
    * @param time         Proxy connection time
    * @param reqMethod    request method
    * @param uri          URI
    * @param contentType  content type
    * @param userAgent    user agent
    * @param responseCode response code
    * @param topDomains   top domains
    * @param agentCounts  agent counts
    * @return
    */
  def proxyWord(proxyHost: String,
                time: String,
                reqMethod: String,
                uri: String,
                contentType: String,
                userAgent: String,
                responseCode: String,
                topDomains: Broadcast[Set[String]],
                agentCounts: Broadcast[Map[String, Long]]): String = {
    Try {
      List(topDomain(proxyHost, topDomains.value).toString,
        // Time binned by hours
        TimeUtilities.getTimeAsHour(time).toString,
        reqMethod,
        // Fixed cutoffs
        MathUtils.bin(Entropy.stringEntropy(uri), EntropyCuts),
        // Just the top level content type for now
        if (contentType.split('/').length > 0) contentType.split('/')(0) else "unknown_content_type",
        // Exponential cutoffs base 2
        MathUtils.logBaseXInt(agentCounts.value(userAgent), 2),
        // Exponential cutoffs base 2
        MathUtils.logBaseXInt(uri.length(), 2),
        // Response code using all 3 digits
        if (responseCode != null) responseCode else "unknown_response_code").mkString("_")
    } match {
      case Success(proxyWord) => proxyWord
      case _ => InvalidDataHandler.WordError
    }
  }


  /**
    * Classifies proxy host domain based on popular domains
    *
    * @param proxyHost  host name
    * @param topDomains list of top domains
    * @return
    */
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

  /**
    * Defines if a domain is safe or not based on a known domain
    *
    * @param domain domain name
    * @return returns true if the domains passed is equal to user domain.
    */
  def domainBelongsToSafeList(domain: String): Boolean = domain == "intel" // TBD parameterize this!

}
