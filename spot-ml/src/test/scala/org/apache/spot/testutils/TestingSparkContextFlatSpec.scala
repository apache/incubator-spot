/**
  * THIS CODE WAS COPIED DIRECTLY FROM THE OPEN SOURCE PROJECT TAP (Trusted Analytics Platform)
  * which has an Apache V2.0
  */
package org.apache.spot.testutils

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.scalatest.{BeforeAndAfter, FlatSpec}

trait TestingSparkContextFlatSpec extends FlatSpec with BeforeAndAfter {

  var sparkContext: SparkContext = null
  var sqlContext : SQLContext = null

  before {
    sparkContext = TestingSparkContext.sparkContext
    sqlContext = new SQLContext(sparkContext)
  }

  /**
    * Clean up after the test is done
    */
  after {
    TestingSparkContext.cleanUp()
    sparkContext = null
  }

}
