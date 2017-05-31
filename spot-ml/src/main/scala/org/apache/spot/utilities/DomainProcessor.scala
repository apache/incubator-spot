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

package org.apache.spot.utilities

import org.apache.spark.broadcast.Broadcast

/**
  * Routines and data for processing URLs for domains, subdomains, country code, top-level domains, etc.
  */
object DomainProcessor extends Serializable {

  val CountryCodes = Set("ac", "ad", "ae", "af", "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "as", "at", "au",
    "aw", "ax", "az", "ba", "bb", "bd", "be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "bq", "br", "bs", "bt",
    "bv", "bw", "by", "bz", "ca", "cc", "cd", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cr", "cu", "cv",
    "cw", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "ee", "eg", "eh", "er", "es", "et", "eu", "fi",
    "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", "gh", "gi", "gl", "gm", "gn", "gp", "gq", "gr",
    "gs", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie", "il", "im", "in", "io", "iq", "ir",
    "is", "it", "je", "jm", "jo", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "krd", "kw", "ky", "kz", "la",
    "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md", "me", "mg", "mh", "mk", "ml", "mm",
    "mn", "mo", "mp", "mq", "mr", "ms", "mt", "mu", "mv", "mw", "mx", "my", "mz", "na", "nc", "ne", "nf", "ng", "ni",
    "nl", "no", "np", "nr", "nu", "nz", "om", "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "ps", "pt",
    "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "", "sk",
    "sl", "sm", "sn", "so", "sr", "ss", "st", "su", "sv", "sx", "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk",
    "tl", "tm", "tn", "to", "tp", "tr", "tt", "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "uz", "va", "vc", "ve",
    "vg", "vi", "vn", "vu", "wf", "ws", "ye", "yt", "za", "zm", "zw")

  val TopLevelDomainNames = Set("com", "org", "net", "int", "edu", "gov", "mil")
  val None = "None"

  /**
    * Extract domain info from a url.
    * @param url           Incoming url.
    * @param topDomainsBC  Broadcast variable containing the top domains set.
    * @param userDomain    Domain of the spot user (example:'intel').
    * @return New [[DomainInfo]] object containing extracted domain information.
    */
  def extractDomainInfo(url: String, topDomainsBC: Broadcast[Set[String]], userDomain: String): DomainInfo = {

    val spliturl = url.split('.')
    val numParts = spliturl.length

    val (domain, subdomain) = extractDomainSubdomain(url)


    val subdomainLength = if (subdomain != None) {
      subdomain.length
    } else {
      0
    }

    val topDomainClass = if (userDomain != "" && domain == userDomain) {
      2
    } else if (topDomainsBC.value contains domain) {
      1
    } else {
      0
    }

    val subdomainEntropy = if (subdomain != "None") Entropy.stringEntropy(subdomain) else 0d

    DomainInfo(domain, topDomainClass, subdomain, subdomainLength, subdomainEntropy, numParts)
  }

  /**
    *
    * @param url Url from which to extract domain.
    * @return Domain name or "None" if there is none.
    */
  def extractDomain(url: String) : String = {
    val (domain, _) = extractDomainSubdomain(url)
    domain
  }

  /**
    * Extrat the domain and subdomain from a URL.
    * @param url URL to be parsed.
    * @return Pair of (domain, subdomain). If there is no domain, both fields contain "None".
    *         If there is no subdomain then the subdomain field is "None"
    */
  def extractDomainSubdomain(url: String) : (String, String) = {
    val spliturl = url.split('.')
    val numParts = spliturl.length


    var domain = None
    var subdomain = None
    // First check if query is an IP address e.g.: 123.103.104.10.in-addr.arpa or a name.
    // Such URLs receive a domain of NO_DOMAIN

     if (numParts >= 2
       && !(numParts > 2 && spliturl(numParts - 1) == "arpa" && spliturl(numParts - 2) == "in-addr")
       && (CountryCodes.contains(spliturl.last) || TopLevelDomainNames.contains(spliturl.last))) {
       val strippedSplitURL = removeTopLevelDomainName(removeCountryCode(spliturl))
       if (strippedSplitURL.length > 0) {
         domain = strippedSplitURL.last
         if (strippedSplitURL.length > 1) {
           subdomain = strippedSplitURL.slice(0, strippedSplitURL.length - 1).mkString(".")
         }
       }
     }


    (domain, subdomain)
    }

  /**
    * Strip the country code from a split URL.
    * @param urlComponents Array of the entries of a URL after splitting on periods.
    * @return URL components with the country code stripped.
    */
  def removeCountryCode(urlComponents: Array[String]): Array[String] = {
    if (CountryCodes.contains(urlComponents.last)) {
      urlComponents.dropRight(1)
    } else {
      urlComponents
    }
  }

  /**
    * Strip the top-level domain name from a split URL.
    * @param urlComponents Array of the entries ofa  URL after splitting on periods.
    * @return URL components with the top-level domain name stripped.
    */
  def removeTopLevelDomainName(urlComponents: Array[String]): Array[String] = {
    if (TopLevelDomainNames.contains(urlComponents.last)) {
      urlComponents.dropRight(1)
    } else {
      urlComponents
    }
  }

  /**
    * Commonly extracted domain features.
    *
    * @param domain           Domain (if any) of a url.
    * @param topDomain        Numerical class of domain: 2 for Intel, 1 for Alexa top domains, 0 for others.
    * @param subdomain        Subdomain (if any) in the url.
    * @param subdomainLength  Length of the subdomain. 0 if there is none.
    * @param subdomainEntropy Entropy of the subdomain viewed as a distribution on its character set.
    *                         0 if there is no subdomain.
    * @param numPeriods       Number of periods + 1 in the url. (Number of sub-strings where url is split by periods.)
    */
  case class DomainInfo(domain: String,
                        topDomain: Int,
                        subdomain: String,
                        subdomainLength: Int,
                        subdomainEntropy: Double,
                        numPeriods: Int)
}
