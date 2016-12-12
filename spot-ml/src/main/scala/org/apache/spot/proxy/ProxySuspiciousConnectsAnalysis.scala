package org.apache.spot.proxy

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.proxy.ProxySchema._

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

    val scoredDF = detectProxyAnomalies(rawDataDF, config, sparkContext, sqlContext, logger)


    val filteredDF = scoredDF.filter(Score +  " <= " + config.threshold)
    val mostSusipiciousDF: DataFrame = filteredDF.orderBy(Score).limit(config.maxResults)

    logger.info("Persisting data to hdfs: " + config.hdfsScoredConnect)
    mostSusipiciousDF.map(_.mkString(config.outputDelimiter)).saveAsTextFile(config.hdfsScoredConnect)

    logger.info("Proxy suspcicious connects completed")
  }


  /**
    * Identify anomalous proxy log entries in in the provided data frame.
    *
    * @param data Data frame of proxy entries
    * @param config
    * @param sparkContext
    * @param sqlContext
    * @param logger
    * @return
    */
  def detectProxyAnomalies(data: DataFrame,
                          config: SuspiciousConnectsConfig,
                          sparkContext: SparkContext,
                          sqlContext: SQLContext,
                          logger: Logger) : DataFrame = {


    logger.info("Fitting probabilistic model to data")
    val model = ProxySuspiciousConnectsModel.trainNewModel(sparkContext, sqlContext, logger, config, data)
    logger.info("Identifying outliers")

    model.score(sparkContext, data)
  }
}