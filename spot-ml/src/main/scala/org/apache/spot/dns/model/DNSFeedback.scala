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

package org.apache.spot.dns.model

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spot.dns.model.DNSSuspiciousConnectsModel.{ModelSchema, modelColumns}
import org.apache.spot.utilities.data.InputOutputDataHandler.getFeedbackRDD

/**
  * Routines for ingesting the feedback file provided by the operational analytics layer.
  *
  */
object DNSFeedback {

  /**
    * Load the feedback file for DNS data.
 *
    * @param sparkSession      Spark Session
    * @param feedbackFile      Local machine path to the DNS feedback file.
    * @param duplicationFactor Number of words to create per flagged feedback entry.
    * @return DataFrame of the feedback events.
    */
  def loadFeedbackDF(sparkSession: SparkSession,
                     feedbackFile: String,
                     duplicationFactor: Int): DataFrame = {


    val feedback: RDD[String] = getFeedbackRDD(sparkSession, feedbackFile)

    if (!feedback.isEmpty()) {

      /*
      The columns and their entries are as follows:
       0 frame_time
       1 frame_len
       2 ip_dst
       3 dns_qry_name
       4 dns_qry_class
       5 dns_qry_type
       6 dns_qry_rcode
       7 score
       8 tld
       9 query_rep
       10 hh
       11 ip_sev
       12 dns_sev
       13 dns_qry_class_name
       14 dns_qry_type_name
       15 dns_qry_rcode_name
       16 network_context
       17 unix_tstamp
      */
      val FrameTimeIndex = 0
      val UnixTimeStampIndex = 17
      val FrameLenIndex = 1
      val IpDstIndex = 2
      val DnsQryNameIndex = 3
      val DnsQryClassIndex = 4
      val DnsQryTypeIndex = 5
      val DnsQryRcodeIndex = 6
      val DnsSevIndex = 12

      sparkSession.createDataFrame(feedback.map(_.split("\t"))
        .filter(row => row(DnsSevIndex).trim.toInt == 3)
        .map(row => Row.fromSeq(Seq(row(FrameTimeIndex),
          row(UnixTimeStampIndex).toLong,
          row(FrameLenIndex).toInt,
          row(IpDstIndex),
          row(DnsQryNameIndex),
          row(DnsQryClassIndex),
          row(DnsQryTypeIndex).toInt,
          row(DnsQryRcodeIndex).toInt)))
        .flatMap(row => List.fill(duplicationFactor)(row)), ModelSchema)
        .select(modelColumns: _*)
    } else {
      sparkSession.createDataFrame(sparkSession.sparkContext.emptyRDD[Row], ModelSchema)
    }
  }
}
