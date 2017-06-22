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

package org.apache.spot.proxy

import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spot.proxy.ProxySchema._
import org.apache.spot.utilities.data.InputOutputDataHandler.getFeedbackRDD


object ProxyFeedback {

  /**
    * Load the feedback file for proxy data.
 *
    * @param sparkSession      Spark Session
    * @param feedbackFile      Local machine path to the proxy feedback file.
    * @param duplicationFactor Number of words to create per flagged feedback entry.
    * @return DataFrame of the feedback events.
    */
  def loadFeedbackDF(sparkSession: SparkSession,
                     feedbackFile: String,
                     duplicationFactor: Int): DataFrame = {


    val feedbackSchema = StructType(
      List(StructField(Date, StringType, nullable = true),
        StructField(Time, StringType, nullable = true),
        StructField(ClientIP, StringType, nullable = true),
        StructField(Host, StringType, nullable = true),
        StructField(ReqMethod, StringType, nullable = true),
        StructField(UserAgent, StringType, nullable = true),
        StructField(ResponseContentType, StringType, nullable = true),
        StructField(RespCode, StringType, nullable = true),
        StructField(FullURI, StringType, nullable = true)))

    val feedback: RDD[String] = getFeedbackRDD(sparkSession, feedbackFile)

    if (!feedback.isEmpty()) {

      val dateIndex = 0
      val timeIndex = 1
      val clientIpIndex = 2
      val hostIndex = 3
      val reqMethodIndex = 4
      val userAgentIndex = 5
      val resContTypeIndex = 6
      val respCodeIndex = 11
      val fullURIIndex = 18

      val fullURISeverityIndex = 22

      sparkSession.createDataFrame(feedback.map(_.split("\t"))
        .filter(row => row(fullURISeverityIndex).trim.toInt == 3)
        .map(row => Row.fromSeq(List(row(dateIndex),
          row(timeIndex),
          row(clientIpIndex),
          row(hostIndex),
          row(reqMethodIndex),
          row(userAgentIndex),
          row(resContTypeIndex),
          row(respCodeIndex),
          row(fullURIIndex))))
        .flatMap(row => List.fill(duplicationFactor)(row)), feedbackSchema)
        .select(Date, Time, ClientIP, Host, ReqMethod, UserAgent, ResponseContentType, RespCode, FullURI)
    } else {
      sparkSession.createDataFrame(sparkSession.sparkContext.emptyRDD[Row], feedbackSchema)
    }
  }
}
