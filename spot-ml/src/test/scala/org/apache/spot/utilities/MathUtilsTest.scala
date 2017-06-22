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


class MathUtilsTest extends TestingSparkContextFlatSpec with Matchers {

  "logBaseXInt" should "return the power in which the base is raised, in order to yield x rounded down to the nearest integer" in {

    val power1 = MathUtils.logBaseXInt(4.0, 2)
    val power2 = MathUtils.logBaseXInt(8.0, 2)
    val power3 = MathUtils.logBaseXInt(1.9, 2)
    val power4 = MathUtils.logBaseXInt(9.5, 3)

    power1 shouldBe 2
    power2 shouldBe 3
    power3 shouldBe 0
    power4 shouldBe 2


  }

  "bin" should "return the correct bin number" in {

    val cuts = Array(0d, 1d, 2d, 3d, Double.PositiveInfinity)

    val result1 = MathUtils.bin(-1d, cuts)
    val result2 = MathUtils.bin(0d, cuts)
    val result3 = MathUtils.bin(1.5d, cuts)
    val result4 = MathUtils.bin(50d, cuts)

    result1 shouldBe 0
    result2 shouldBe 0
    result3 shouldBe 2
    result4 shouldBe 4


  }
}
