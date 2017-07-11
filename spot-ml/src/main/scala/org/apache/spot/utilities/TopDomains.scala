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

import scala.io.Source

/**
  * List of top domains used for DNS and Proxy analysis.
  */
object TopDomains {
  val alexaTop1MPath = "top-1m.csv"

  val TopDomains: Set[String] = if (new java.io.File(alexaTop1MPath).exists) {
    Source.fromFile(alexaTop1MPath).getLines.map(line => {
      val parts = line.split(",")
      parts(1).split('.')(0)
    }).toSet
  } else {
    // Default domains for unit testing only.
    Set("spot-ml-unit-test-1", "spot-ml-unit-test-2", "spot-ml-unit-test-3", "spot-ml-unit-test-4")
  }

}
