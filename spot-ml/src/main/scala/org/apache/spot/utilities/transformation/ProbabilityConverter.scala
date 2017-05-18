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

package org.apache.spot.utilities.transformation

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

/**
  * ProbabilityConverter will transform document probability from Double to Float if scaling option is set to 32 bit,
  * if default or 64 bit is selected, it will just return the same Double probability.
  *
  * This abstract class permits the execution of a single path during the entire analysis. Instead of checking what
  * type should the data be converted, each implementation will know what type should be converted into and converted
  * back.
  *
  */

abstract class ProbabilityConverter extends Serializable {

  type ScalingType

  /**
    * Converts a probability into the scaling type; it can be Float or Double.
    * For the Double implementation it will just return the same value without any transformation.
    *
    * @param probability a probability to convert from Double to scaling type.
    * @return
    */
  def convertProbability(probability: Double): ScalingType

  /**
    * Converts back a set of probabilities from the scaling type into Double.
    *
    * @param scaledProbabilities
    * @return
    */
  def convertBackSetOfProbabilities(scaledProbabilities: Seq[ScalingType]): Array[Double]

  /**
    * Converts a column for a given DataFrame from Double to the scaling type. If the scaling type is Double, it will
    * just return the same DataFrame without any transformation.
    *
    * @param dataFrame  a DataFrame containing a column to be converted from Double to the scaling type
    * @param columnName the name of the column to be converted.
    * @return
    */
  def convertDataFrameColumn(dataFrame: DataFrame, columnName: String): DataFrame

}

/**
  * ProbabilityConverter implementation for Float.
  * Will convert probabilities from Double to Float and back to Double to make the workload half the size that it'd
  * be with Double. This conversion will permit to broadcast document probabilities if it falls in the
  * autoBroadcastJoinThreshold - defined by users - and accelerate the join process between document probabilities
  * and the entire data set.
  */
object ProbabilityConverterFloat extends ProbabilityConverter {

  type ScalingType = Float
  val convertUDF = udf((probabilities: Seq[Double]) => {
    for (probability <- probabilities) yield convertProbability(probability)
  })

  def convertProbability(probability: Double): Float = probability.toFloat

  def convertBackSetOfProbabilities(scaledProbabilities: Seq[Float]): Array[Double] =
    for (scaledProbability <- scaledProbabilities.toArray) yield scaledProbability.toDouble

  override def convertDataFrameColumn(dataFrame: DataFrame, columnName: String): DataFrame = {
    dataFrame.withColumn("temp_column", convertUDF(dataFrame(columnName)))
      .drop(columnName)
      .withColumnRenamed("temp_column", columnName)
  }
}

/**
  * ProbabilityConverter implementation for Double.
  * Users that don't want to reduce the workload, can continue working with Doubles. This implementation will receive
  * and send the same value, it won't do any transformation. The reason for this to exists is to avoid multiple code
  * paths depending on the user wanting to scale or not.
  */
object ProbabilityConverterDouble extends ProbabilityConverter {

  type ScalingType = Double

  def convertProbability(probability: Double): Double = probability

  // Since Double is the default data type, this code is going to return the same array without any transformation.
  def convertBackSetOfProbabilities(scaledProbability: Seq[Double]): Array[Double] = scaledProbability.toArray

  // Since Double is the default data type, this code won't actually do any calculation but instead just return the
  // same DataFrame.
  override def convertDataFrameColumn(dataFrame: DataFrame, columnName: String): DataFrame = dataFrame
}