package org.apache.spot.netflow

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.netflow.model.FlowSuspiciousConnectsModel


/**
  * The suspicious connections analysis of netflow records develops a probabilistic model the traffic about each
  * IP and flags transactions with an exceptionally low probability in the model as anomalous.
  */

object FlowSuspiciousConnectsAnalysis {

  def run(config: SuspiciousConnectsConfig, sparkContext: SparkContext, sqlContext: SQLContext, logger: Logger)(implicit outputDelimiter: String) = {

    logger.info("Loading data")

    val rawDataDF = sqlContext.read.parquet(config.inputPath)
      .filter(Hour + " BETWEEN 0 AND 23 AND  " + Minute + " BETWEEN 0 AND 59 AND  " + Second + " BETWEEN 0 AND 59")
      .select(inColumns: _*)


    logger.info("Training the model")

    val model =
      FlowSuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, config, rawDataDF, config.topicCount)

    logger.info("Scoring")
    val scoredDF = model.score(sparkContext, sqlContext, rawDataDF)

    val filteredDF = scoredDF.filter(Score + " <= " + config.threshold)

    val mostSusipiciousDF: DataFrame = filteredDF.orderBy(Score).limit(config.maxResults)


    val outputDF = mostSusipiciousDF.select(OutColumns: _*)

    logger.info("Netflow  suspicious connects analysis completed.")
    logger.info("Saving results to : " + config.hdfsScoredConnect)
    outputDF.map(_.mkString(config.outputDelimiter)).saveAsTextFile(config.hdfsScoredConnect)
  }

  val inSchema = StructType(List(TimeReceivedField,
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
    ObytField))

  val inColumns = inSchema.fieldNames.map(col)

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
      ScoreField))

  val OutColumns = OutSchema.fieldNames.map(col)
}