package org.apache.spot.proxy

import org.apache.spark.sql.types._

/**
  * Data frame column names used in the proxy suspicious connects analysis.
  */
object ProxySchema {

  // fields from the input

  val Date = "p_date"
  val DateField = StructField(Date, StringType, nullable = true)

  val Time = "p_time"
  val TimeField = StructField(Time, StringType, nullable = true)

  val ClientIP = "clientip"
  val ClientIPField = StructField(ClientIP, StringType, nullable = true)

  val Host = "host"
  val HostField = StructField(Host, StringType, nullable = true)

  val ReqMethod = "reqmethod"
  val ReqMethodField = StructField(ReqMethod, StringType, nullable = true)

  val UserAgent = "useragent"
  val UserAgentField = StructField(UserAgent, StringType, nullable = true)

  val ResponseContentType = "resconttype"
  val ResponseContentTypeField = StructField(ResponseContentType, StringType, nullable = true)

  val Duration = "duration"
  val DurationField = StructField(Duration, IntegerType, nullable = true)

  val UserName = "username"
  val UserNameField = StructField(UserName, StringType, nullable = true)

  val AuthGroup = "authgroup"

  val ExceptionId = "exceptionid"

  val FilterResult = "filterresult"

  val WebCat = "webcat"
  val WebCatField = StructField(WebCat, StringType, nullable = true)

  val Referer = "referer"
  val RefererField = StructField(Referer, StringType, nullable = true)

  val RespCode = "respcode"
  val RespCodeField = StructField(RespCode, StringType, nullable = true)

  val Action = "action"

  val URIScheme = "urischeme"

  val URIPort = "uriport"
  val URIPortField = StructField(URIPort, StringType, nullable = true)

  val URIPath = "uripath"
  val URIPathField = StructField(URIPath, StringType, nullable = true)

  val URIQuery = "uriquery"
  val URIQueryField = StructField(URIQuery, StringType, nullable = true)

  val URIExtension = "uriextension"

  val ServerIP = "serverip"
  val ServerIPField = StructField(ServerIP, StringType, nullable = true)

  val SCBytes = "scbytes"
  val SCBytesField = StructField(SCBytes, IntegerType, nullable = true)

  val CSBytes = "csbytes"
  val CSBytesField = StructField(CSBytes, IntegerType, nullable = true)

  val VirusID = "virusid"
  val BcappName = "bcappname"
  val BcappOper = "bcappoper"

  val FullURI = "fulluri"
  val FullURIField = StructField(FullURI, StringType, nullable = true)

  // output fields

  val Word = "word"
  val WordField = StructField(Word, StringType, nullable = true)

  val Score = "score"
  val ScoreField = StructField(Score, DoubleType, nullable = true)
}
