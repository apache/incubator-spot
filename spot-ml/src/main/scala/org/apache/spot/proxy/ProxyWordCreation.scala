package org.apache.spot.proxy

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.functions._
import org.apache.spot.utilities.{Entropy, Quantiles, DomainProcessor, TimeUtilities}


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

    List(topDomain(proxyHost, topDomains.value).toString,
      Quantiles.bin(TimeUtilities.getTimeAsDouble(time), timeCuts).toString,
      reqMethod,
      Quantiles.bin(Entropy.stringEntropy(uri), entropyCuts),
      if (contentType.split('/').length > 0) contentType.split('/')(0) else "unknown_content_type",
          // just the top level content type for now
      Quantiles.bin(agentCounts.value(userAgent), agentCuts),
      responseCode(0)).mkString("_")
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
