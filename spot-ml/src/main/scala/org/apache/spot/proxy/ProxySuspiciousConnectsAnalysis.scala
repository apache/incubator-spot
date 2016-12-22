package org.apache.spot.proxy

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.proxy.ProxySchema._
import org.apache.spot.utilities.data.validation.{InvalidDataHandler => dataValidation}

/**
  * Run suspicious connections analysis on proxy data.
  */
object ProxySuspiciousConnectsAnalysis {

  /**
    * Run suspicious connections analysis on proxy data.
    *
    * @param config       SuspicionConnectsConfig object, contains runtime parameters from CLI.
    * @param sparkContext Apache Spark context.
    * @param sqlContext   Spark SQL context.
    * @param logger       Logs execution progress, information and errors for user.
    */
  def run(config: SuspiciousConnectsConfig, sparkContext: SparkContext, sqlContext: SQLContext, logger: Logger,
          inputProxyRecords: DataFrame) = {

    logger.info("Starting proxy suspicious connects analysis.")

    val cleanProxyRecords = filterAndSelectCleanProxyRecords(inputProxyRecords)

    logger.info("Training the model")
    val model =
      ProxySuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, config, cleanProxyRecords)

    logger.info("Scoring")
    val scoredProxyRecords = model.score(sparkContext, cleanProxyRecords)

    // take the maxResults least probable events of probability below the threshold and sort

    val filteredProxyRecords = filterScoredProxyRecords(scoredProxyRecords, config.threshold)

    val orderedProxyRecords = filteredProxyRecords.orderBy(Score)

    val mostSuspiciousProxyRecords = if(config.maxResults > 0)  orderedProxyRecords.limit(config.maxResults) else orderedProxyRecords

    val outputProxyRecords = mostSuspiciousProxyRecords.select(OutSchema:_*)

    logger.info("Proxy suspicious connects analysis completed")
    logger.info("Saving results to: " + config.hdfsScoredConnect)
    outputProxyRecords.map(_.mkString(config.outputDelimiter)).saveAsTextFile(config.hdfsScoredConnect)

    val invalidProxyRecords = filterAndSelectInvalidProxyRecords(inputProxyRecords)
    dataValidation.showAndSaveInvalidRecords(invalidProxyRecords, config.hdfsScoredConnect, logger)

    val corruptProxyRecords = filterAndSelectCorruptProxyRecords(scoredProxyRecords)
    dataValidation.showAndSaveCorruptRecords(corruptProxyRecords, config.hdfsScoredConnect, logger)
  }

  /**
    *
    * @param inputProxyRecords raw proxy records.
    * @return
    */
  def filterAndSelectCleanProxyRecords(inputProxyRecords: DataFrame): DataFrame ={

    val cleanProxyRecordsFilter =  inputProxyRecords(Date).isNotNull &&
      inputProxyRecords(Time).isNotNull &&
      inputProxyRecords(ClientIP).isNotNull &&
      inputProxyRecords(Host).isNotNull &&
      inputProxyRecords(FullURI).isNotNull

    inputProxyRecords
      .filter(cleanProxyRecordsFilter)
      .select(InSchema:_*)
      .na.fill(DefaultUserAgent, Seq(UserAgent))
      .na.fill(DefaultResponseContentType, Seq(ResponseContentType))
  }

  /**
    *
    * @param inputProxyRecords raw proxy records.
    * @return
    */
  def filterAndSelectInvalidProxyRecords(inputProxyRecords: DataFrame): DataFrame ={

    val invalidProxyRecordsFilter = inputProxyRecords(Date).isNull ||
      inputProxyRecords(Time).isNull ||
      inputProxyRecords(ClientIP).isNull ||
      inputProxyRecords(Host).isNull ||
      inputProxyRecords(FullURI).isNull

    inputProxyRecords
      .filter(invalidProxyRecordsFilter)
      .select(InSchema: _*)
  }

  /**
    *
    * @param scoredProxyRecords scored proxy records.
    * @param threshold score tolerance.
    * @return
    */
  def filterScoredProxyRecords(scoredProxyRecords: DataFrame, threshold: Double): DataFrame ={

    val filteredProxyRecordsFilter = scoredProxyRecords(Score).leq(threshold) &&
      scoredProxyRecords(Score).gt(dataValidation.ScoreError)

    scoredProxyRecords.filter(filteredProxyRecordsFilter)
  }

  /**
    *
    * @param scoredProxyRecords scored proxy records.
    * @return
    */
  def filterAndSelectCorruptProxyRecords(scoredProxyRecords: DataFrame): DataFrame ={

    val corruptProxyRecordsFilter = scoredProxyRecords(Score).equalTo(dataValidation.ScoreError)

    scoredProxyRecords
      .filter(corruptProxyRecordsFilter)
      .select(OutSchema: _*)
  }

  val DefaultUserAgent = "-"
  val DefaultResponseContentType = "-"

  val InSchema = StructType(
    List(DateField,
      TimeField,
      ClientIPField,
      HostField,
      ReqMethodField,
      UserAgentField,
      ResponseContentTypeField,
      DurationField,
      UserNameField,
      WebCatField,
      RefererField,
      RespCodeField,
      URIPortField,
      URIPathField,
      URIQueryField,
      ServerIPField,
      SCBytesField,
      CSBytesField,
      FullURIField)).fieldNames.map(col)

  val OutSchema = StructType(
    List(DateField,
      TimeField,
      ClientIPField,
      HostField,
      ReqMethodField,
      UserAgentField,
      ResponseContentTypeField,
      DurationField,
      UserNameField,
      WebCatField,
      RefererField,
      RespCodeField,
      URIPortField,
      URIPathField,
      URIQueryField,
      ServerIPField,
      SCBytesField,
      CSBytesField,
      FullURIField,
      WordField,
      ScoreField)).fieldNames.map(col)
}