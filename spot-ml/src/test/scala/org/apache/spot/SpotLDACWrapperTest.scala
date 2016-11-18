package org.apache.spot

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.functions._
import org.apache.spot.spotldacwrapper.SpotLDACWrapper
import org.apache.spot.spotldacwrapper.SpotLDACInput
import org.apache.spot.spotldacwrapper.SpotLDACSchema._
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers


class SpotLDACWrapperTest extends TestingSparkContextFlatSpec with Matchers{

  val logger = LogManager.getLogger("SuspiciousConnectsAnalysis")
  logger.setLevel(Level.INFO)


  "normalizeWord" should "calculate exponential of each value in the input string, then sum up all the exponential and " +
    "then divide  each exponential by the total sum" in {

    val wordProbability = "1 2 3 4 5"
    val result = SpotLDACWrapper.getWordProbabilitesFromTopicLine(wordProbability)

    result.length shouldBe 5
    result(0) shouldBe 0.011656230956039607
    result(1) shouldBe 0.03168492079612427
    result(2) shouldBe 0.0861285444362687
    result(3) shouldBe 0.23412165725273662
    result(4) shouldBe 0.6364086465588308
  }

  "getTopicDocument" should "return string of document and 20 values when the sum of each value in the line is bigger" +
    "than 0. Each result value should be divided by the sum of all values" in {
    val topicCount = 20
    val document = "192.168.1.1"
    val line = "0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 " +
      "0.0124531442 0.0124531442 0.0124531442 23983.5532262138 0.0124531442 0.0124531442 0.0124531442 0.0124531442 " +
      "0.0124531442 0.0124531442 22999.4716800747 0.0124531442"

    var topicMixOUT = SpotLDACWrapper.getDocumentToTopicProbabilityArray(line, topicCount)

    topicMixOUT shouldBe Array(2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7,
      2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7,
      2.6505498126219955E-7, 2.6505498126219955E-7, 0.5104702996191969, 2.6505498126219955E-7, 2.6505498126219955E-7,
      2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 0.48952492939114034,
      2.6505498126219955E-7)
  }

