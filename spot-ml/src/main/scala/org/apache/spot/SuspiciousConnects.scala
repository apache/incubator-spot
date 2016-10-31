package org.apache.spot

import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.dns.DNSSuspiciousConnectsAnalysis
import org.apache.spot.netflow.FlowSuspiciousConnectsAnalysis
import org.apache.spot.proxy.ProxySuspiciousConnectsAnalysis


/**
  * Top level entrypoint to execute suspicious connections analysis on network data.
  * Supported analyses:
  *  flow  : netflow data
  *  dns : DNS server logs
  *  proxy : proxy server logs
  */

object SuspiciousConnects {

  val OutputDelimiter = "\t"

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
        val sparkConfig = new SparkConf().setAppName("Spot ML:  " + analysis + " suspicious connects analysis")
        val sparkContext = new SparkContext(sparkConfig)
        val sqlContext = new SQLContext(sparkContext)
        implicit val outputDelimiter = OutputDelimiter

        analysis match {
          case "flow" => FlowSuspiciousConnectsAnalysis.run(config, sparkContext, sqlContext, logger)
          case "dns" => DNSSuspiciousConnectsAnalysis.run(config, sparkContext, sqlContext, logger)
          case "proxy" => ProxySuspiciousConnectsAnalysis.run(config, sparkContext, sqlContext, logger)
          case _ => logger.error("Unsupported (or misspelled) analysis: " + analysis)
        }

        sparkContext.stop()

      case None => logger.error("Error parsing arguments")
    }

    System.exit(0)
  }


}