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
  * THIS CODE WAS COPIED DIRECTLY FROM THE OPEN SOURCE PROJECT TAP (Trusted Analytics Platform)
  * which has an Apache V2.0
  */

package org.apache.spot.testutils

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.scalatest.{BeforeAndAfterAll, WordSpec}

trait TestingSparkContextWordSpec extends WordSpec with BeforeAndAfterAll {

  var sparkContext: SparkContext = null
  var sqlContext : SQLContext = null

  override def beforeAll() = {
    sparkContext = TestingSparkContext.sparkContext
    sqlContext = new SQLContext(sparkContext)
  }

  /**
   * Clean up after the test is done
   */
  override def afterAll() = {
    TestingSparkContext.cleanUp()
    sparkContext = null
  }

}
