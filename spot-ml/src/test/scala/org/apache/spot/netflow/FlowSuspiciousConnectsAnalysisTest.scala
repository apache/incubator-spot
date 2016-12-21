package org.apache.spot.netflow

import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

case class FlowRecord(treceived: String,
                      tryear: Int,
                      trmonth: Int,
                      trday: Int,
                      trhour: Int,
                      trminute: Int,
                      trsec: Int,
                      tdur: Float,
                      sip: String,
                      dip: String,
                      sport: Int,
                      dport: Int,
                      proto: String,
                      ipkt: Int,
                      ibyt: Int,
                      opkt: Int,
                      obyt: Int)

class FlowSuspiciousConnectsAnalysisTest extends TestingSparkContextFlatSpec with Matchers {


  val testConfig = SuspiciousConnectsConfig(analysis = "flow",
    inputPath = "",
    feedbackFile = "",
    duplicationFactor = 1,
    topicCount = 20,
    hdfsScoredConnect = "",
    threshold = 1.0d,
    maxResults = 1000,
    outputDelimiter = "\t",
    ldaPRGSeed = None,
    ldaMaxiterations = 20,
    ldaAlpha = 1.02,
    ldaBeta = 1.001)

  "netflow suspicious connects" should "correctly identify time-of-day anomalies" in {

    val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
    logger.setLevel(Level.OFF)

    val anomalousRecord = FlowRecord("2016-05-05 00:11:01", 2016, 5, 5, 0, 0, 1, 0.972f, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39, 12522, 0, 0)
    val typicalRecord = FlowRecord("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972f, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39, 12522, 0, 0)


    val data = sqlContext.createDataFrame(Seq(anomalousRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord, typicalRecord,
      typicalRecord, typicalRecord, typicalRecord, typicalRecord))


    val scoredData : DataFrame = FlowSuspiciousConnectsAnalysis.detectFlowAnomalies(data,
      testConfig,
      sparkContext,
      sqlContext,
      logger)



    val anomalyScore = scoredData.filter(scoredData(Hour) === 0).first().getAs[Double](Score)
    val typicalScores = scoredData.filter(scoredData(Hour) === 13).collect().map(_.getAs[Double](Score))

    Math.abs(anomalyScore - 0.1d) should be < 0.01
    typicalScores.length shouldBe 9
    Math.abs(typicalScores(0) - 0.9d) should be < 0.01
    Math.abs(typicalScores(1) - 0.9d) should be < 0.01
    Math.abs(typicalScores(2) - 0.9d) should be < 0.01
    Math.abs(typicalScores(3) - 0.9d) should be < 0.01
    Math.abs(typicalScores(4) - 0.9d) should be < 0.01
    Math.abs(typicalScores(5) - 0.9d) should be < 0.01
    Math.abs(typicalScores(6) - 0.9d) should be < 0.01
    Math.abs(typicalScores(7) - 0.9d) should be < 0.01
    Math.abs(typicalScores(8) - 0.9d) should be < 0.01



  }

}
