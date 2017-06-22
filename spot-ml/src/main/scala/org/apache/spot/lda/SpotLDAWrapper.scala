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
import org.apache.spark.mllib.clustering._
import org.apache.spark.mllib.linalg.{Matrix, Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.utilities.FloatPointPrecisionUtility

import scala.collection.immutable.Map

/**
  * Spark LDA implementation
  * Contains routines for LDA using Scala Spark implementation from mllib
  * 1. Creates list of unique documents, words and model based on those two
  * 2. Processes the model using Spark LDA
  * 3. Reads Spark LDA results: Topic distributions per document (docTopicDist) and word distributions per topic (wordTopicMat)
  * 4. Convert wordTopicMat (Matrix) and docTopicDist (RDD) to appropriate formats (maps) originally specified in LDA-C
  */

object SpotLDAWrapper {

  /**
    * Runs Spark LDA and returns a new model.
    *
    * @param sparkSession       the SparkSession
    * @param docWordCount       RDD with document list and the word count for each document (corpus)
    * @param topicCount         number of topics to find
    * @param logger             application logger
    * @param ldaSeed            LDA seed
    * @param ldaAlpha           document concentration
    * @param ldaBeta            topic concentration
    * @param ldaOptimizerOption LDA optimizer, em or online
    * @param maxIterations      maximum number of iterations for the optimizer
    * @param precisionUtility   FloatPointPrecisionUtility implementation based on user configuration (64 or 32 bit)
    * @return
    */
  def runLDA(sparkSession: SparkSession,
             docWordCount: RDD[SpotLDAInput],
             topicCount: Int,
             logger: Logger,
             ldaSeed: Option[Long],
             ldaAlpha: Double,
             ldaBeta: Double,
             ldaOptimizerOption: String,
             maxIterations: Int,
             precisionUtility: FloatPointPrecisionUtility): SpotLDAOutput = {

    import sparkSession.implicits._

    val docWordCountCache = docWordCount.cache()

    // Forcing an action to cache results.
    docWordCountCache.count()

    // Create word Map Word,Index for further usage
    val wordDictionary: Map[String, Int] = {
      val words = docWordCountCache
        .map({ case SpotLDAInput(doc, word, count) => word })
        .distinct
        .collect
      words.zipWithIndex.toMap
    }

    val documentDictionary: DataFrame = docWordCountCache
      .map({ case SpotLDAInput(doc, word, count) => doc })
      .distinct
      .zipWithIndex
      .toDF(DocumentName, DocumentNumber)
      .cache

    // Structure corpus so that the index is the docID, values are the vectors of word occurrences in that doc
    val ldaCorpus: RDD[(Long, Vector)] =
      formatSparkLDAInput(docWordCountCache,
        documentDictionary,
        wordDictionary,
        sparkSession)

    docWordCountCache.unpersist()

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

    // If caller does not provide seed to lda, ie. ldaSeed is empty, lda is seeded automatically set to hash value of class name

    if (ldaSeed.nonEmpty) {
      lda.setSeed(ldaSeed.get)
    }

    val (wordTopicMat, docTopicDist) = ldaOptimizer match {
      case _: EMLDAOptimizer => {
        val ldaModel = lda.run(ldaCorpus).asInstanceOf[DistributedLDAModel]

        // Get word topic mix, from Spark documentation:
        // Inferred topics, where each topic is represented by a distribution over terms.
        // This is a matrix of size vocabSize x k, where each column is a topic.
        // No guarantees are given about the ordering of the topics.
        val wordTopicMat: Matrix = ldaModel.topicsMatrix

        // Topic distribution: for each document, return distribution (vector) over topics for that docs where entry
        // i is the fraction of the document which belongs to topic i
        val docTopicDist: RDD[(Long, Vector)] = ldaModel.topicDistributions

        (wordTopicMat, docTopicDist)

      }

      case _: OnlineLDAOptimizer => {
        val ldaModel = lda.run(ldaCorpus).asInstanceOf[LocalLDAModel]

        // Get word topic mix, from Spark documentation:
        // Inferred topics, where each topic is represented by a distribution over terms.
        // This is a matrix of size vocabSize x k, where each column is a topic.
        // No guarantees are given about the ordering of the topics.
        val wordTopicMat: Matrix = ldaModel.topicsMatrix

        // Topic distribution: for each document, return distribution (vector) over topics for that docs where entry
        // i is the fraction of the document which belongs to topic i
        val docTopicDist: RDD[(Long, Vector)] = ldaModel.topicDistributions(ldaCorpus)

        (wordTopicMat, docTopicDist)

      }

    }

    // Create doc results from vector: convert docID back to string, convert vector of probabilities to array
    val docToTopicMixDF =
      formatSparkLDADocTopicOutput(docTopicDist, documentDictionary, sparkSession, precisionUtility)

    documentDictionary.unpersist()

    // Create word results from matrix: convert matrix to sequence, wordIDs back to strings, sequence of
    // probabilities to array
    val revWordMap: Map[Int, String] = wordDictionary.map(_.swap)

    val wordResults = formatSparkLDAWordOutput(wordTopicMat, revWordMap)

    // Create output object
    SpotLDAOutput(docToTopicMixDF, wordResults)
  }

  /**
    * Formats input data for LDA algorithm
    *
    * @param docWordCount       RDD with document list and the word count for each document (corpus)
    * @param documentDictionary DataFrame with a distinct list of documents and its id
    * @param wordDictionary     immutable Map with distinct list of word and its id
    * @param sparkSession       the SparkSession
    * @return
    */
  def formatSparkLDAInput(docWordCount: RDD[SpotLDAInput],
                          documentDictionary: DataFrame,
                          wordDictionary: Map[String, Int],
                          sparkSession: SparkSession): RDD[(Long, Vector)] = {

    import sparkSession.implicits._

    val getWordId = {
      udf((word: String) => (wordDictionary(word)))
    }

    val docWordCountDF = docWordCount
      .map({ case SpotLDAInput(doc, word, count) => (doc, word, count) })
      .toDF(DocumentName, WordName, WordNameWordCount)

    // Convert SpotSparkLDAInput into desired format for Spark LDA: (doc, word, count) -> word count per doc, where RDD
    // is indexed by DocID
    val wordCountsPerDocDF = docWordCountDF
      .join(documentDictionary, docWordCountDF(DocumentName) === documentDictionary(DocumentName))
      .drop(documentDictionary(DocumentName))
      .withColumn(WordNumber, getWordId(docWordCountDF(WordName)))
      .drop(WordName)

    val wordCountsPerDoc: RDD[(Long, Iterable[(Int, Double)])]
    = wordCountsPerDocDF
      .select(DocumentNumber, WordNumber, WordNameWordCount)
      .rdd
      .map({ case Row(documentId: Long, wordId: Int, wordCount: Int) => (documentId.toLong, (wordId, wordCount.toDouble)) })
      .groupByKey

    // Sum of distinct words in each doc (words will be repeated between different docs), used for sparse vec size
    val numUniqueWords = wordDictionary.size
    val ldaInput: RDD[(Long, Vector)] = wordCountsPerDoc
      .mapValues({ case vs => Vectors.sparse(numUniqueWords, vs.toSeq) })

    ldaInput
  }

  /**
    * Format LDA output topicMatrix for spot-ml scoring
    *
    * @param wordTopMat LDA model topicMatrix
    * @param wordMap    immutable Map with distinct list of word and its id
    * @return
    */
  def formatSparkLDAWordOutput(wordTopMat: Matrix, wordMap: Map[Int, String]): scala.Predef.Map[String, Array[Double]] = {

    // incoming word top matrix is in column-major order and the columns are unnormalized
    val m = wordTopMat.numRows
    val n = wordTopMat.numCols
    val columnSums: Array[Double] = Range(0, n).map(j => (Range(0, m).map(i => wordTopMat(i, j)).sum)).toArray

    val wordProbs: Seq[Array[Double]] = wordTopMat.transpose.toArray.grouped(n).toSeq
      .map(unnormProbs => unnormProbs.zipWithIndex.map({ case (u, j) => u / columnSums(j) }))

    wordProbs.zipWithIndex.map({ case (topicProbs, wordInd) => (wordMap(wordInd), topicProbs) }).toMap
  }

  /**
    * Format LDA output topicDistribution for spot-ml scoring
    *
    * @param docTopDist         LDA model topicDistribution
    * @param documentDictionary DataFrame with a distinct list of documents and its id
    * @param sparkSession       the SparkSession
    * @param precisionUtility   FloatPointPrecisionUtility implementation based on user configuration (64 or 32 bit)
    * @return
    */
  def formatSparkLDADocTopicOutput(docTopDist: RDD[(Long, Vector)], documentDictionary: DataFrame, sparkSession: SparkSession,
                                   precisionUtility: FloatPointPrecisionUtility):
  DataFrame = {
    import sparkSession.implicits._

    val topicDistributionToArray = udf((topicDistribution: Vector) => topicDistribution.toArray)
    val documentToTopicDistributionDF = docTopDist.toDF(DocumentNumber, TopicProbabilityMix)

    val documentToTopicDistributionArray = documentToTopicDistributionDF
      .join(documentDictionary, documentToTopicDistributionDF(DocumentNumber) === documentDictionary(DocumentNumber))
      .drop(documentDictionary(DocumentNumber))
      .drop(documentToTopicDistributionDF(DocumentNumber))
      .select(DocumentName, TopicProbabilityMix)
      .withColumn(TopicProbabilityMixArray, topicDistributionToArray(documentToTopicDistributionDF(TopicProbabilityMix)))
      .selectExpr(s"$DocumentName  AS $DocumentName", s"$TopicProbabilityMixArray AS $TopicProbabilityMix")

    precisionUtility.castColumn(documentToTopicDistributionArray, TopicProbabilityMix)
  }

  case class SpotLDAInput(doc: String, word: String, count: Int) extends Serializable

  case class SpotLDAOutput(docToTopicMix: DataFrame, wordResults: Map[String, Array[Double]])

}