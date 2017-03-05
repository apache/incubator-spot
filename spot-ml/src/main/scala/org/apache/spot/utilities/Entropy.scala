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

import scala.math._

/**
  * Created by nlsegerl on 8/4/16.
  */
object Entropy {

  /**
    * Calculates the "entropy" in string, interpreted as a distribution on its constituent characters.
    *
    * @param v Input string
    * @return
    */
  def stringEntropy(v: String): Double = {
    if (v.length() > 0) {
      v.groupBy(a => a).values.map(i => i.length.toDouble / v.length).map(p => -p * log10(p) / log10(2)).sum
    } else {
      0
    }
  }
}
