package org.apache.spot.netflow

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spot.netflow.FlowSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

/**
  * Created by rabarona on 12/15/16.
  */
class FlowSuspiciousCoonectsAnalysisTest extends TestingSparkContextFlatSpec with Matchers{

  "filterAndSelectCleanFlowRecords" should "return data set without garbage" in {

    val cleanedFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterAndSelectCleanFlowRecords(testFlowRecords.inputFlowRecordsDF)

    cleanedFlowRecords.count should be(5)
    cleanedFlowRecords.schema.size should be(17)
  }

  "filterAndSelectInvalidFlowRecords" should "return invalid records" in {

    val invalidFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterAndSelectInvalidFlowRecords(testFlowRecords.inputFlowRecordsDF)

    invalidFlowRecords.count should be(7)
    invalidFlowRecords.schema.size should be(17)
  }

  "filterScoredFlowRecords" should "return records with score less or equal to threshold" in {

    val threshold = 10e-5

    val scoredFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterScoredFlowRecords(testFlowRecords.scoredFlowRecordsDF, threshold)

    scoredFlowRecords.count should be(2)
  }

  "filterAndSelectCorruptFlowRecords" should "return records where Score is equal to -1" in {

    val corruptFlowRecords = FlowSuspiciousConnectsAnalysis
      .filterAndSelectCorruptFlowRecords(testFlowRecords.scoredFlowRecordsDF)

    corruptFlowRecords.count should be(1)
    corruptFlowRecords.schema.size should be(18)
  }

  def testFlowRecords = new {
    val sqlContext = new SQLContext(sparkContext)

    val inputFlowRecordsRDD = sparkContext.parallelize(wrapRefArray(Array(
      Seq("2016-05-05 13:54:58",2016,5,5,24,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,60,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,60,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0),
      Seq(null,2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,null,"10.0.2.202",1024,80,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129",null,1024,80,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",null,80,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,null,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",null,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,null,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,null,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,null),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0))
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
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0, -1d),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0, 1d),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0, 0.0000005),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0, 0.05),
      Seq("2016-05-05 13:54:58",2016,5,5,13,54,58,0.972,"172.16.0.129","10.0.2.202",1024,80,"TCP",39l,12522l,0,0,0.0001))
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
