package org.apache.spot.netflow.model

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spot.SuspiciousConnectsArgumentParser.SuspiciousConnectsConfig
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

class FlowSuspiciousConnectsModelTest extends TestingSparkContextFlatSpec with Matchers {

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

  def testFlowRecords = new {
    val sqlContext = new SQLContext(sparkContext)

    val inputFlowRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 24, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 59, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 59, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq(null, 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, null, "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", null, 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", null, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, null, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", null, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, null, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, null, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, null),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0))
      .map(row => Row.fromSeq(row))))

    val inputFlowRecordsSchema = StructType(
      Array(TimeReceivedField,
        YearField,
        MonthField,
        DayField,
        HourField,
        MinuteField,
        SecondField,
        DurationField,
        SourceIPField,
        DestinationIPField,
        SourcePortField,
        DestinationPortField,
        ProtocolField,
        IpktField,
        IbytField,
        OpktField,
        ObytField))

    val inputFlowRecordsDF = sqlContext.createDataFrame(inputFlowRecordsRDD, inputFlowRecordsSchema)

    val scoredFlowRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, -1d),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, 1d),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, 0.0000005),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, 0.05),
      Seq("2016-05-05 13:54:58", 2016, 5, 5, 13, 54, 58, 0.972, "172.16.0.129", "10.0.2.202", 1024, 80, "TCP", 39l, 12522l, 0, 0, 0.0001))
      .map(row => Row.fromSeq(row))))

    val scoredFlowRecordsSchema = StructType(
      Array(TimeReceivedField,
        YearField,
        MonthField,
        DayField,
        HourField,
        MinuteField,
        SecondField,
        DurationField,
        SourceIPField,
        DestinationIPField,
        SourcePortField,
        DestinationPortField,
        ProtocolField,
        IpktField,
        IbytField,
        OpktField,
        ObytField,
        ScoreField))

    val scoredFlowRecordsDF = sqlContext.createDataFrame(scoredFlowRecordsRDD, scoredFlowRecordsSchema)
  }
}
