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

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.{LDAModel, _}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Spark LDA implementation
  * Contains routines for LDA using Scala Spark implementation from org.apache.spark.mllib.clustering
  * 1. Creates list of unique documents, words and model based on those two
  * 2. Processes the model using Spark LDA
  * 3. Reads Spark LDA results: Topic distributions per document (docTopicDist) and word distributions per topic (wordTopicMat)
  * 4. Convert wordTopicMat (Matrix) and docTopicDist (RDD) to appropriate formats (maps) originally specified in LDA-C
  */

object SpotLDAWrapper {

  /**
    * Runs Spark LDA and returns a new model.
    *
    * @param topicCount         number of topics to find
    * @param logger             application logger
    * @param ldaSeed            LDA seed
    * @param ldaAlpha           document concentration
    * @param ldaBeta            topic concentration
    * @param ldaOptimizerOption LDA optimizer, em or online
    * @param maxIterations      maximum number of iterations for the optimizer
    * @return
    */
  def run(topicCount: Int,
          logger: Logger,
          ldaSeed: Option[Long],
          ldaAlpha: Double,
          ldaBeta: Double,
          ldaOptimizerOption: String,
          maxIterations: Int,
          helper: SpotLDAHelper): SpotLDAModel = {


    // Structure corpus so that the index is the docID, values are the vectors of word occurrences in that doc
    val ldaCorpus: RDD[(Long, Vector)] = helper.formattedCorpus

    // Instantiate optimizer based on input
    val ldaOptimizer = ldaOptimizerOption match {
      case "em" => new EMLDAOptimizer
      case "online" => new OnlineLDAOptimizer().setOptimizeDocConcentration(true).setMiniBatchFraction({
        val corpusSize = ldaCorpus.count()
        if (corpusSize < 2) 0.75
        else (0.05 + 1) / corpusSize
      })
      case _ => throw new IllegalArgumentException(
        s"Invalid LDA optimizer $ldaOptimizerOption")
    }

    logger.info(s"Running Spark LDA with params alpha = $ldaAlpha beta = $ldaBeta " +
      s"Max iterations = $maxIterations Optimizer = $ldaOptimizerOption")

    // Set LDA params from input args
    val lda =
      new LDA()
        .setK(topicCount)
        .setMaxIterations(maxIterations)
        .setAlpha(ldaAlpha)
        .setBeta(ldaBeta)
        .setOptimizer(ldaOptimizer)

    // If caller does not provide seed to lda, ie. ldaSeed is empty,
    // lda is seeded automatically set to hash value of class name
    if (ldaSeed.nonEmpty) {
      lda.setSeed(ldaSeed.get)
    }

    val model: LDAModel = lda.run(ldaCorpus)

    SpotLDAModel(model)
  }

  /**
    * Load an existing model from HDFS location.
    *
    * @param sparkSession       the Spark session.
    * @param location           the HDFS location for the model.
    * @param ldaOptimizerOption LDA optimizer, em or online.
    * @return SpotLDAModel
    */
  def load(sparkSession: SparkSession, location: String, ldaOptimizerOption: String): SpotLDAModel = {
    val sparkContext: SparkContext = sparkSession.sparkContext

    val model = ldaOptimizerOption match {
      case "em" => DistributedLDAModel.load(sparkContext, location)
      case "online" => LocalLDAModel.load(sparkContext, location)
      case _ => throw new IllegalArgumentException(
        s"Invalid LDA optimizer $ldaOptimizerOption")
    }

    SpotLDAModel(model)
  }
}