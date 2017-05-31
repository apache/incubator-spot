package org.apache.spot.utilities

import org.apache.spark.sql.types._
import org.apache.spot.lda.SpotLDAWrapperSchema.{DocumentName, TopicProbabilityMix}
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

/**
  * Created by rabarona on 5/17/17.
  */
class FloatingPointUtility64 extends TestingSparkContextFlatSpec with Matchers {

  "toTargetType" should "just return the same value with the same type" in {
    val testValue: Double = 5d

    val result = FloatPointPrecisionUtility64.toTargetType(testValue)

    result shouldBe testValue
    result shouldBe a[java.lang.Double]

  }

  "toDoubles" should "return an array of the same type" in {

    val testSeq: Seq[Double] = Seq(1d, 2d, 3d)

    val result: Seq[Double] = FloatPointPrecisionUtility64.toDoubles(testSeq)

    result shouldBe a[Seq[Double]]
    result.length shouldBe 3
  }

  "castColumn" should "return the exact same data frame" in {

    val testDataFrame = sqlContext.createDataFrame(Seq(("doc1", Array(1d, 2d)), ("doc2", Array(2d, 3d))))
      .withColumnRenamed("_1", DocumentName).withColumnRenamed("_2", TopicProbabilityMix)

    val result = FloatPointPrecisionUtility64.castColumn(testDataFrame, TopicProbabilityMix)

    val schema = StructType(
      Array(StructField(DocumentName, StringType, true),
        StructField(TopicProbabilityMix, ArrayType(DoubleType, false), true)))

    result shouldBe testDataFrame
    result.schema shouldBe schema
  }

}
