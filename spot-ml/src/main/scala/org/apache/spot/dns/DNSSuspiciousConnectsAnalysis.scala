package org.apache.spot.dns

import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSchema._
import org.apache.spot.dns.model.DNSSuspiciousConnectsModel
import org.apache.log4j.Logger
import org.apache.spot.dns.model.DNSSuspiciousConnectsModel.ModelSchema
import org.apache.spot.proxy.ProxySchema.Score
import org.apache.spot.utilities.data.validation.{InvalidDataHandler => dataValidation}

/**
  * The suspicious connections analysis of DNS log data develops a probabilistic model the DNS queries
  * made by each client IP and flags
  */

object DNSSuspiciousConnectsAnalysis {

  /**
    * Run suspicious connections analysis on DNS log data.
    *
    * @param config Object encapsulating runtime parameters and CLI options.
    * @param sparkContext
    * @param sqlContext
    * @param logger
    */
  def run(config: SuspiciousConnectsConfig, sparkContext: SparkContext, sqlContext: SQLContext, logger: Logger,
          inputDNSRecords: DataFrame) = {

    logger.info("Starting DNS suspicious connects analysis.")

    val userDomain = config.userDomain

    val cleanDNSRecords = filterAndSelectCleanDNSRecords(inputDNSRecords)

    logger.info("Training the model")

    val model =
      DNSSuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, config, cleanDNSRecords, config.topicCount)

    logger.info("Scoring")
    val scoredDNSRecords = model.score(sparkContext, sqlContext, cleanDNSRecords, userDomain)

    val filteredDNSRecords = filterScoredDNSRecords(scoredDNSRecords, config.threshold)

    val orderedDNSRecords = filteredDNSRecords.orderBy(Score)

    val mostSuspiciousDNSRecords = if(config.maxResults > 0)  orderedDNSRecords.limit(config.maxResults) else orderedDNSRecords

    val outputDNSRecords = mostSuspiciousDNSRecords.select(OutSchema:_*).sort(Score)

    logger.info("DNS  suspicious connects analysis completed.")
    logger.info("Saving results to : " + config.hdfsScoredConnect)
    outputDNSRecords.map(_.mkString(config.outputDelimiter)).saveAsTextFile(config.hdfsScoredConnect)

    val invalidDNSRecords = filterAndSelectInvalidDNSRecords(inputDNSRecords)
    dataValidation.showAndSaveInvalidRecords(invalidDNSRecords, config.hdfsScoredConnect, logger)

    val corruptDNSRecords = filterAndSelectCorruptDNSRecords(scoredDNSRecords)
    dataValidation.showAndSaveCorruptRecords(corruptDNSRecords, config.hdfsScoredConnect, logger)
  }

  /**
    *
    * @param inputDNSRecords raw DNS records.
    * @return
    */
  def filterAndSelectCleanDNSRecords(inputDNSRecords: DataFrame): DataFrame ={

    val cleanDNSRecordsFilter = inputDNSRecords(Timestamp).isNotNull &&
      inputDNSRecords(Timestamp).notEqual("") &&
      inputDNSRecords(Timestamp).notEqual("-") &&
      inputDNSRecords(UnixTimestamp).isNotNull &&
      inputDNSRecords(FrameLength).isNotNull &&
      inputDNSRecords(QueryName).isNotNull &&
      inputDNSRecords(QueryName).notEqual("") &&
      inputDNSRecords(QueryName).notEqual("-") &&
      inputDNSRecords(QueryName).notEqual("(empty)") &&
      inputDNSRecords(ClientIP).isNotNull &&
      inputDNSRecords(ClientIP).notEqual("") &&
      inputDNSRecords(ClientIP).notEqual("-") &&
      ((inputDNSRecords(QueryClass).isNotNull &&
        inputDNSRecords(QueryClass).notEqual("") &&
        inputDNSRecords(QueryClass).notEqual("-")) ||
        inputDNSRecords(QueryType).isNotNull ||
        inputDNSRecords(QueryResponseCode).isNotNull)

    inputDNSRecords
      .filter(cleanDNSRecordsFilter)
      .select(InSchema: _*)
      .na.fill(DefaultQueryClass, Seq(QueryClass))
      .na.fill(DefaultQueryType, Seq(QueryType))
      .na.fill(DefaultQueryResponseCode, Seq(QueryResponseCode))
  }

  /**
    *
    * @param inputDNSRecords raw DNS records.
    * @return
    */
  def filterAndSelectInvalidDNSRecords(inputDNSRecords: DataFrame): DataFrame ={

    val invalidDNSRecordsFilter = inputDNSRecords(Timestamp).isNull ||
      inputDNSRecords(Timestamp).equalTo("") ||
      inputDNSRecords(Timestamp).equalTo("-") ||
      inputDNSRecords(UnixTimestamp).isNull ||
      inputDNSRecords(FrameLength).isNull ||
      inputDNSRecords(QueryName).isNull ||
      inputDNSRecords(QueryName).equalTo("") ||
      inputDNSRecords(QueryName).equalTo("-") ||
      inputDNSRecords(QueryName).equalTo("(empty)") ||
      inputDNSRecords(ClientIP).isNull ||
      inputDNSRecords(ClientIP).equalTo("") ||
      inputDNSRecords(ClientIP).equalTo("-") ||
      ((inputDNSRecords(QueryClass).isNull ||
        inputDNSRecords(QueryClass).equalTo("") ||
        inputDNSRecords(QueryClass).equalTo("-")) &&
        inputDNSRecords(QueryType).isNull &&
        inputDNSRecords(QueryResponseCode).isNull)

    inputDNSRecords
      .filter(invalidDNSRecordsFilter)
      .select(InSchema: _*)
  }

  /**
    *
    * @param scoredDNSRecords scored DNS records.
    * @param threshold score tolerance.
    * @return
    */
  def filterScoredDNSRecords(scoredDNSRecords: DataFrame, threshold: Double): DataFrame ={

    val filteredDNSRecordsFilter = scoredDNSRecords(Score).leq(threshold) &&
      scoredDNSRecords(Score).gt(dataValidation.ScoreError)

    scoredDNSRecords.filter(filteredDNSRecordsFilter)
  }

  /**
    *
    * @param scoredDNSRecords scored DNS records.
    * @return
    */
  def filterAndSelectCorruptDNSRecords(scoredDNSRecords: DataFrame): DataFrame = {

    val corruptDNSRecordsFilter = scoredDNSRecords(Score).equalTo(dataValidation.ScoreError)

    scoredDNSRecords
      .filter(corruptDNSRecordsFilter)
      .select(OutSchema: _*)

  }

  val DefaultQueryClass = "unknown"
  val DefaultQueryType = -1
  val DefaultQueryResponseCode = -1

  val InStructType = StructType(List(TimestampField, UnixTimestampField, FrameLengthField, ClientIPField,
    QueryNameField, QueryClassField, QueryTypeField, QueryResponseCodeField))

  val InSchema = InStructType.fieldNames.map(col)

  assert(ModelSchema.fields.forall(InStructType.fields.contains(_)))

  val OutSchema = StructType(
    List(TimestampField,
      UnixTimestampField,
      FrameLengthField,
      ClientIPField,
      QueryNameField,
      QueryClassField,
      QueryTypeField,
      QueryResponseCodeField,
      ScoreField)).fieldNames.map(col)
}