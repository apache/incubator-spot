package org.apache.spot.dns.sideinformation

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row
import org.apache.spot.dns.DNSWordCreation
import org.apache.spot.utilities.DomainProcessor.{DomainInfo, extractDomainInfo}

/**
  * Add side information for OA to a dataframe.
 *
  * @param fieldNames
  * @param frameLengthCuts
  * @param timeCuts
  * @param subdomainLengthCuts
  * @param entropyCuts
  * @param numberPeriodsCuts
  * @param topDomainsBC
  */

class DNSSideInformationFunction(fieldNames: Array[String],
                                 frameLengthCuts: Array[Double],
                                 timeCuts: Array[Double],
                                 subdomainLengthCuts: Array[Double],
                                 entropyCuts: Array[Double],
                                 numberPeriodsCuts: Array[Double],
                                 topDomainsBC: Broadcast[Set[String]]) extends Serializable {

  val dnsWordCreator = new DNSWordCreation(frameLengthCuts, timeCuts, subdomainLengthCuts, entropyCuts, numberPeriodsCuts, topDomainsBC)


  def getSideFields(row: Row,
                    timeStampCol: String,
                    unixTimeStampCol: String,
                    frameLengthCol: String,
                    clientIPCol: String,
                    queryNameCol: String,
                    queryClassCol: String,
                    dnsQueryTypeCol: String,
                    dnsQueryRCodeCol: String): Seq[Any] = {


    val timeStamp = row.getString(fieldNames.indexOf(timeStampCol))
    val unixTimeStamp = row.getLong(fieldNames.indexOf(unixTimeStampCol))
    val frameLength = row.getInt(fieldNames.indexOf(frameLengthCol))
    val clientIP = row.getString(fieldNames.indexOf(clientIPCol))
    val queryName = row.getString(fieldNames.indexOf(queryNameCol))
    val queryClass = row.getString(fieldNames.indexOf(queryClassCol))
    val dnsQueryType = row.getInt(fieldNames.indexOf(dnsQueryTypeCol))
    val dnsQueryRcode = row.getInt(fieldNames.indexOf(dnsQueryRCodeCol))

    val DomainInfo(domain, topDomain, subdomain, subdomainLength, subdomainEntropy, numPeriods) =
      extractDomainInfo(queryName, topDomainsBC)


    val word = dnsWordCreator.dnsWord(timeStamp,
      unixTimeStamp,
      frameLength,
      clientIP,
      queryName,
      queryClass,
      dnsQueryType,
      dnsQueryRcode)

    Seq(domain, subdomain, subdomainLength, subdomainEntropy, topDomain, numPeriods, word)
  }
}
