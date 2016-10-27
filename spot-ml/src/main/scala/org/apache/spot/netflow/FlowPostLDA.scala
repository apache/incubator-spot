package org.apache.spot.netflow

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.netflow.FlowSchema._

/**
  * Contains routines for scoring incoming netflow records from a netflow suspicious connections model.
  */
object FlowPostLDA {

  def flowPostLDA(inputPath: String,
                  resultsFilePath: String,
                  outputDelimiter: String,
                  threshold: Double, topK: Int,
                  docToTopicMix: Map[String, Array[Double]],
                  wordToProbPerTopic: Map[String, Array[Double]],
                  topicCount: Int,
                  sc: SparkContext,
                  sqlContext: SQLContext,
                  logger: Logger) = {

    logger.info("loading machine learning results")

    import sqlContext.implicits._
    logger.info("loading data")
    val totalDataDF: DataFrame = {
      sqlContext.read.parquet(inputPath)
        .filter(Hour + " BETWEEN 0 AND 23 AND  " +
          Minute + " BETWEEN 0 AND 59 AND  " +
          Second + " BETWEEN 0 AND 59")
        .select(TimeReceived,
          Year,
          Month,
          Day,
          Hour,
          Minute,
          Second,
          Duration,
          SourceIP,
          DestinationIP,
          SourcePort,
          DestinationPort,
          proto,
          Flag,
          fwd,
          stos,
          ipkt,
          ibyt,
          opkt,
          obyt,
          input,
          output,
          sas,
          das,
          dtos,
          dir,
          rip)
    }

    val dataWithWord = FlowWordCreation.flowWordCreation(totalDataDF, sc, logger, sqlContext)

    logger.info("Computing conditional probability")

    val docToTopicMixRDD: RDD[(String, Array[Double])] = sc.parallelize(docToTopicMix.toSeq)

    val docToTopicMixDF = docToTopicMixRDD.map({ case (doc, probabilities) => DocTopicMix(doc, probabilities) }).toDF

    val words = sc.broadcast(wordToProbPerTopic)

    val dataWithSrcScore = score(sc, dataWithWord, docToTopicMixDF, words, SourceScore, SourceIP, SourceProbabilities, SourceWord, topicCount)
    val dataWithDestScore = score(sc, dataWithSrcScore, docToTopicMixDF, words, DestinationScore, DestinationIP, DestinationProbabilities, DestinationWord, topicCount)
    val dataScored = minimumScore(dataWithDestScore)

    logger.info("Persisting data")
    val filteredDF = dataScored.filter(MinimumScore + " <= " + threshold)
    filteredDF.orderBy(MinimumScore).limit(topK).rdd.map(row => Row.fromSeq(row.toSeq.dropRight(1))).map(_.mkString(outputDelimiter)).saveAsTextFile(resultsFilePath)

    logger.info("Flow post LDA completed")
  }

  def score(sc: SparkContext,
            dataFrame: DataFrame,
            docToTopicMixesDF: DataFrame,
            wordToProbPerTopic: Broadcast[Map[String, Array[Double]]],
            scoreColumnName: String,
            ipColumnName: String,
            ipProbabilitiesColumnName: String,
            wordColumnName: String,
            topicCount: Int): DataFrame = {

    val dataWithIpProbJoin = dataFrame.join(docToTopicMixesDF, dataFrame(ipColumnName) === docToTopicMixesDF(Doc))

    var newSchemaColumns = dataFrame.schema.fieldNames :+ Probabilities + " as " + ipProbabilitiesColumnName
    val dataWithIpProb = dataWithIpProbJoin.selectExpr(newSchemaColumns: _*)

    def scoreFunction(word: String, ipProbabilities: Seq[Double], topicCount: Int): Double = {
      val uniformProb = Array.fill(topicCount)(0.0d)
      val wordGivenTopicProb = wordToProbPerTopic.value.getOrElse(word, uniformProb)

      ipProbabilities.zip(wordGivenTopicProb)
        .map({ case (pWordGivenTopic, pTopicGivenDoc) => pWordGivenTopic * pTopicGivenDoc })
        .sum
    }

    def udfScoreFunction = udf((word: String, ipProbabilities: Seq[Double]) => scoreFunction(word, ipProbabilities, topicCount))

    val result: DataFrame = dataWithIpProb.withColumn(scoreColumnName, udfScoreFunction(dataWithIpProb(wordColumnName), dataWithIpProb(ipProbabilitiesColumnName)))
    newSchemaColumns = dataFrame.schema.fieldNames :+ scoreColumnName
    result.select(newSchemaColumns.map(col): _*)
  }

  def minimumScore(dataWithDestScore: DataFrame): DataFrame = {

    def minimumScoreFunction(sourceScore: Double, destinationScore: Double): Double = {
      scala.math.min(sourceScore, destinationScore)
    }

    def udfMinimumScoreFunction = udf((sourceScore: Double, destinationScore: Double) =>
      minimumScoreFunction(sourceScore, destinationScore))

    dataWithDestScore.withColumn(MinimumScore,
      udfMinimumScoreFunction(dataWithDestScore(SourceScore), dataWithDestScore(DestinationScore)))
  }

  case class DocTopicMix(doc: String, probabilities: Array[Double]) extends Serializable
}
