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

import org.apache.log4j.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spot.SuspiciousConnects.SuspiciousConnectsAnalysisResults
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.netflow.model.FlowSuspiciousConnectsModel
import org.apache.spot.utilities.data.validation.{InvalidDataHandler => dataValidation}


/**
  * The suspicious connections analysis of netflow records develops a probabilistic model the traffic about each
  * IP and flags transactions with an exceptionally low probability in the model as anomalous.
  */

object FlowSuspiciousConnectsAnalysis {


  val InSchema = StructType(List(TimeReceivedField,
    YearField,
    MonthField,
    DayField,
    HourField,
    MinuteField,
    SecondField,
    DurationField,
    SourceIPField,
    DestinationIPField,
    SourcePortField,
    DestinationPortField,
    ProtocolField,
    IpktField,
    IbytField,
    OpktField,
    ObytField)).fieldNames.map(col)
  val OutSchema = StructType(
    List(TimeReceivedField,
      YearField,
      MonthField,
      DayField,
      HourField,
      MinuteField,
      SecondField,
      DurationField,
      SourceIPField,
      DestinationIPField,
      SourcePortField,
      DestinationPortField,
      ProtocolField,
      IpktField,
      IbytField,
      OpktField,
      ObytField,
      ScoreField)).fieldNames.map(col)

  /**
    * Runs suspicious connects analysis for Flow records
    *
    * @param config           spot-ml configuration
    * @param sparkSession     the SparkSession
    * @param logger           application logger
    * @param inputFlowRecords input Flow records for analysis
    * @return
    */
  def run(config: SuspiciousConnectsConfig, sparkSession: SparkSession, logger: Logger,
          inputFlowRecords: DataFrame): SuspiciousConnectsAnalysisResults = {

    logger.info("Starting flow suspicious connects analysis.")

    val flowRecords = filterRecords(inputFlowRecords).select(InSchema: _*)

    logger.info("Fitting probabilistic model to data")
    val model =
      FlowSuspiciousConnectsModel.trainModel(sparkSession, logger, config, flowRecords)

    logger.info("Identifying outliers")
    val scoredFlowRecords = model.score(sparkSession, flowRecords, config.precisionUtility)

    val filteredScored = filterScoredRecords(scoredFlowRecords, config.threshold)

    val orderedFlowRecords = filteredScored.orderBy(Score)

    val mostSuspiciousFlowRecords =
      if (config.maxResults > 0) orderedFlowRecords.limit(config.maxResults) else orderedFlowRecords

    val outputFlowRecords = mostSuspiciousFlowRecords.select(OutSchema: _*)

    val invalidFlowRecords = filterInvalidRecords(inputFlowRecords).select(InSchema: _*)

    SuspiciousConnectsAnalysisResults(outputFlowRecords, invalidFlowRecords)

  }

  /**
    * Apply a filter to raw data (Flow records) and returns only those that are good for word creation and that can be
    * passed to LDA
    *
    * @param inputFlowRecords raw flow records
    * @return
    */
  def filterRecords(inputFlowRecords: DataFrame): DataFrame = {

    val cleanFlowRecordsFilter = inputFlowRecords(Hour).between(0, 23) &&
      inputFlowRecords(Minute).between(0, 59) &&
      inputFlowRecords(Second).between(0, 59) &&
      inputFlowRecords(TimeReceived).isNotNull &&
      inputFlowRecords(SourceIP).isNotNull &&
      inputFlowRecords(DestinationIP).isNotNull &&
      inputFlowRecords(SourcePort).geq(0) &&
      inputFlowRecords(DestinationPort).geq(0) &&
      inputFlowRecords(Ibyt).geq(0) &&
      inputFlowRecords(Ipkt).geq(0)

    inputFlowRecords
      .filter(cleanFlowRecordsFilter)
  }

  /**
    * Gets invalid records based on required fields validations
    *
    * @param inputFlowRecords raw flow records.
    * @return
    */
  def filterInvalidRecords(inputFlowRecords: DataFrame): DataFrame = {

    val invalidFlowRecordsFilter = inputFlowRecords(Hour).between(0, 23) &&
      inputFlowRecords(Minute).between(0, 59) &&
      inputFlowRecords(Second).between(0, 59) &&
      inputFlowRecords(TimeReceived).isNull ||
      inputFlowRecords(SourceIP).isNull ||
      inputFlowRecords(DestinationIP).isNull ||
      inputFlowRecords(SourcePort).isNull ||
      inputFlowRecords(SourcePort).lt(0) ||
      inputFlowRecords(DestinationPort).isNull ||
      inputFlowRecords(DestinationPort).lt(0) ||
      inputFlowRecords(Ibyt).isNull ||
      inputFlowRecords(Ibyt).lt(0) ||
      inputFlowRecords(Ipkt).isNull ||
      inputFlowRecords(Ipkt).lt(0)

    inputFlowRecords
      .filter(invalidFlowRecordsFilter)
  }

  /**
    * Gets the records with a score equal or below the threshold
    *
    * @param scoredFlowRecords scored flow records.
    * @param threshold         score tolerance.
    * @return
    */
  def filterScoredRecords(scoredFlowRecords: DataFrame, threshold: Double): DataFrame = {

    val filteredFlowRecordsFilter = scoredFlowRecords(Score).leq(threshold) &&
      scoredFlowRecords(Score).gt(dataValidation.ScoreError)

    scoredFlowRecords.filter(filteredFlowRecordsFilter)
  }

}