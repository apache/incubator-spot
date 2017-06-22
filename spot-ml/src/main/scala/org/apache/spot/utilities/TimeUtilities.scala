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

object TimeUtilities {


  /**
    * It converts HH:MM:SS string to seconds
    *
    * @param timeStr This is time in the form of a string
    * @return It returns time converted to seconds
    */

  def getTimeAsDouble(timeStr: String) : Double = {
    val s = timeStr.split(":")
    val hours = s(0).toInt
    val minutes = s(1).toInt
    val seconds = s(2).toInt

    (3600*hours + 60*minutes + seconds).toDouble
  }

  /**
    * It takes only the hour element of time
    *
    * @param timeStr This is time in the form of a string
    * @return It returns only the hour of time
    */
  def getTimeAsHour(timeStr: String): Int = {
    val s = timeStr.split(":")
    val hours = s(0).toInt
    hours
  }

}
