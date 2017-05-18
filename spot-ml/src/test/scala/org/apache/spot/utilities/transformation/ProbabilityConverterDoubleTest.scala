package org.apache.spot.utilities.transformation

import org.apache.spark.sql.types._
import org.apache.spot.lda.SpotLDAWrapperSchema.{DocumentName, TopicProbabilityMix}
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

/**
  * Created by rabarona on 5/17/17.
  */
class ProbabilityConverterDoubleTest extends TestingSparkContextFlatSpec with Matchers {

  "convertProbability" should "just return the same value with the same type" in {
    val testValue: Double = 5d

    val result = ProbabilityConverterDouble.convertProbability(testValue)

    result shouldBe testValue
    result shouldBe a[java.lang.Double]

  }

  "convertBackSetOfProbabilities" should "return a array of the same type" in {

    val testSeq: Seq[Double] = Seq(1d, 2d, 3d)

    val result = ProbabilityConverterDouble.convertBackSetOfProbabilities(testSeq)

    result shouldBe a[Array[Double]]
    result.length shouldBe 3
  }

  "convertDataFrameColumn" should "return the exact same data frame" in {

    val testDataFrame = sqlContext.createDataFrame(Seq(("doc1", Array(1d, 2d)), ("doc2", Array(2d, 3d))))
      .withColumnRenamed("_1", DocumentName).withColumnRenamed("_2", TopicProbabilityMix)

    val result = ProbabilityConverterDouble.convertDataFrameColumn(testDataFrame, TopicProbabilityMix)

    val schema = StructType(
      Array(StructField(DocumentName, StringType, true),
        StructField(TopicProbabilityMix, ArrayType(DoubleType, false), true)))

    result shouldBe testDataFrame
    result.schema shouldBe schema
  }

}
