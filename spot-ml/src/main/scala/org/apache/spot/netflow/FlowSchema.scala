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

package org.apache.spot.netflow

import org.apache.spark.sql.types._

/**
  * Data frame schemas and column names used in the netflow suspicious connects analysis.
  */

object FlowSchema {

  // input fields

  val TimeReceived = "treceived"
  val TimeReceivedField = StructField(TimeReceived, StringType, nullable = true)

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
  val DurationField = StructField(Duration, DoubleType, nullable = true)

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

  val Ipkt = "ipkt"
  val IpktField = StructField(Ipkt, LongType, nullable = true)

  val Ibyt = "ibyt"
  val IbytField = StructField(Ibyt, LongType, nullable = true)

  val Opkt = "opkt"
  val OpktField = StructField(Opkt, LongType, nullable = true)

  val Obyt = "obyt"
  val ObytField = StructField(Obyt, LongType, nullable = true)


  // intermediate fields

  val SrcIpTopicMix  = "source_ip_topic_mix"
  val DstIpTopicMix  = "destination_ip_topic_mix"


  // output fields

  val SourceWord = "source_word"
  val DestinationWord = "destination_word"
  val Score = "score"
  val ScoreField = StructField(Score, DoubleType)
}
