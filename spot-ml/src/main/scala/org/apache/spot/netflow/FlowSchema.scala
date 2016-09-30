package org.apache.spot.netflow

object FlowSchema {

  // input fields

  val TimeReceived = "treceived"
  val UnixTimestamp = "unix_tstamp"
  val Year = "tryear"
  val Month = "trmonth"
  val Day = "trday"
  val Hour = "trhour"
  val Minute = "trminute"
  val Second = "trsec"
  val Duration = "tdur"
  val SourceIP = "sip"
  val DestinationIP = "dip"
  val SourcePort = "sport"
  val DestinationPort = "dport"
  val proto = "proto"
  val Flag = "flag"
  val fwd = "fwd"
  val stos = "stos"
  val ipkt = "ipkt"
  val ibyt = "ibyt"
  val opkt = "opkt"
  val obyt = "obyt"
  val input = "input"
  val output = "output"
  val sas = "sas"
  val das = "das"
  val dtos = "dtos"
  val dir = "dir"
  val rip = "rip"

  // derived and intermediate fields
  val NumTime = "num_time"
  val IBYTBin = "ibyt_bin"
  val IPKTBin = "ipkt_bin"
  val TimeBin = "time_bin"
  val PortWord = "port_word"
  val IpPair = "ip_pair"
  val SourceProbabilities = "sourceProb"
  val DestinationProbabilities = "destProb"

  // temporary fields
  val Probabilities = "probabilities"
  val Doc = "doc"


  // output fields

  val SourceWord = "source_word"
  val DestinationWord = "destination_word"
  val SourceScore = "source_score"
  val DestinationScore = "destination_score"
  val MinimumScore = "min_score"
}
