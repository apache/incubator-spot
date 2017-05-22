package org.apache.spot.natetest


import org.apache.spot.netflow._
import org.apache.spot.SuspiciousConnectsArgumentParser._
import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.sql.types.{LongType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
/**
  * Top level entrypoint to execute suspicious connections analysis on network data.
  * Supported analyses:
  *  flow  : netflow data
  *  dns : DNS server logs
  *  proxy : proxy server logs
  */

object FlowTune {

  /**
    * Execute suspicious connections analysis on network data.
    *
    * @param args Command line arguments.
    */
  def main(args: Array[String]) {

    val logger = LogManager.getLogger("SuspiciousConnects: Flow Model Testing")

    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val sparkConfig = new SparkConf().setAppName("Netflow tuning")
    val sparkContext = new SparkContext(sparkConfig)
    val sqlContext = new SQLContext(sparkContext)

    val InputParquetFile = "/user/nlsegerl/2016.11.11.synthetic.batch1.labeled.pqt"
    val FeedbackFile     = ""
    val ScoredParquetFile = "/user/nlsegerl/test/2016.11.11.baseline.scored.pqt"


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


    val scoredDF = FlowSuspiciousConnectsAnalysis.run(config, sparkContext, sqlContext, logger, inputDataFrame).cache()

    println("let me show you ten: ")
    scoredDF.show(10)

    // let's do the ROC first

    val E = new BinaryClassificationMetrics(scoredDF.select("score", "is_attack").rdd.map({case (r: Row) => (r.getAs[Double](0), r.getAs[Double](1))}))

    val auc = E.areaUnderROC()

    println("Here's your area under the ROC curve:   " + auc)

    System.exit(0)

    println("and now we have a dataframe with all of the ranked attacks")

    val newSchema = StructType(scoredDF.schema.fields ++ Array(StructField("rank", LongType, false)))

    val newRowsRDD = scoredDF.rdd.zipWithIndex().map({case (r,i) => Row.fromSeq(r.toSeq ++ Array[Any](i))})

    val indexedAll = sqlContext.createDataFrame(newRowsRDD, newSchema)

    val indexedAttacks = indexedAll.filter(indexedAll("is_attack") === 1)

    indexedAttacks.show(20)
    // i guess I will need  to save this somewhere
  }


}