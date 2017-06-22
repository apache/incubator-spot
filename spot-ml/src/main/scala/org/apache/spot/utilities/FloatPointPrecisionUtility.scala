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

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

/**
  * PrecisionUtility will transform a number from Double to Float if precision option is set to 32 bit,
  * if default or 64 bit is selected, it will just return the same number type Double.
  *
  */
sealed trait FloatPointPrecisionUtility extends Serializable {

  type TargetType

  /**
    * Converts a number into the precision type; it can be Float (32) or Double (64).
    *
    * @param double a number to convert from Double to Target type.
    * @return
    */
  def toTargetType(double: Double): TargetType

  /**
    * Converts back an Iterable of numbers from TargetType to Double. <% determines that it can process any object that
    * extends from Iterable i.e. List, Vector, Seq, Queue, etc.
    *
    * @param targetTypeIterable an iterable that needs to be converted each element from TargetType to Double
    * @return
    */
  def toDoubles[A <% Traversable[TargetType], B <% Traversable[Double]](targetTypeIterable: A): B

  /**
    * Converts a DataFrame column from Seq[Double] to a Seq[TargetType].
    *
    * @param dataFrame  a DataFrame containing a column to be converted from Double to the TargetType
    * @param columnName the name of the column to convert, the column should be Seq[Double]
    * @return
    */
  def castColumn(dataFrame: DataFrame, columnName: String): DataFrame

}

/**
  * PrecisionUtility implementation for Float.
  * Will convert numbers from Double to Float and back to Double.
  *
  */
object FloatPointPrecisionUtility32 extends FloatPointPrecisionUtility {

  type TargetType = Float

  def toTargetType(double: Double): Float = double.toFloat

  def toDoubles[A <% Traversable[Float], B <% Traversable[Double]](targetTypeIterable: A): B =
    targetTypeIterable.map(_.toDouble).asInstanceOf[B]


  private val convertUDF = udf((doubles: Seq[Double]) => {
    doubles.map(double => toTargetType(double))
  })

  def castColumn(dataFrame: DataFrame, columnName: String): DataFrame = {
    val TempColumn = "temp_column"

    dataFrame.withColumn(TempColumn, convertUDF(dataFrame(columnName)))
      .drop(columnName)
      .withColumnRenamed(TempColumn, columnName)
  }

}

/**
  * PrecisionUtility implementation for Double.
  * This implementation will receive and send the same value, it won't do any transformation.
  *
  */
object FloatPointPrecisionUtility64 extends FloatPointPrecisionUtility {

  type TargetType = Double

  // For this implementation it will just return the same value without any transformation.
  def toTargetType(double: Double): Double = double

  // Since Double is the default data type, this code is going to return the same array without any transformation.
  def toDoubles[A <% Traversable[Double], B <% Traversable[Double]](targetTypeIterable: A): B =
    targetTypeIterable.asInstanceOf[B]

  // Since Double is the default data type, this code won't actually do any calculation but instead just return the
  // same DataFrame.
  def castColumn(dataFrame: DataFrame, columnName: String): DataFrame = dataFrame
}