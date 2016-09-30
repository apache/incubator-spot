package org.apache.spot.testutils

import java.util.Date

import org.apache.spark.{SparkConf, SparkContext}

import scala.concurrent.Lock


/**
  * THIS CODE WAS COPIED DIRECTLY FROM THE OPEN SOURCE PROJECT TAP (Trusted Analytics Platform)
  * which has an Apache V2.0
  */

/**
  * Don't use this class directly!!  Use the FlatSpec or WordSpec version for your tests
  *
  * TestingSparkContext supports two basic modes:
  *
  * 1. shared SparkContext for all tests - this is fast
  * 2. starting and stopping SparkContext for every test - this is slow but more independent
  *
  * You can't have more than one local SparkContext running at the same time.
  */
private[testutils] object TestingSparkContext {

  /** lock allows non-Spark tests to still run concurrently */
  private val lock = new Lock()

  /** global SparkContext that can be re-used between tests */
  private lazy val sc: SparkContext = createLocalSparkContext()

  /** System property can be used to turn off globalSparkContext easily */
  private val useGlobalSparkContext: Boolean = System.getProperty("useGlobalSparkContext", "true").toBoolean

  /**
    * Should be called from before()
    */
  def sparkContext: SparkContext = {
    if (useGlobalSparkContext) {
      // reuse the global SparkContext
      sc
    }
    else {
      // create a new SparkContext each time
      lock.acquire()
      createLocalSparkContext()
    }
  }

  /**
    * Should be called from after()
    */
  def cleanUp(): Unit = {
    if (!useGlobalSparkContext) {
      cleanupSpark()
      lock.release()
    }
  }

  private def createLocalSparkContext(
                                       serializer: String = "org.apache.spark.serializer.KryoSerializer",
                                       registrator: String = "org.trustedanalytics.atk.graphbuilder.GraphBuilderKryoRegistrator"): SparkContext = {
    // LogUtils.silenceSpark()
    System.setProperty("spark.driver.allowMultipleContexts", "true")
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName(this.getClass.getSimpleName + " " + new Date())
    //conf.set("spark.serializer", serializer)
    //conf.set("spark.kryo.registrator", registrator)
    conf.set("spark.sql.shuffle.partitions", "2")

    new SparkContext(conf)
  }

  /**
    * Shutdown spark and release the lock
    */
  private def cleanupSpark(): Unit = {
    try {
      if (sc != null) {
        sc.stop()
      }
    }
    finally {
      // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
      System.clearProperty("spark.driver.port")
    }
  }

}