  it should "return string of document and 0.0 20 times when the sum of each value in the line is 0" in {
    val document = "192.168.1.1"
    val line = "0.0 0.0 1.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0"

    val topicCount = 20
    val topicMixOUT = SpotLDACWrapper.getDocumentToTopicProbabilityArray(line, topicCount)

    topicMixOUT shouldBe Array(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
  }

  "createModel" should "return model in Array[String] format. Each string should contain the document count and the" +
    "total count for each word" in {

    val testSqlContext = new org.apache.spark.sql.SQLContext(sparkContext)
    import testSqlContext.implicits._

    val documentWordData = sparkContext.parallelize(Array(SpotLDACInput("192.168.1.1", "333333_7.0_0.0_1.0", 8),
      SpotLDACInput("10.10.98.123", "1111111_6.0_3.0_5.0", 4),
      SpotLDACInput("66.23.45.11", "-1_43_7.0_2.0_6.0", 2),
      SpotLDACInput("192.168.1.1", "-1_80_6.0_1.0_1.0", 5)))

    val wordDictionary = Map("333333_7.0_0.0_1.0" -> 0,
      "1111111_6.0_3.0_5.0" -> 1,
      "-1_43_7.0_2.0_6.0" -> 2,
      "-1_80_6.0_1.0_1.0" -> 3)

    val modelDF: DataFrame = SpotLDACWrapper.createModel(documentWordData, wordDictionary, sparkContext, sqlContext, logger)

    val model = modelDF.select(col(DocumentNameWordNameWordCount)).rdd
      .map(
        x=> ( x.toString().replaceAll("\\[","").replaceAll("\\]","") )
      ).collect

    val documentDictionary = modelDF.select(col(DocumentName))
      .rdd
      .map(x=>x.toString.replaceAll("\\]","").replaceAll("\\[",""))
      .zipWithIndex.toDF(DocumentName, DocumentId)

    model should contain ("2 0:8 3:5")
    model should contain ("1 1:4")
    model should contain ("1 2:2")
    model shouldBe Array("1 2:2", "1 1:4", "2 0:8 3:5")

  }

  "getDocumentResults" should "return DataFrame with two columns. One for document (document_name) and the other for the " +
    "probabilities distribution (topic_prob_mix) when the sum of each value in the line is bigger" +
    "than 0. Each result value should be divided by the sum of all values and the result array order should be the same" +
    "as incoming data (topicDocument Data)" in {

    val testSqlContext = new org.apache.spark.sql.SQLContext(sparkContext)
    import testSqlContext.implicits._

    val topicCount = 20
    val documentDictionary = sparkContext.parallelize(Array
    (("10.10.98.123"->3), ( "66.23.45.11"->0), ( "192.168.1.1"->1 ), ("133.546.43.22" -> 2))).toDF(DocumentName,DocumentId)

    val topicDocumentData = Array("0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 " +
      "0.0124531442 0.0124531442 0.0124531442 0.0124531442 23983.5532262138 0.0124531442 0.0124531442 0.0124531442 " +
      "0.0124531442 0.0124531442 0.0124531442 22999.4716800747 0.0124531442",
    "0.0124531442 0.0124531442 45583.9162803520 0.0124531442 24896.2502172987 0.0124531442 0.0124531442 0.0124531442 " +
      "38659.6152616495 0.0124531442 0.0124531442 0.0124531442 0.0124531442 64804.4886288180 25162.0192468565 " +
      "0.0124531442 0.0124531442 36361.7850838895 0.0124531442 0.0124531442",
    "0.0124531442 0.0124531442 53816.0629371489 0.0124531442 24404.5440320289 0.0124531442 0.0124531442 0.0124531442 " +
      "28303.9934577136 0.0124531442 0.0124531442 0.0124531442 0.0124531442 71151.8773513492 26093.1297387104 " +
      "0.0124531442 0.0124531442 29142.4672019139 0.0124531442 0.0124531442",
    "0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 " +
      "0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 0.0124531442 12.0124531442 " +
      "0.0124531442 0.0124531442 0.0124531442 0.0124531442")

    val resultsDF = SpotLDACWrapper.getDocumentResults(topicDocumentData, documentDictionary, topicCount, sparkContext, testSqlContext)

    // Since DNS is still broadcasting ip to topic mix, we need to convert data frame to Map[String, Array[Double]]
    val ipToTopicMix = resultsDF
      .rdd
      .map({ case (ipToTopicMixRow: Row) => (ipToTopicMixRow.toSeq.toArray) })
      .map({
        case (ipToTopicMixSeq) => (ipToTopicMixSeq(0).asInstanceOf[String], ipToTopicMixSeq(1).asInstanceOf[Seq[Double]]
          .toArray)
      })
      .collectAsMap
      .toMap

    ipToTopicMix("66.23.45.11") shouldBe Array(2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7,
      2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7,
      2.6505498126219955E-7, 2.6505498126219955E-7, 0.5104702996191969, 2.6505498126219955E-7, 2.6505498126219955E-7,
      2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 2.6505498126219955E-7, 0.48952492939114034, 2.6505498126219955E-7)

    ipToTopicMix("192.168.1.1") shouldBe Array(5.28867235797652E-8, 5.28867235797652E-8, 0.19358837746391266, 5.28867235797652E-8,
      0.10573081643228267, 5.28867235797652E-8, 5.28867235797652E-8, 5.28867235797652E-8, 0.1641818606776375, 5.28867235797652E-8,
      5.28867235797652E-8, 5.28867235797652E-8, 5.28867235797652E-8, 0.27521540117076093, 0.10685949951637365, 5.28867235797652E-8,
      5.28867235797652E-8, 0.15442330432490242, 5.28867235797652E-8, 5.28867235797652E-8)

    ipToTopicMix("133.546.43.22") shouldBe Array(5.346710724792232E-8, 5.346710724792232E-8, 0.2310572464680428, 5.346710724792232E-8,
      0.1047799938827603, 5.346710724792232E-8, 5.346710724792232E-8, 5.346710724792232E-8, 0.12152213364300919, 5.346710724792232E-8,
      5.346710724792232E-8, 5.346710724792232E-8, 5.346710724792232E-8, 0.30548791503077616, 0.11202987324065357, 5.346710724792232E-8,
      5.346710724792232E-8, 0.12512208919525633, 5.346710724792232E-8, 5.346710724792232E-8)

    ipToTopicMix("10.10.98.123") shouldBe Array(0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624,
      0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624,
      0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624,
      0.0010166609738175624, 0.9806834414974662, 0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624, 0.0010166609738175624)
  }

  "getWordResults" should "return Array[String] with 20 elements and N word probability. The result array should have the" +
    "same order as the input data (topicWordData)" in {

    val wordDictionary = Map("-1_23.0_7.0_7.0_4.0" -> 3, "23.0_7.0_7.0_4.0" -> 0, "333333.0_7.0_7.0_4.0" -> 2, "80.0_7.0_7.0_4.0" -> 1)
    val topicWordData = Array("-15.3937807051 -536.5105632673 -532.4503365466 -10.8318318078",
    "-532.7002781469 -447.4890465231 -525.1690620213 -13.9180467610",
    "-19.5667065907 -5.4442353782 -8.0252973255 -12.4288410371",
    "-537.3544437825 -15.4983056878 -22.4722439197 -14.7817718408",
    "-13.3791705206 -5.3657121205 -9.6286562391 -10.8083455362",
    "-539.4466499733 -535.9756606126 -122.4199569787 -535.7615756409",
    "-14.9824699328 -532.0369117371 -527.9766850251 -11.1874512926",
    "-15.1889345209 -534.5750685563 -530.5148418392 -13.5374760178",
    "-20.4194054373 -5.3920297886 -9.0613037095 -11.5228207125",
    "-16.7232163605 -533.1029777067 -529.0427509894 -11.7742458954",
    "-15.4319201624 -533.2550983208 -529.1948716024 -10.7487557143",
    "-536.2164790868 -532.7454897639 -528.6852629847 -532.5314047894",
    "-14.9148351018 -535.3169147724 -531.2566880561 -10.9572759378",
    "-19.2062800183 -5.8636196300 -8.3638265558 -10.6807087581",
    "-20.4589237180 -5.2246884025 -8.1727002832 -10.7430483178",
    "-341.6123784319 -339.5204175204 -337.1106321483 -339.3909978330",
    "-534.8483200081 -531.3773307914 -527.3171040363 -17.0420121428",
    "-19.9682608636 -5.4274731739 -8.0755820355 -12.0653946611",
    "-535.7333037656 -532.2623144682 -528.2020876890 -532.0482294927",
    "-18.4350359818 -534.4612736041 -530.4010468817 -11.0784977885")

    val results = SpotLDACWrapper.getWordToProbPerTopicMap(topicWordData, wordDictionary)

    results.keySet.size shouldBe 4
    results("23.0_7.0_7.0_4.0").length shouldBe 20
    results("23.0_7.0_7.0_4.0")(0) shouldBe 0.010333787125924218
    results("23.0_7.0_7.0_4.0")(19) shouldBe 6.379973716629045E-4
    results("-1_23.0_7.0_7.0_4.0")(0) shouldBe 0.9896662128740757
    results("-1_23.0_7.0_7.0_4.0")(19) shouldBe 0.9993620026283371
  }

}