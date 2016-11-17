package org.apache.spot.proxy

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.proxy.ProxySchema._
import org.apache.spot.utilities.DataFrameUtils

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
  def run(config: SuspiciousConnectsConfig, sparkContext: SparkContext, sqlContext: SQLContext, logger: Logger) = {

    logger.info("Starting proxy suspicious connects analysis.")

    logger.info("Loading data from: " + config.inputPath)

    val rawDataDF = sqlContext.read.parquet(config.inputPath).
      filter(Date + " is not null and " + Time + " is not null and " + ClientIP + " is not null").
      select(Date, Time, ClientIP, Host, ReqMethod, UserAgent, ResponseContentType, Duration, UserName,
        WebCat, Referer, RespCode, URIPort, URIPath, URIQuery, ServerIP, SCBytes, CSBytes, FullURI)

    logger.info("Training the model")
    val model =
      ProxySuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, config, rawDataDF)

    logger.info("Scoring")
    val scoredDF = model.score(sparkContext, rawDataDF)

    // take the maxResults least probable events of probability below the threshold and sort

    val filteredDF = scoredDF.filter(Score +  " <= " + config.threshold)
    val topRows = DataFrameUtils.dfTakeOrdered(filteredDF, "score", config.maxResults)
    val scoreIndex = scoredDF.schema.fieldNames.indexOf("score")
    val outputRDD = sparkContext.parallelize(topRows).sortBy(row => row.getDouble(scoreIndex))

    logger.info("Persisting data")
    outputRDD.map(_.mkString(config.outputDelimiter)).saveAsTextFile(config.hdfsScoredConnect)

    logger.info("Proxy suspcicious connects completed")
  }
}