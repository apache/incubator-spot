/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
  * THIS CODE WAS ORIGINALLY COPIED DIRECTLY FROM THE OPEN SOURCE PROJECT TAP (Trusted Analytics Platform)
  * which has an Apache V2.0. IT WAS LATER UPDATED TO SUPPORT SPARK 2.1 SparkSession
  */

package org.apache.spot.testutils

import java.util.concurrent.locks.ReentrantLock

import org.apache.spark.sql.SparkSession

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
  private val lock = new ReentrantLock()

  /** global SparkSession that can be re-used between tests */
  private lazy val sparkSession: SparkSession = createLocalSparkSession()

  /** System property can be used to turn off globalSparkContext easily */
  private val useGlobalSparkContext: Boolean = System.getProperty("useGlobalSparkContext", "true").toBoolean

  /**
    * Should be called from before()
    */
  def getSparkSession: SparkSession = {
    if (useGlobalSparkContext) {
      // reuse the global SparkContext
      sparkSession
    }
    else {
      // create a new SparkSession each time
      lock.lock()
      createLocalSparkSession()
    }
  }

  /**
    * Should be called from after()
    */
  def cleanUp(): Unit = {
    if (!useGlobalSparkContext) {
      cleanupSpark()
    }
  }

  private def createLocalSparkSession(serializer: String = "org.apache.spark.serializer.KryoSerializer",
                                      registrator: String = "org.trustedanalytics.atk.graphbuilder" +
                                        ".GraphBuilderKryoRegistrator"): SparkSession = {
    SparkSession.builder
      .appName("spot-ml-testing")
      .master("local")
      .config("spark.sql.shuffle.partitions", "2")
      .getOrCreate()
  }

  /**
    * Shutdown spark and release the lock
    */
  private def cleanupSpark(): Unit = {
    try {
      if (sparkSession != null) {
        sparkSession.stop()
      }
    }
    finally {
      // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
      System.clearProperty("spark.driver.port")
      lock.unlock()
    }
  }

}
