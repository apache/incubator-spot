

import org.apache.spot.netflow._
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.SuspiciousConnectsArgumentParser._
import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spot.netflow.model.FlowSuspiciousConnectsModel

val logger = LogManager.getLogger("SuspiciousConnects: Flow Model Testing")

Logger.getLogger("org").setLevel(Level.WARN)
Logger.getLogger("akka").setLevel(Level.OFF)

val InputParquetFile = "/user/duxbury/flow/hive/y=2016/m=05/d=05"
//val InputParquetFile = "/user/nlsegerl/2016.11.11.synthetic.batch1.labeled.pqt"
val FeedbackFile     = ""
val ScoredParquetFile = "/user/nlsegerl/test/2016.05.05.baseline.scored.pqt"


val inputDataFrame = sqlContext.read.parquet(InputParquetFile)

val config = SuspiciousConnectsConfig(analysis = "flow",
  inputPath = InputParquetFile,
  feedbackFile = FeedbackFile,
  duplicationFactor  = 1000,
  topicCount = 10,
  userDomain = "intel",
  hdfsScoredConnect = ScoredParquetFile,
  threshold = 1.0d,
  maxResults = -1,
  outputDelimiter = "\t",
  ldaPRGSeed = Some(0xdeadbeef),
  ldaMaxiterations = 5,
  ldaAlpha = 1.02,
  ldaBeta = 1.001)



logger.info("Starting flow suspicious connects analysis.")

val cleanFlows : DataFrame = FlowSuspiciousConnectsModel.cleanData(inputDataFrame) // ? should we cache here?

logger.info("Fitting model to data")

val model = FlowSuspiciousConnectsModel.trainModel(sc, sqlContext, logger, config, cleanFlows)

logger.info("Assigning scores to flow records")

val scoredFlowRecords = model.score(sc, sqlContext, cleanFlows)

logger.info("Selecting most suspicious flow records")

val orderedFlowRecords = scoredFlowRecords.filter(scoredFlowRecords(Score) < 0.0001).orderBy(Score)



orderedFlowRecords.show(10)


val df = orderedFlowRecords



