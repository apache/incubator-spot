package org.apache.spot.dns.model

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.dns.model.DNSSuspiciousConnectsModel.{ModelSchema, modelColumns}

import scala.io.Source

/**
  * Routines for ingesting the feedback file provided by the operational analytics layer.
  *
  */
object DNSFeedback {

  /**
    * Load the feedback file for DNS data.
 *
    * @param sc Spark context.
    * @param sqlContext Spark SQL context.
    * @param feedbackFile Local machine path to the DNS feedback file.
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
      The columns and their entries are thus:

      0   frame_time             object
      1   frame_len              object
      2   ip_dst                 object
      3   dns_qry_name           object
      4   dns_qry_class           int64
      5   dns_qry_type            int64
      6   dns_qry_rcode           int64
      7   domain                 object
      8   subdomain              object
      9   subdomain_length        int64
      10  num_periods             int64
      11  subdomain_entropy     float64
      12  top_domain              int64
      13  word                   object
      14  score                 float64
      15  query_rep              object
      16  hh                      int64
      17  ip_sev                  int64
      18  dns_sev                 int64
      19  dns_qry_class_name     object
      20  dns_qry_type_name      object
      21  dns_qry_rcode_name     object
      22  network_context       float64
      23  unix_tstamp
      */
      val FrameTimeIndex = 0
      val UnixTimeStampIndex = 23
      val FrameLenIndex = 1
      val IpDstIndex = 2
      val DnsQryNameIndex = 3
      val DnsQryClassIndex = 4
      val DnsQryTypeIndex = 5
      val DnsQryRcodeIndex = 6
      val DnsSevIndex = 18
      val fullURISeverityIndex = 22


      sqlContext.createDataFrame(feedback.map(_.split("\t"))
        .filter(row => row(DnsSevIndex).trim.toInt == 3)
        .map(row => Row.fromSeq(Seq(row(FrameTimeIndex),
          row(UnixTimeStampIndex),
          row(FrameLenIndex),
          row(IpDstIndex),
          row(DnsQryNameIndex),
          row(DnsQryClassIndex),
          row(DnsQryTypeIndex),
          row(DnsQryRcodeIndex))))
        .flatMap(row => List.fill(duplicationFactor)(row)), ModelSchema)
        .select(modelColumns:_*)
    } else {
      sqlContext.createDataFrame(sc.emptyRDD[Row], ModelSchema)
    }
  }
}
