

import org.apache.spot.netflow._
import org.apache.spot.SuspiciousConnectsArgumentParser._
import org.apache.log4j.{Level, LogManager, Logger}
val logger = LogManager.getLogger("SuspiciousConnects: Flow Model Testing")

val InputParquetFile = "/user/nlsegerl/2016.11.11.synthetic.batch1.labeled.pqt"
val FeedbackFile     = ""
val ScoredParquetFile = "/user/nlsegerl/test/2016.11.11/baseline.scored.pqt"


val inputDataFrame = sqlContext.read.parquet(InputParquetFile)

val config = SuspiciousConnectsConfig(analysis = "flow",
  inputPath = InputParquetFile,
  feedbackFile = FeedbackFile,
  duplicationFactor  = 1000,
  topicCount = 20,
  userDomain = "intel",
  hdfsScoredConnect = ScoredParquetFile,
  threshold = 1.0d,
  maxResults = -1,
  outputDelimiter = "\t",
  ldaPRGSeed = Some(0xdeadbeef),
  ldaMaxiterations = 20,
  ldaAlpha = 1.02,
  ldaBeta = 1.001)


val scoredDF = FlowSuspiciousConnectsAnalysis.run(config, sc, sqlContext, logger, inputDataFrame).cache()

// there are two big tasks .... rank the attacks and calculate the area under the ROC curve

