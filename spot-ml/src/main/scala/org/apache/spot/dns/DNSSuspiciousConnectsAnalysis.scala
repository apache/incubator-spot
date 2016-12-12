package org.apache.spot.dns

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSchema._
import org.apache.spot.dns.model.DNSSuspiciousConnectsModel
import org.apache.spot.dns.model.DNSSuspiciousConnectsModel.ModelSchema

/**
  * The suspicious connections analysis of DNS log data develops a probabilistic model the DNS queries
  * made by each client IP and flags
  */

object DNSSuspiciousConnectsAnalysis {

  val inSchema = StructType(List(TimestampField, UnixTimestampField, FrameLengthField, ClientIPField,
      QueryNameField, QueryClassField, QueryTypeField, QueryResponseCodeField))

  val inColumns = inSchema.fieldNames.map(col)


  assert(ModelSchema.fields.forall(inSchema.fields.contains(_)))

  val OutSchema = StructType(
    List(TimestampField,
      UnixTimestampField,
      FrameLengthField,
      ClientIPField,
      QueryNameField,
      QueryClassField,
      QueryTypeField,
      QueryResponseCodeField,
      ScoreField))

  val OutColumns = OutSchema.fieldNames.map(col)


  /**
    * Run suspicious connections analysis on DNS log data.
    * Saves the most suspicious connections to a CSV file on HDFS.
    *
    * @param config Object encapsulating runtime parameters and CLI options.
    * @param sparkContext
    * @param sqlContext
    * @param logger
    */
  def run(config: SuspiciousConnectsConfig, sparkContext: SparkContext, sqlContext: SQLContext, logger: Logger) = {
    logger.info("Starting DNS suspicious connects analysis.")


    logger.info("Loading data")

    val rawDataDF = sqlContext.read.parquet(config.inputPath)
      .filter(Timestamp + " is not null and " + UnixTimestamp + " is not null")
      .select(inColumns:_*)



    val scoredDF = detectDNSAnomalies(rawDataDF, config, sparkContext, sqlContext, logger)


    val filteredDF = scoredDF.filter(Score + " <= " + config.threshold)
    val mostSusipiciousDF: DataFrame = filteredDF.orderBy(Score).limit(config.maxResults)

    mostSusipiciousDF.select(OutColumns:_*).sort(Score)
    logger.info("Saving results to : " + config.hdfsScoredConnect)


    mostSusipiciousDF.map(_.mkString(config.outputDelimiter)).saveAsTextFile(config.hdfsScoredConnect)
  }

  /**
    * Identify anomalous DNS log entries in in the provided data frame.
    *
    * @param data Data frame of DNS entries
    * @param config
    * @param sparkContext
    * @param sqlContext
    * @param logger
    * @return
    */
  def detectDNSAnomalies(data: DataFrame, config: SuspiciousConnectsConfig,
                         sparkContext: SparkContext,
                         sqlContext: SQLContext,
                         logger: Logger) : DataFrame = {


    logger.info("Fitting probabilistic model to data")
    val model =
      DNSSuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, config, data, config.topicCount)

    logger.info("Identifying outliers")
    model.score(sparkContext, sqlContext, data)
  }
}