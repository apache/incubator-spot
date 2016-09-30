package org.apache.spot.proxy

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.types.{StructType, StructField, StringType}
import scala.io.Source

import org.apache.spot.proxy.ProxySchema._


object ProxyFeedback {

  /**
    * Load the feedback file for proxy data.
 *
    * @param sc Spark context.
    * @param sqlContext Spark SQL context.
    * @param feedbackFile Local machine path to the proxy feedback file.
    * @param duplicationFactor Number of words to create per flagged feedback entry.
    * @return DataFrame of the feedback events.
    */
  def loadFeedbackDF(sc: SparkContext,
                     sqlContext: SQLContext,
                     feedbackFile: String,
                     duplicationFactor: Int): DataFrame = {


    val feedbackSchema = StructType(
      List(StructField(Date, StringType, nullable= true),
        StructField(Time, StringType, nullable= true),
        StructField(ClientIP, StringType, nullable= true),
        StructField(Host, StringType, nullable= true),
        StructField(ReqMethod, StringType, nullable= true),
        StructField(UserAgent, StringType, nullable= true),
        StructField(ResponseContentType, StringType, nullable= true),
        StructField(RespCode, StringType, nullable= true),
        StructField(FullURI, StringType, nullable= true)))

    if (new java.io.File(feedbackFile).exists) {

      val dateIndex = 0
      val timeIndex = 1
      val clientIpIndex = 2
      val hostIndex = 3
      val reqMethodIndex = 4
      val userAgentIndex = 5
      val resContTypeIndex = 6
      val respCodeIndex = 11
      val fullURIIndex = 18

      val fullURISeverityIndex = 22

      val lines = Source.fromFile(feedbackFile).getLines().toArray.drop(1)
      val feedback: RDD[String] = sc.parallelize(lines)

      sqlContext.createDataFrame(feedback.map(_.split("\t"))
        .filter(row => row(fullURISeverityIndex).trim.toInt == 3)
        .map(row => Row.fromSeq(List(row(dateIndex),
          row(timeIndex),
          row(clientIpIndex),
          row(hostIndex),
          row(reqMethodIndex),
          row(userAgentIndex),
          row(resContTypeIndex),
          row(respCodeIndex),
          row(fullURIIndex))))
        .flatMap(row => List.fill(duplicationFactor)(row)), feedbackSchema)
        .select(Date, Time, ClientIP, Host, ReqMethod, UserAgent, ResponseContentType, RespCode, FullURI)
    } else {
      sqlContext.createDataFrame(sc.emptyRDD[Row], feedbackSchema)
    }
  }
}
