package org.apache.spot.netflow

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spot.SpotLDACWrapper
import org.apache.spot.SpotLDACWrapper.SpotLDACOutput
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig

object FlowSuspiciousConnects {

  def run(config: SuspiciousConnectsConfig, sparkContext: SparkContext, sqlContext: SQLContext, logger: Logger)(implicit outputDelimiter: String) = {
    
    logger.info("Flow LDA starts")

    val docWordCount = FlowPreLDA.flowPreLDA(config.inputPath, config.scoresFile, config.duplicationFactor, sparkContext,
      sqlContext, logger)

    val SpotLDACOutput(documentResults, wordResults) = SpotLDACWrapper.runLDA(docWordCount, config.modelFile, config.topicDocumentFile, config.topicWordFile,
      config.mpiPreparationCmd, config.mpiCmd, config.mpiProcessCount, config.topicCount, config.localPath,
      config.ldaPath, config.localUser,  config.analysis, config.nodes, config.ldaPRGSeed)

    FlowPostLDA.flowPostLDA(config.inputPath, config.hdfsScoredConnect, config.outputDelimiter, config.threshold, config.maxResults, documentResults,
      wordResults, config.topicCount, sparkContext, sqlContext, logger)

    logger.info("Flow LDA completed")
  }

}