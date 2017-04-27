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

import org.apache.spark.sql.functions._
import org.apache.spot.utilities.Quantiles
import org.apache.spot.utilities.data.validation.InvalidDataHandler

import scala.util.{Success, Try}


/**
  * Pair of netflow words extracted from a netflow record - one for the source IP and one for the destination IP.
  *
  * @param srcWord The word summarizing the communication from the POV of the source IP.
  * @param dstWord The word summarizing the communication from the POV of the destination IP.
  */
case class FlowWords(srcWord: String, dstWord: String)

/**
  * Contains methods and Spark SQL udf objects for calculation of netflow words from netflow records.
  *
  * @param timeCuts Quantile cut-offs for the time of day. Time of day is a floating point number
  *                 >= 0.0 and < 24.0
  * @param ibytCuts Quantile cut-offs for the inbytes.
  * @param ipktCuts Quantile cut-offs if the incoming packet counts.
  */
class FlowWordCreator(timeCuts: Array[Double],
                      ibytCuts: Array[Double],
                      ipktCuts: Array[Double],
                      useProtocol: Boolean = false,
                      hourBinTime: Boolean = false,
                      expBinBytes: Boolean = false,
                      expBinPackets: Boolean =false ) extends Serializable {


  /**
    * Spark SQL UDF for calculating the word summarizing a netflow transaction at the source IP
    *
    * @return String "word" summarizing a netflow connection.
    */
  def srcWordUDF = udf((hour: Int,
                        minute: Int,
                        second: Int,
                        srcIP: String,
                        dstIP: String,
                        srcPort: Int,
                        dstPort: Int,
                        protocol: String,
                        ibyt: Long,
                        ipkt: Long) =>
    flowWords(hour, minute, second, srcPort, dstPort, protocol, ibyt, ipkt).srcWord)


  /**
    * Spark SQL UDF for calculating the word summarizing a netflow transaction at the destination IP
    *
    * @return String "word" summarizing a netflow connection.
    */
  def dstWordUDF = udf((hour: Int,
                        minute: Int,
                        second: Int,
                        srcIP: String,
                        dstIP: String,
                        srcPort: Int,
                        dstPort: Int,
                       protocol: String,
                        ibyt: Long,
                        ipkt: Long) =>
    flowWords(hour, minute, second, srcPort, dstPort, protocol, ibyt, ipkt).dstWord)


  /**
    * Calculate the source and destination words summarizing a netflow record.
    *
    * @param hour
    * @param minute
    * @param second
    * @param srcPort
    * @param dstPort
    * @param ipkt
    * @param ibyt
    * @return [[FlowWords]] containing source and destination words.
    */
  def flowWords(hour: Int, minute: Int, second: Int, srcPort: Int, dstPort: Int, protocol: String, ibyt: Long, ipkt: Long): FlowWords = {

    Try {
      val timeOfDay: Double = hour.toDouble + minute.toDouble / 60 + second.toDouble / 3600

      val timeBin = if (hourBinTime == true) {
        hour
      } else {
        Quantiles.bin(timeOfDay, timeCuts)
      }

      val lnOf2 = scala.math.log(2) // natural log of 2
      val ibytBin : Long = if (expBinBytes) {
          scala.math.ceil(scala.math.log(ibyt) / lnOf2).toLong  // 0 values should never ever happen
      } else {
        Quantiles.bin(ibyt, ibytCuts)

      }
      val ipktBin : Long = if (expBinPackets) {
        scala.math.ceil(scala.math.log(ipkt) / lnOf2).toLong // 0 values should never ever happen
      } else {
        Quantiles.bin(ipkt, ibytCuts)
      }

      val LowToLowPortEncoding = 111111
      val HighToHighPortEncoding = 333333

      val proto = if (useProtocol == true) {
        protocol
      } else {
        ""
      }

      if (dstPort == 0 && srcPort == 0) {

        val baseWord = Array("0", proto, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)

      } else if (dstPort == 0 && srcPort > 0) {

        val baseWord = Array(srcPort,  proto, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = "-1_" + baseWord, dstWord = baseWord)

      } else if (srcPort == 0 && dstPort > 0) {

        val baseWord = Array(dstPort,  proto, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = "-1_" + baseWord)

      } else if (srcPort <= 1024 && dstPort <= 1024) {

        val baseWord = Array(LowToLowPortEncoding, proto, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)

      } else if (srcPort <= 1024 && dstPort > 1024) {

        val baseWord = Array(srcPort,  proto, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = "-1_" + baseWord, dstWord = baseWord)

      } else if (srcPort > 1024 && dstPort <= 1024) {

        val baseWord = Array(dstPort,  proto, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = "-1_" + baseWord)

      } else {

        // this is the srcPort > 1024 && dstPort > 1024 case



        val baseWord = Array(HighToHighPortEncoding, proto, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)
      }

    } match {
      case Success(flowWords) => flowWords
      case _ => FlowWords(InvalidDataHandler.WordError, InvalidDataHandler.WordError)
    }
  }

}

