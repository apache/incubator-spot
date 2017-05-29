package org.apache.spot.utilities.transformation

import org.apache.spark.sql.types._
import org.apache.spot.lda.SpotLDAWrapperSchema.{DocumentName, TopicProbabilityMix}
import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers

/**
  * Created by rabarona on 5/17/17.
  */
class PrecisionUtilityFloatTest extends TestingSparkContextFlatSpec with Matchers {

  "toTargetType" should "just return value converted to Float" in {
    val testValue: Double = 5d

    val result = PrecisionUtilityFloat.toTargetType(testValue)

    result shouldBe 5f
    result shouldBe a[java.lang.Float]

  }

  "toDoubles" should "convert a Seq of Float to Seq of Double" in {

    val testSeq: Seq[Float] = Seq(1f, 2f, 3f)

    val result: Seq[Double] = PrecisionUtilityFloat.toDoubles(testSeq)

    result(0) shouldBe a[java.lang.Double]
    result(1) shouldBe a[java.lang.Double]
    result(2) shouldBe a[java.lang.Double]
  }

  "castColumn" should "return a data frame with a schema modified, Seq[Float] instead of Seq[Double]" in {

    val testDataFrame = sqlContext.createDataFrame(Seq(("doc1", Array(1d, 2d)), ("doc2", Array(2d, 3d))))
      .withColumnRenamed("_1", DocumentName).withColumnRenamed("_2", TopicProbabilityMix)

    val result = PrecisionUtilityFloat.castColumn(testDataFrame, TopicProbabilityMix)
    result.count

    val schema = StructType(
      Array(StructField(DocumentName, StringType, true),
        StructField(TopicProbabilityMix, ArrayType(FloatType, false), true)))

    result.schema shouldBe schema

  }
}
