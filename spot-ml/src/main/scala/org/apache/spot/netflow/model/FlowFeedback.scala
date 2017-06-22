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

package org.apache.spot.netflow.model

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spot.netflow.model.FlowSuspiciousConnectsModel._
import org.apache.spot.utilities.data.InputOutputDataHandler.getFeedbackRDD

/**
  * Routines for ingesting the feedback file provided by the operational analytics layer.
  *
  */
object FlowFeedback {


  /**
    * Load the feedback file for netflow data.
    *
    * @param sparkSession      Spark Session.
    * @param feedbackFile      Local machine path to the netflow feedback file.
    * @param duplicationFactor Number of words to create per flagged feedback entry.
    * @return DataFrame of the feedback events.
    */
  def loadFeedbackDF(sparkSession: SparkSession,
                     feedbackFile: String,
                     duplicationFactor: Int): DataFrame = {

    val feedback: RDD[String] = getFeedbackRDD(sparkSession, feedbackFile)

    if (!feedback.isEmpty()) {
      /*
         flow_scores.csv - feedback file structure
         0	sev
         1	tstart
         2	srcIP
         3	dstIP
         4	sport
         5	dport
         6	ipkt
         7	ibyt
        */

      // Given the structure pull out indexes we need for a new DataFrame creation
      // containing the columns for word creation only.

      val ScoreIndex = 0
      val TimeStartIndex = 1
      val SourceIpIndex = 2
      val DestinationIpIndex = 3
      val SourcePortIndex = 4
      val DestinationPortIndex = 5
      val IpktIndex = 6
      val IbytIndex = 7

      sparkSession.createDataFrame(feedback.map(_.split("\t"))
        .filter(row => row(ScoreIndex).trim.toInt == 3)
        .map(row => Row.fromSeq(Seq(
          row(TimeStartIndex).split(" ")(1).split(":")(0).trim.toInt, // hour
          row(TimeStartIndex).split(" ")(1).split(":")(1).trim.toInt, // minute
          row(TimeStartIndex).split(" ")(1).split(":")(2).trim.toInt, // second
          row(SourceIpIndex),
          row(DestinationIpIndex),
          row(SourcePortIndex).trim.toInt,
          row(DestinationPortIndex).trim.toInt,
          row(IpktIndex).trim.toLong,
          row(IbytIndex).trim.toLong)))
        .flatMap(row => List.fill(duplicationFactor)(row)), ModelSchema)
        .select(ModelColumns: _*)

    } else {
      sparkSession.createDataFrame(sparkSession.sparkContext.emptyRDD[Row], ModelSchema)
    }
  }
}
