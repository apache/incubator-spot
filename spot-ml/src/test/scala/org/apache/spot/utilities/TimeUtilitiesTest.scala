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

package org.apache.spot.utilities

import org.apache.spot.testutils.TestingSparkContextFlatSpec
import org.scalatest.Matchers


class TimeUtilitiesTest extends TestingSparkContextFlatSpec with Matchers {

  val time1 = "2:35:45"
  val time2 = "14:00:08"
  val time3 = "1:35:56"
  val time4 = "0:25:00"

  "getTimeAsDouble" should "return the time converted to seconds given the format HH:MM:SS..." in {

    val timeSec1 = TimeUtilities.getTimeAsDouble(time1)
    val timeSec2 = TimeUtilities.getTimeAsDouble(time2)
    val timeSec3 = TimeUtilities.getTimeAsDouble(time3)
    val timeSec4 = TimeUtilities.getTimeAsDouble(time4)

    timeSec1 shouldBe 9345.0
    timeSec2 shouldBe 50408.0
    timeSec3 shouldBe 5756.0
    timeSec4 shouldBe 1500.0
  }

  "getTimeAsHour" should "return only the hour part of time given the format HH:MM:SS..." in {


    val hour1 = TimeUtilities.getTimeAsHour(time1).toString
    val hour2 = TimeUtilities.getTimeAsHour(time2).toString
    val hour3 = TimeUtilities.getTimeAsHour(time3).toString
    val hour4 = TimeUtilities.getTimeAsHour(time4).toString

    hour1 shouldBe "2"
    hour2 shouldBe "14"
    hour3 shouldBe "1"
    hour4 shouldBe "0"
  }

}
