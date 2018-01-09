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

package org.apache.spot.lda

import org.apache.spark.mllib.clustering.{DistributedLDAModel, LDAModel, LocalLDAModel}
import org.apache.spark.sql.SparkSession

/**
  * Spot LDAModel.
  */
sealed trait SpotLDAModel {

  /**
    * Save the model to HDFS
    *
    * @param sparkSession
    * @param location
    */
  def save(sparkSession: SparkSession, location: String): Unit

  /**
    * Predict topicDistributions and get topicsMatrix along with results formatted for Apache Spot scoring
    *
    * @param helper
    * @return
    */
  def predict(helper: SpotLDAHelper): SpotLDAResult
}

/**
  * Spark LocalLDAModel wrapper.
  *
  * @param ldaModel Spark LDA Model
  */
class SpotLocalLDAModel(final val ldaModel: LDAModel) extends SpotLDAModel {

  /**
    * Save LocalLDAModel on HDFS location
    *
    * @param sparkSession the Spark session
    * @param location     the HDFS location
    */
  override def save(sparkSession: SparkSession, location: String): Unit = {
    val sparkContext = sparkSession.sparkContext

    ldaModel.save(sparkContext, location)
  }

  /**
    * Predict topicDistributions and get topicsMatrix along with results formatted for Apache Spot scoring.
    * SpotLocalLDAModel.predict will use corpus from spotLDAHelper which can be a new set of documents or the same
    * documents used for training.
    *
    * @param spotLDAHelper Spot LDA Helper object, can be the same used for training or a new instance with new
    *                      documents.
    * @return SpotLDAResult
    */
  override def predict(spotLDAHelper: SpotLDAHelper): SpotLDAResult = {

    val localLDAModel: LocalLDAModel = ldaModel.asInstanceOf[LocalLDAModel]

    val topicDistributions = localLDAModel.topicDistributions(spotLDAHelper.formattedCorpus)
    val topicMix = localLDAModel.topicsMatrix

    SpotLDAResult(spotLDAHelper, topicDistributions, topicMix)
  }
}

/** Spark DistributedLDAModel wrapper.
  * Ideally, this model should be used only for batch processing.
  *
  * @param ldaModel Spark LDA Model
  */
class SpotDistributedLDAModel(final val ldaModel: LDAModel) extends
  SpotLDAModel {

  /**
    * Save DistributedLDAModel on HDFS location
    *
    * @param sparkSession the Spark session
    * @param location     the HDFS location
    */
  override def save(sparkSession: SparkSession, location: String): Unit = {
    val sparkContext = sparkSession.sparkContext

    ldaModel.save(sparkContext, location)
  }

  /**
    * Predict topicDistributions and get topicsMatrix along with results formatted for Apache Spot scoring.
    * SpotDistributeLDAModel.predict will use same documents that were used for training, can't predict on new
    * documents. When passing spotLDAHelper we recommend to make sure it's the same object it was passed for training.
    *
    * @param spotLDAHelper Spot LDA Helper object used for training
    * @return SpotLDAResult
    */
  override def predict(spotLDAHelper: SpotLDAHelper): SpotLDAResult = {

    val distributedLDAModel: DistributedLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]

    val topicDistributions = distributedLDAModel.topicDistributions
    val topicsMatrix = distributedLDAModel.topicsMatrix

    SpotLDAResult(spotLDAHelper, topicDistributions, topicsMatrix)
  }
}

object SpotLDAModel {

  /**
    * Factory method, based on instance of ldaModel will generate an object based on DistributedLDAModel
    * implementation or LocalLDAModel.
    *
    * @param ldaModel Spark LDAModel
    * @return
    */
  def apply(ldaModel: LDAModel): SpotLDAModel = {

    ldaModel match {
      case model: DistributedLDAModel => new SpotDistributedLDAModel(model)
      case model: LocalLDAModel => new SpotLocalLDAModel(model)
    }
  }
}
