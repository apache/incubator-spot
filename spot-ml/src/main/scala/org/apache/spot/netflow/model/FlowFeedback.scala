package org.apache.spot.netflow.model

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.netflow.model.FlowSuspiciousConnectsModel._
import scala.io.Source

/**
  * Routines for ingesting the feedback file provided by the operational analytics layer.
  *
  */
object FlowFeedback {


  /**
    * Load the feedback file for netflow data.
    *
    * @param sc                Spark context.
    * @param sqlContext        Spark SQL context.
    * @param feedbackFile      Local machine path to the netflow feedback file.
    * @param duplicationFactor Number of words to create per flagged feedback entry.
    * @return DataFrame of the feedback events.
    */
  def loadFeedbackDF(sc: SparkContext,
                     sqlContext: SQLContext,
                     feedbackFile: String,
                     duplicationFactor: Int): DataFrame = {


    if (new java.io.File(feedbackFile).exists) {

      /*
      feedback file is a tab-separated file with a single header line.
      */

      val lines = Source.fromFile(feedbackFile).getLines().toArray.drop(1)
      val feedback: RDD[String] = sc.parallelize(lines)

      /*
         flow_scores.csv - feedback file structure
         0	sev
         1	tstart
         2	srcIP
         3	dstIP
         4	sport
         5	dport
         6	ipkt
         7	ibyt
        */

      // Given the structure pull out indexes we need for a new DataFrame creation
      // containing the columns for word creation only.

      val ScoreIndex = 0
      val TimeStartIndex = 1
      val SourceIpIndex = 2
      val DestinationIpIndex = 3
      val SourcePortIndex = 4
      val DestinationPortIndex = 5
      val IpktIndex = 6
      val IbytIndex = 7
      val HourIndex = 20
      val MinuteIndex = 21
      val SecondIndex = 22

      sqlContext.createDataFrame(feedback.map(_.split("\t"))
        .filter(row => row(ScoreIndex).trim.toInt == 3)
        .map(row => Row.fromSeq(Seq(
          row(TimeStartIndex).split(" ")(1).split(":")(0).trim.toInt, // hour
          row(TimeStartIndex).split(" ")(1).split(":")(1).trim.toInt, // minute
          row(TimeStartIndex).split(" ")(1).split(":")(2).trim.toInt, // second
          row(SourceIpIndex),
          row(DestinationIpIndex),
          row(SourcePortIndex),
          row(DestinationPortIndex),
          row(IpktIndex).trim.toLong,
          row(IbytIndex).trim.toLong)))
        .flatMap(row => List.fill(duplicationFactor)(row)), ModelSchema)
        .select(ModelColumns: _*)

    } else {
      sqlContext.createDataFrame(sc.emptyRDD[Row], ModelSchema)
    }
  }
}
