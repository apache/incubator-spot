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

package org.apache.spot

import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSuspiciousConnectsAnalysis
import org.apache.spot.netflow.FlowSuspiciousConnectsAnalysis
import org.apache.spot.proxy.ProxySuspiciousConnectsAnalysis
import org.apache.spot.utilities.data.InputOutputDataHandler
import org.apache.spot.utilities.data.validation.InvalidDataHandler


/**
  * Top level entrypoint to execute suspicious connections analysis on network data.
  * Supported analyses:
  *  flow  : netflow data
  *  dns : DNS server logs
  *  proxy : proxy server logs
  */

object SuspiciousConnects {

  /**
    * Execute suspicious connections analysis on network data.
    *
    * @param args Command line arguments.
    */
  def main(args: Array[String]) {

    val parser = SuspiciousConnectsArgumentParser.parser

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.INFO)

    parser.parse(args, SuspiciousConnectsConfig()) match {
      case Some(config) =>

        Logger.getLogger("org").setLevel(Level.WARN)
        Logger.getLogger("akka").setLevel(Level.OFF)

        val analysis = config.analysis

        val spark = SparkSession.builder
          .appName("Spot ML:  " + analysis + " suspicious connects analysis")
          .master("yarn")
          .getOrCreate()

        val inputDataFrame = InputOutputDataHandler.getInputDataFrame(spark, config.inputPath, logger)
          .getOrElse(spark.emptyDataFrame)
        if(inputDataFrame.rdd.isEmpty()) {
          logger.error("Couldn't read data from location " + config.inputPath +", please verify it's a valid location and that " +
            s"contains parquet files with a given schema and try again.")
          System.exit(0)
        }

        val results: Option[SuspiciousConnectsAnalysisResults] = analysis match {
          case "flow" => Some(FlowSuspiciousConnectsAnalysis.run(config, spark, logger,
            inputDataFrame))
          case "dns" => Some(DNSSuspiciousConnectsAnalysis.run(config, spark, logger,
            inputDataFrame))
          case "proxy" => Some(ProxySuspiciousConnectsAnalysis.run(config, spark, logger,
            inputDataFrame))
          case _ => None
        }

        results match {
          case Some(SuspiciousConnectsAnalysisResults(resultRecords, invalidRecords)) => {

            logger.info(s"$analysis suspicious connects analysis completed.")
            logger.info("Saving results to : " + config.hdfsScoredConnect)

            import spark.implicits._
            resultRecords.map(_.mkString(config.outputDelimiter)).rdd.saveAsTextFile(config.hdfsScoredConnect)

            InputOutputDataHandler.mergeResultsFiles(spark, config.hdfsScoredConnect, analysis, logger)

            InvalidDataHandler.showAndSaveInvalidRecords(invalidRecords, config.hdfsScoredConnect, logger)
          }

          case None => logger.error("Unsupported (or misspelled) analysis: " + analysis)
        }

        spark.stop()

      case None => logger.error("Error parsing arguments.")
    }

    System.exit(0)
  }

  /**
    *
    * @param suspiciousConnects
    * @param invalidRecords
    */
  case class SuspiciousConnectsAnalysisResults(val suspiciousConnects: DataFrame, val invalidRecords: DataFrame)


}