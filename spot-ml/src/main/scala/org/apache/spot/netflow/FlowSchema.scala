package org.apache.spot.netflow

import org.apache.spark.sql.types._

/**
  * Data frame schemas and column names used in the netflow suspicious connects analysis.
  */

object FlowSchema {

  // input fields

  val TimeReceived = "treceived"
  val TimeReceivedField = StructField(TimeReceived, StringType, nullable = true)

  val UnixTimestamp = "unix_tstamp"
  val UnixTimestampField = StructField(UnixTimestamp, LongType, nullable = true)

  val Year = "tryear"
  val YearField = StructField(Year, IntegerType, nullable = true)

  val Month = "trmonth"
  val MonthField = StructField(Month, IntegerType, nullable = true)

  val Day = "trday"
  val DayField = StructField(Day, IntegerType, nullable = true)

  val Hour = "trhour"
  val HourField = StructField(Hour, IntegerType, nullable = true)

  val Minute = "trminute"
  val MinuteField = StructField(Minute, IntegerType, nullable = true)

  val Second = "trsec"
  val SecondField = StructField(Second, IntegerType, nullable = true)

  val Duration = "tdur"
  val DurationField = StructField(Duration, FloatType, nullable = true)

  val SourceIP = "sip"
  val SourceIPField = StructField(SourceIP, StringType, nullable = true)

  val DestinationIP = "dip"
  val DestinationIPField = StructField(DestinationIP, StringType, nullable = true)

  val SourcePort = "sport"
  val SourcePortField = StructField(SourcePort, IntegerType, nullable = true)

  val DestinationPort = "dport"
  val DestinationPortField = StructField(DestinationPort, IntegerType, nullable = true)

  val Protocol = "proto"
  val ProtocolField = StructField(Protocol, StringType, nullable = true)

  val Flag = "flag"
  val FlagField = StructField(Flag, StringType, nullable = true)

  val Fwd = "fwd"
  val FwdField = StructField(Fwd, IntegerType, nullable = true)

  val Stos = "stos"
  val StosField = StructField(Stos, IntegerType, nullable = true)

  val Ipkt = "ipkt"
  val IpktField = StructField(Ipkt, LongType, nullable = true)

  val Ibyt = "ibyt"
  val IbytField = StructField(Ibyt, LongType, nullable = true)

  val Opkt = "opkt"
  val OpktField = StructField(Opkt, LongType, nullable = true)

  val Obyt = "obyt"
  val ObytField = StructField(Obyt, LongType, nullable = true)

  val Input = "input"
  val InputField = StructField(Input, IntegerType, nullable = true)

  val Output = "output"
  val OutputField = StructField(Output, IntegerType, nullable = true)

  val Sas = "sas"
  val SasField = StructField(Sas, IntegerType, nullable = true)

  val Das = "das"
  val DasField = StructField(Das, IntegerType, nullable = true)

  val Dtos = "dtos"
  val DtosField = StructField(Dtos, IntegerType, nullable = true)

  val Dir = "dir"
  val DirField = StructField(Dir, IntegerType, nullable = true)

  val Rip = "rip"
  val RipField = StructField(Rip, StringType, nullable = true)

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
  val Score = "score"
  val ScoreField = StructField(Score, DoubleType)
}
