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
    *
    * @param config Object encapsulating runtime parameters and CLI options.
    * @param sparkContext
    * @param sqlContext
    * @param logger
    */
  def run(config: SuspiciousConnectsConfig, sparkContext: SparkContext, sqlContext: SQLContext, logger: Logger) = {
    logger.info("Starting DNS suspicious connects analysis.")


    logger.info("Loading data")

    val userDomain = config.userDomain

    val rawDataDF = sqlContext.read.parquet(config.inputPath)
      .filter(Timestamp + " is not null and " + UnixTimestamp + " is not null")
      .select(inColumns:_*)

    logger.info("Training the model")

    val model =
      DNSSuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, config, rawDataDF, config.topicCount)

    logger.info("Scoring")
    val scoredDF = model.score(sparkContext, sqlContext, rawDataDF, userDomain)


    val filteredDF = scoredDF.filter(Score + " <= " + config.threshold)
    val mostSusipiciousDF: DataFrame = filteredDF.orderBy(Score).limit(config.maxResults)

    val outputDF = mostSusipiciousDF.select(OutColumns:_*).sort(Score)

    logger.info("DNS  suspcicious connects analysis completed.")
    logger.info("Saving results to : " + config.hdfsScoredConnect)
    outputDF.map(_.mkString(config.outputDelimiter)).saveAsTextFile(config.hdfsScoredConnect)
  }
}