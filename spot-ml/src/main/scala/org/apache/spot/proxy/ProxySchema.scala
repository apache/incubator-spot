package org.apache.spot.proxy

/**
  * Data frame column names used in the proxy suspicious connects analysis.
  */
object ProxySchema {

  // fields from the input

  val Date = "p_date"
  val Time = "p_time"
  val ClientIP = "clientip"
  val Host = "host"
  val ReqMethod = "reqmethod"
  val UserAgent = "useragent"
  val ResponseContentType = "resconttype"
  val Duration = "duration"
  val UserName = "username"
  val AuthGroup = "authgroup"
  val ExceptionId = "exceptionid"
  val FilterResult = "filterresult"
  val WebCat = "webcat"
  val Referer = "referer"
  val RespCode = "respcode"
  val Action = "action"
  val URIScheme = "urischeme"
  val URIPort = "uriport"
  val URIPath = "uripath"
  val URIQuery = "uriquery"
  val URIExtension = "uriextension"
  val ServerIP = "serverip"
  val SCBytes = "scbytes"
  val CSBytes = "csbytes"
  val VirusID = "virusid"
  val BcappName = "bcappname"
  val BcappOper = "bcappoper"
  val FullURI = "fulluri"

  // output fields

  val Word = "word"
  val Score = "score"
}
