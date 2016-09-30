
package org.apache.spot.netflow

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.SpotLDACWrapper.SpotLDACInput
import org.apache.spot.netflow.FlowSchema._

import scala.io.Source


/**
  * Contains routines for creating the "words" for a suspicious connects analysis from incoming netflow records.
  */
object FlowPreLDA {

  def flowPreLDA(inputPath: String, scoresFile: String, duplicationFactor: Int,
                 sc: SparkContext, sqlContext: SQLContext, logger: Logger ): RDD[SpotLDACInput] = {

    logger.info("Flow pre LDA starts")

    import sqlContext.implicits._
    val scoredFile = scoresFile
    logger.info("scoredFile:  " + scoredFile)

    val scoredFileExists = if (scoredFile != "") new java.io.File(scoredFile).exists else false

    val falsePositives: DataFrame = if (scoredFileExists) {

      /*
        flow_scores.csv - feedback file structure
        0	sev
        1	tstart
        2	srcIP
        3	dstIP
        4	sport
        5	dport
        6	proto
        7	flag
        8	ipkt
        9	ibyt
        10	lda_score
        11	rank
        12	srcIpInternal
        13	destIpInternal
        14	srcGeo
        15	dstGeo
        16	srcDomain
        17	dstDomain
        18	srcIP_rep
        19	dstIP_rep
       */

      // Given the structure pull out indexes we need for a new DataFrame creation
      // containing the columns for word creation only.

      val scoreIndex = 0
      val timeStartIndex = 1
      val sourceIpIndex = 2
      val destinationIpIndex = 3
      val sourcePortIndex = 4
      val destinationPortIndex = 5
      val ipktIndex = 8
      val ibytIndex = 9
      val hourIndex = 20
      val minuteIndex = 21
      val secondIndex = 22


      logger.info("Duplication factor: " + duplicationFactor)
      val rowsToDuplicate = Source.fromFile(scoredFile)
        .getLines()
        .toArray
        .drop(1)

      logger.info("User feedback read from: " + scoredFile + ". "
        + rowsToDuplicate.length + " many connections flagged nonthreatening.")

      val feedback: RDD[String] = sc.parallelize(rowsToDuplicate)

      feedback.map(_.split("\t"))
        .filter(row => row.length == 22 && row(scoreIndex).trim.toInt == 3)
        .map(row => row :+
          row(timeStartIndex).split(" ")(1).split(":")(0) :+
          row(timeStartIndex).split(" ")(1).split(":")(1) :+
          row(timeStartIndex).split(" ")(1).split(":")(2))
        .map(row => Feedback(row(hourIndex).trim.toInt,
          row(minuteIndex).trim.toInt,
          row(secondIndex).trim.toInt,
          row(sourceIpIndex),
          row(destinationIpIndex),
          row(sourcePortIndex),
          row(destinationPortIndex),
          row(ipktIndex).trim.toLong,
          row(ibytIndex).trim.toLong))
        .flatMap(row => List.fill(duplicationFactor)(row))
        .toDF()
    } else {
      null
    }

    logger.info("Trying to read file:  " + inputPath)
    val rawdata: DataFrame = {
      sqlContext.read.parquet(inputPath)
        .filter(Hour + " BETWEEN 0 AND 23 AND  " +
          Minute + " BETWEEN 0 AND 59 AND  " +
          Second + " BETWEEN 0 AND 59")
        .select(Hour,
          Minute,
          Second,
          SourceIP,
          DestinationIP,
          SourcePort,
          DestinationPort,
          ipkt,
          ibyt)
    }

    val totalDataDF: DataFrame = {
      if (!scoredFileExists) {
        rawdata
      } else {
        rawdata.unionAll(falsePositives)
      }
    }

    val dataWithWordDF = FlowWordCreation.flowWordCreation(totalDataDF, sc, logger, sqlContext)

    val src_word_counts = dataWithWordDF.select(SourceIP, SourceWord)
      .map({ case Row(sourceIp: String, sourceWord: String) => (sourceIp, sourceWord) -> 1 })
      .reduceByKey(_ + _)

    val dest_word_counts = dataWithWordDF.select(DestinationIP, DestinationWord)
      .map({ case Row(destinationIp: String, destinationWord: String) => (destinationIp, destinationWord) -> 1 })
      .reduceByKey(_ + _)

    val word_counts = sc.union(src_word_counts, dest_word_counts).map({case ((ip, word), count) => SpotLDACInput(ip, word, count)})

    logger.info("Flow pre LDA completed")
    word_counts

  }

  case class Feedback(hour: Int,
                      minute: Int,
                      second: Int,
                      sourceIp: String,
                      destinationIp: String,
                      sourcePort: String,
                      destinationPort: String,
                      ipkt: Long,
                      ibyt: Long) extends Serializable

}
