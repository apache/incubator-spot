package org.apache.spot

import org.apache.spark.rdd.RDD
import org.apache.spot.utilities.Quantiles
import org.scalatest.Matchers
import testutils.TestingSparkContextFlatSpec


class QuantilesTest extends TestingSparkContextFlatSpec with Matchers {

  val allOnes = List(1.0, 1.0, 1.0, 1.0, 1.0)
  val onesAndTwos = List(1.0, 2.0, 1.0, 2.0)
  val countToTen = List(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)


  "ecdf" should "work on an empty list" in {
    val rddIn: RDD[Double] = sparkContext.parallelize(List())
    val rddOut = Quantiles.computeEcdf(rddIn)

    val out = rddOut.collect()

    out.length shouldBe 0
  }

  "ecdf" should "work on a constant list" in {
    val rddIn = sparkContext.parallelize(allOnes)
    val rddOut = Quantiles.computeEcdf(rddIn)

    val out = rddOut.collect()

    out.length shouldBe 1
    out(0) shouldBe(1.0, 1.0)
  }

  "ecdf" should "work on a split 50/50 list" in {
    val rddIn = sparkContext.parallelize(onesAndTwos)
    val rddOut = Quantiles.computeEcdf(rddIn)

    val out = rddOut.collect()

    out.length shouldBe 2
    out(0) shouldBe(1.0, 0.5)
    out(1) shouldBe(2.0, 1.0)
  }

  "ecdf" should "work on count-to-ten list" in {
    val rddIn = sparkContext.parallelize(countToTen)
    val rddOut = Quantiles.computeEcdf(rddIn)

    val out = rddOut.collect()

    out.length shouldBe 10
    out(0) shouldBe(1.0, 0.1)
    out(1) shouldBe(2.0, 0.2)
    out(2) shouldBe(3.0, 0.3)
    out(3) shouldBe(4.0, 0.4)
    out(4) shouldBe(5.0, 0.5)
    out(5) shouldBe(6.0, 0.6)
    out(6) shouldBe(7.0, 0.7)
    out(7) shouldBe(8.0, 0.8)
    out(8) shouldBe(9.0, 0.9)
    out(9) shouldBe(10.0, 1.0)
  }

  "quantiles" should "work on an empty list" in {
    val rddIn: RDD[Double] = sparkContext.parallelize(List())
    val quantiles = Array(0.0, 0.5)
    val out = Quantiles.computeQuantiles(rddIn, quantiles)

    out.length shouldBe 2
    out(0) shouldBe Double.PositiveInfinity
    out(1) shouldBe Double.PositiveInfinity
  }

  "quantiles" should "work on all ones list" in {
    val rddIn = sparkContext.parallelize(allOnes)
    val quantiles = Array(0.0, 0.5)
    val out = Quantiles.computeQuantiles(rddIn, quantiles)

    out.length shouldBe 2
    out(0) shouldBe 1.0
    out(1) shouldBe 1.0
  }

  "quantiles" should "work on a 50/50 1s and 2s list" in {
    val rddIn = sparkContext.parallelize(onesAndTwos)
    val quantiles = Array(0.0, 0.5)
    val out = Quantiles.computeQuantiles(rddIn, quantiles)

    out.length shouldBe 2
    out(0) shouldBe 1.0
    out(1) shouldBe 1.0
  }

  "quantiles" should "work on a 50/50 1s and 2s list with unbalanced thresholds" in {
    val rddIn = sparkContext.parallelize(onesAndTwos)
    val quantiles = Array(0.0, 0.6, 0.99)
    val out = Quantiles.computeQuantiles(rddIn, quantiles)

    out.length shouldBe 3
    out(0) shouldBe 1.0
    out(1) shouldBe 2.0
    out(2) shouldBe 2.0
  }

  "quantiles" should "work on a count-to-ten list" in {
    val rddIn = sparkContext.parallelize(countToTen)
    val quantiles = Array(0.0, 0.5)
    val out = Quantiles.computeQuantiles(rddIn, quantiles)

    out.length shouldBe 2
    out(0) shouldBe 1.0
    out(1) shouldBe 5.0

  }

  "deciles" should "work on a count-to-ten list" in {
    val rddIn = sparkContext.parallelize(countToTen)
    val out = Quantiles.computeDeciles(rddIn)

    out.length shouldBe 10
    out(0) shouldBe 1.0
    out(1) shouldBe 2.0
    out(2) shouldBe 3.0
    out(3) shouldBe 4.0
    out(4) shouldBe 5.0
    out(5) shouldBe 6.0
    out(6) shouldBe 7.0
    out(7) shouldBe 8.0
    out(8) shouldBe 9.0
    out(9) shouldBe 10.0
  }

  "quintiles" should "work on a count-to-ten list" in {
    val rddIn = sparkContext.parallelize(countToTen)
    val out = Quantiles.computeQuintiles(rddIn)

    out.length shouldBe 5
    out(0) shouldBe 2.0
    out(1) shouldBe 4.0
    out(2) shouldBe 6.0
    out(3) shouldBe 8.0
    out(4) shouldBe 10.0
  }

    "bin" should "return 3 when the value is not bigger than the fourth quintile" in {
      val quintiles = Array(1.0, 2.0, 3.0, 4.0, 5.0)

      val result = Quantiles.bin(3.5, quintiles)

      result shouldBe 3
    }

    it should "return 0 when the value is less than the first quintile" in {
      val quintiles = Array(1.0, 2.0, 3.0, 4.0, 5.0)

      val result = Quantiles.bin(0.0, quintiles)

      result shouldBe 0
    }
}