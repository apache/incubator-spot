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

package org.apache.spot.dns

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.functions._
import org.apache.spot.utilities.DomainProcessor.{DomainInfo, extractDomainInfo}
import org.apache.spot.utilities.{MathUtils, TimeUtilities}
import org.apache.spot.proxy.ProxySuspiciousConnectsModel.EntropyCuts
import org.apache.spot.utilities.data.validation.InvalidDataHandler
import scala.util.{Success, Try}


/**
  * Convert DNS log entries into "words" for topic modelling analyses.
  *
  * @param topDomainsBC List of most popular top level domain names.
  * @param userDomain User's domain for internal network.
  */
class DNSWordCreation(topDomainsBC: Broadcast[Set[String]], userDomain: String) extends Serializable {


  /**
    * Create a new UDF for adding a word column to a dataframe.
    * The dataframe must provide the following columns:
    *
    * A string timeStamp, a long unixtimeStamp, an integer framelength,
    * a string client IP, a string query name, a string query class, an integer query type
    * and an integer query response code.
    *
    * @return A dataframe UDF of signature:
    *         (timeStamp: String, unixTimeStamp: Long, frameLength: Int, clientIP: String,
    *         queryName: String, queryClass: String, dnsQueryType: Int, dnsQueryRCode :Int) =>  Word: String
    */
  def wordCreationUDF =
    udf((timeStamp: String,
         unixTimeStamp: Long,
         frameLength: Int,
         clientIP: String,
         queryName: String,
         queryClass: String,
         dnsQueryType: Int,
         dnsQueryRcode: Int) => dnsWord(timeStamp,
      unixTimeStamp,
      frameLength,
      clientIP,
      queryName,
      queryClass,
      dnsQueryType,
      dnsQueryRcode))


  /**
    * Simplify a DNS log entry into a word.
    *
    * @param timeStamp     Time stamp as a string.
    * @param unixTimeStamp Unix time stamp as a 64 bit integer
    * @param frameLength   Framelength as an integer.
    * @param clientIP      IP of client making DNS query as string.
    * @param queryName     URL being queried.
    * @param queryClass    Query class as string.
    * @param dnsQueryType  Query type as integer.
    * @param dnsQueryRcode Query response code as integer.
    * @return The word representation of the DNS entry.
    */

  def dnsWord(timeStamp: String,
              unixTimeStamp: Long,
              frameLength: Int,
              clientIP: String,
              queryName: String,
              queryClass: String,
              dnsQueryType: Int,
              dnsQueryRcode: Int): String = {

    Try {
      val DomainInfo(domain, topDomain, subdomain, subdomainLength, subdomainEntropy, numPeriods) =
        extractDomainInfo(queryName, topDomainsBC, userDomain)

      Seq(topDomain,
        MathUtils.logBaseXInt(frameLength.toDouble, 2),
        TimeUtilities.getTimeAsHour(timeStamp.split(" +")(3)).toString,
        MathUtils.logBaseXInt(subdomainLength.toDouble, 2),
        MathUtils.bin(subdomainEntropy, EntropyCuts),
        MathUtils.logBaseXInt(numPeriods.toDouble, 2),
        dnsQueryType,
        dnsQueryRcode).mkString("_")
    } match {
      case Success(word) => word
      case _ => InvalidDataHandler.WordError
    }

  }
}


