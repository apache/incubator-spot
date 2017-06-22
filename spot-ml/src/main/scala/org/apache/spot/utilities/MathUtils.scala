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

import scala.math.log10

object MathUtils {
  /**
    * Answers the question "To what power must "base" be raised, in order to yield "x"?  https://en.wikipedia.org/wiki/Logarithm
    *
    * @param x    This is a Double which is the result of the formula: base to the power of y = x
    * @param base This is the base of the number we are trying to find
    * @return y rounded down to an integer
    */
  def logBaseXInt(x: Double, base: Int): Int = if (x == 0) 0 else (log10(x) / log10(base)).toInt

  /**
    * Returns the ceiling of the logarithm base 2 of the incoming double.
    *
    * @param x A double.
    * @return Integer ceiling of logarithm base-2 of x.
    */
  def ceilLog2(x: Double) : Int = Math.ceil(log10(x) / log10(2d)).toInt

  /**
    *
    * @param value A number that we want to find an appropriate bin number for.
    * @param cuts The array that determines the right hand ends of the bins. If the last cut is not
    *             Double.PositiveInfinity any value greater than the last cut will return a bin number of -1.
    * @return An integer bin number into which the value falls.
    */
  def bin(value: Double, cuts: Array[Double]) : Int = {
    cuts.indexWhere(cut => value <= cut)
  }
}
