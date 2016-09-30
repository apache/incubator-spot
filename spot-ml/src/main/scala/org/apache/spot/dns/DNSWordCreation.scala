package org.apache.spot.dns

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.functions._
import org.apache.spot.utilities.DomainProcessor.{DomainInfo, extractDomainInfo}
import org.apache.spot.utilities.Quantiles


/**
  * Convert DNS log entries into "words" for topic modelling analyses.
  *
  * @param frameLengthCuts
  * @param timeCuts
  * @param subdomainLengthCuts
  * @param entropyCuts
  * @param numberPeriodsCuts
  * @param topDomainsBC
  */
class DNSWordCreation(frameLengthCuts: Array[Double],
                      timeCuts: Array[Double],
                      subdomainLengthCuts: Array[Double],
                      entropyCuts: Array[Double],
                      numberPeriodsCuts: Array[Double],
                      topDomainsBC: Broadcast[Set[String]]) extends Serializable {


  /**
    * Create a new UDF for adding a word column to a dataframe.
    * The dataframe must provide the following columns:
    *
    * A string timestamp, a long unixtimestamp, an integer framelength,
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
    * @param timeStamp     Timestamp as a string.
    * @param unixTimeStamp Unix timestamp as a 64 bit integer
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


    val DomainInfo(domain, topDomain, subdomain, subdomainLength, subdomainEntropy, numPeriods) =
      extractDomainInfo(queryName, topDomainsBC)

    Seq(topDomain,
      Quantiles.bin(frameLength.toDouble, frameLengthCuts),
      Quantiles.bin(unixTimeStamp.toDouble, timeCuts),
      Quantiles.bin(subdomainLength.toDouble, subdomainLengthCuts),
      Quantiles.bin(subdomainEntropy, entropyCuts),
      Quantiles.bin(numPeriods.toDouble, numberPeriodsCuts),
      dnsQueryType,
      dnsQueryRcode).mkString("_")
  }
}


