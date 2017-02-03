package org.apache.spot.netflow

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.netflow.model.FlowSuspiciousConnectsModel
import org.apache.spot.utilities.data.validation.{InvalidDataHandler => dataValidation}


/**
  * The suspicious connections analysis of netflow records develops a probabilistic model the traffic about each
  * IP and flags transactions with an exceptionally low probability in the model as anomalous.
  */

object FlowSuspiciousConnectsAnalysis {


  def run(config: SuspiciousConnectsConfig, sparkContext: SparkContext, sqlContext: SQLContext, logger: Logger,
          inputFlowRecords: DataFrame) = {

    logger.info("Starting flow suspicious connects analysis.")

    val cleanFlowRecords = filterAndSelectCleanFlowRecords(inputFlowRecords)

    val scoredFlowRecords = detectFlowAnomalies(cleanFlowRecords, config, sparkContext, sqlContext, logger)

    val filteredFlowRecords = filterScoredFlowRecords(scoredFlowRecords, config.threshold)

    val orderedFlowRecords = filteredFlowRecords.orderBy(Score)

    val mostSuspiciousFlowRecords =
      if (config.maxResults > 0) orderedFlowRecords.limit(config.maxResults) else orderedFlowRecords

    val outputFlowRecords = mostSuspiciousFlowRecords.select(OutSchema: _*)

    logger.info("Netflow  suspicious connects analysis completed.")
    logger.info("Saving results to : " + config.hdfsScoredConnect)
    outputFlowRecords.map(_.mkString(config.outputDelimiter)).saveAsTextFile(config.hdfsScoredConnect)

    val invalidFlowRecords = filterAndSelectInvalidFlowRecords(inputFlowRecords)
    dataValidation.showAndSaveInvalidRecords(invalidFlowRecords, config.hdfsScoredConnect, logger)

  }

  /**
    * Identify anomalous netflow log entries in in the provided data frame.
    *
    * @param data Data frame of netflow entries
    * @param config
    * @param sparkContext
    * @param sqlContext
    * @param logger
    * @return
    */
  def detectFlowAnomalies(data: DataFrame,
                          config: SuspiciousConnectsConfig,
                          sparkContext: SparkContext,
                          sqlContext: SQLContext,
                          logger: Logger): DataFrame = {


    logger.info("Fitting probabilistic model to data")
    val model =
      FlowSuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, config, data, config.topicCount)

    logger.info("Identifying outliers")
    model.score(sparkContext, sqlContext, data)
  }

  /**
    *
    * @param inputFlowRecords raw flow records
    * @return
    */
  def filterAndSelectCleanFlowRecords(inputFlowRecords: DataFrame): DataFrame = {

    val cleanFlowRecordsFilter = inputFlowRecords(Hour).between(0, 23) &&
      inputFlowRecords(Minute).between(0, 59) &&
      inputFlowRecords(Second).between(0, 59) &&
      inputFlowRecords(TimeReceived).isNotNull &&
      inputFlowRecords(SourceIP).isNotNull &&
      inputFlowRecords(DestinationIP).isNotNull &&
      inputFlowRecords(SourcePort).isNotNull &&
      inputFlowRecords(DestinationPort).isNotNull &&
      inputFlowRecords(Ibyt).isNotNull &&
      inputFlowRecords(Ipkt).isNotNull

    inputFlowRecords
      .filter(cleanFlowRecordsFilter)
      .select(InSchema: _*)

  }

  /**
    *
    * @param inputFlowRecords raw flow records.
    * @return
    */
  def filterAndSelectInvalidFlowRecords(inputFlowRecords: DataFrame): DataFrame = {

    val invalidFlowRecordsFilter = inputFlowRecords(Hour).between(0, 23) &&
      inputFlowRecords(Minute).between(0, 59) &&
      inputFlowRecords(Second).between(0, 59) &&
      inputFlowRecords(TimeReceived).isNull ||
      inputFlowRecords(SourceIP).isNull ||
      inputFlowRecords(DestinationIP).isNull ||
      inputFlowRecords(SourcePort).isNull ||
      inputFlowRecords(DestinationPort).isNull ||
      inputFlowRecords(Ibyt).isNull ||
      inputFlowRecords(Ipkt).isNull

    inputFlowRecords
      .filter(invalidFlowRecordsFilter)
      .select(InSchema: _*)
  }

  /**
    *
    * @param scoredFlowRecords scored flow records.
    * @param threshold         score tolerance.
    * @return
    */
  def filterScoredFlowRecords(scoredFlowRecords: DataFrame, threshold: Double): DataFrame = {

    val filteredFlowRecordsFilter = scoredFlowRecords(Score).leq(threshold) &&
      scoredFlowRecords(Score).gt(dataValidation.ScoreError)

    scoredFlowRecords.filter(filteredFlowRecordsFilter)
  }

  /**
    *
    * @param scoredFlowRecords scored flow records.
    * @return
    */
  def filterAndSelectCorruptFlowRecords(scoredFlowRecords: DataFrame): DataFrame = {

    val corruptFlowRecordsFilter = scoredFlowRecords(Score).equalTo(dataValidation.ScoreError)

    scoredFlowRecords
      .filter(corruptFlowRecordsFilter)
      .select(OutSchema: _*)

  }


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

}