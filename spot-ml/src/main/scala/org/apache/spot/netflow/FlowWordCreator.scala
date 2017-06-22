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
import org.apache.spot.utilities.MathUtils.ceilLog2
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
  */
object FlowWordCreator extends Serializable {


  /**
    * Spark SQL UDF for calculating the word summarizing a netflow transaction at the source IP
    *
    * @return String "word" summarizing a netflow connection.
    */
  def srcWordUDF = udf((hour: Int,
                        srcIP: String,
                        dstIP: String,
                        srcPort: Int,
                        dstPort: Int,
                        protocol: String,
                        ibyt: Long,
                        ipkt: Long) =>
    flowWords(hour, srcPort, dstPort, protocol, ibyt, ipkt).srcWord)


  /**
    * Spark SQL UDF for calculating the word summarizing a netflow transaction at the destination IP
    *
    * @return String "word" summarizing a netflow connection.
    */
  def dstWordUDF = udf((hour: Int,
                        srcIP: String,
                        dstIP: String,
                        srcPort: Int,
                        dstPort: Int,
                        protocol: String,
                        ibyt: Long,
                        ipkt: Long) =>
    flowWords(hour, srcPort, dstPort, protocol, ibyt, ipkt).dstWord)


  /**
    * Calculate the source and destination words summarizing a netflow record.
    *
    * @param hour
    * @param srcPort
    * @param dstPort
    * @param ipkt
    * @param ibyt
    * @return [[FlowWords]] containing source and destination words.
    */
  def flowWords(hour: Int, srcPort: Int, dstPort: Int, protocol: String, ibyt: Long, ipkt: Long): FlowWords = {

    Try {

      val ibytBin = ceilLog2(ibyt + 1)
      val ipktBin = ceilLog2(ipkt + 1)

      val LowToLowPortEncoding = 111111
      val HighToHighPortEncoding = 333333

      val proto = protocol


      if (dstPort == 0 && srcPort == 0) {

        val baseWord = Array("0", proto, hour, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)

      } else if (dstPort == 0 && srcPort > 0) {

        val baseWord = Array(srcPort, proto, hour, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = "-1_" + baseWord, dstWord = baseWord)

      } else if (srcPort == 0 && dstPort > 0) {

        val baseWord = Array(dstPort, proto, hour, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = "-1_" + baseWord)

      } else if (srcPort <= 1024 && dstPort <= 1024) {

        val baseWord = Array(LowToLowPortEncoding, proto, hour, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)

      } else if (srcPort <= 1024 && dstPort > 1024) {

        val baseWord = Array(srcPort, proto, hour, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = "-1_" + baseWord, dstWord = baseWord)

      } else if (srcPort > 1024 && dstPort <= 1024) {

        val baseWord = Array(dstPort, proto, hour, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = "-1_" + baseWord)

      } else {

        // this is the srcPort > 1024 && dstPort > 1024 case


        val baseWord = Array(HighToHighPortEncoding, proto, hour, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)
      }

    } match {
      case Success(flowWords) => flowWords
      case _ => FlowWords(InvalidDataHandler.WordError, InvalidDataHandler.WordError)
    }
  }

}

