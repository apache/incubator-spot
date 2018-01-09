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

import org.apache.spark.mllib.linalg.{Matrix, Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spot.lda.SpotLDAWrapperSchema._
import org.apache.spot.utilities.{FloatPointPrecisionUtility, FloatPointPrecisionUtility64}

import scala.collection.immutable.Map

/**
  * Apache Spot routines to format Spark LDA input and output for scoring.
  */
class SpotLDAHelper(private final val sparkSession: SparkSession,
                    final val docWordCount: RDD[SpotLDAInput],
                    private final val documentDictionary: DataFrame,
                    private final val wordDictionary: Map[String, Int],
                    private final val precisionUtility: FloatPointPrecisionUtility = FloatPointPrecisionUtility64)
  extends Serializable {

  /**
    * Format document word count as RDD[(Long, Vector)] - input data for LDA algorithm
    *
    * @return RDD[(Long, Vector)]
    */
  val formattedCorpus: RDD[(Long, Vector)] = {
    import sparkSession.implicits._

    val getWordId = {
      udf((word: String) => wordDictionary(word))
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
      .mapValues(vs => Vectors.sparse(numUniqueWords, vs.toSeq))

    ldaInput
  }

  /**
    * Format LDA output topicDistribution for spot-ml scoring
    *
    * @param documentDistributions LDA model topicDistributions
    * @return DataFrame
    */
  def formatDocumentDistribution(documentDistributions: RDD[(Long, Vector)]): DataFrame = {
    import sparkSession.implicits._

    val topicDistributionToArray = udf((topicDistribution: Vector) => topicDistribution.toArray)
    val documentToTopicDistributionDF = documentDistributions.toDF(DocumentNumber, TopicProbabilityMix)

    val documentToTopicDistributionArray = documentToTopicDistributionDF
      .join(documentDictionary, documentToTopicDistributionDF(DocumentNumber) === documentDictionary(DocumentNumber))
      .drop(documentDictionary(DocumentNumber))
      .drop(documentToTopicDistributionDF(DocumentNumber))
      .select(DocumentName, TopicProbabilityMix)
      .withColumn(TopicProbabilityMixArray, topicDistributionToArray(documentToTopicDistributionDF(TopicProbabilityMix)))
      .selectExpr(s"$DocumentName  AS $DocumentName", s"$TopicProbabilityMixArray AS $TopicProbabilityMix")

    precisionUtility.castColumn(documentToTopicDistributionArray, TopicProbabilityMix)
  }

  /**
    * Format LDA output topicMatrix for spot-ml scoring
    *
    * @param topicsMatrix LDA model topicMatrix
    * @return Map[String, Array[Double]]
    **/
  def formatTopicDistributions(topicsMatrix: Matrix): Map[String, Array[Double]] = {
    // Incoming word top matrix is in column-major order and the columns are unnormalized
    val m = topicsMatrix.numRows
    val n = topicsMatrix.numCols
    val reverseWordDictionary = wordDictionary.map(_.swap)

    val columnSums: Array[Double] = Range(0, n).map(j => Range(0, m).map(i => topicsMatrix(i, j)).sum).toArray

    val wordProbabilities: Seq[Array[Double]] = topicsMatrix.transpose.toArray.grouped(n).toSeq
      .map(unNormalizedProbabilities => unNormalizedProbabilities.zipWithIndex.map({ case (u, j) => u / columnSums(j) }))

    wordProbabilities.zipWithIndex
      .map({ case (topicProbabilities, wordInd) => (reverseWordDictionary(wordInd), topicProbabilities) }).toMap
  }

}

object SpotLDAHelper {

  /**
    * Factory method for SpotLDAHelper new instance.
    *
    * @param docWordCount Document word count.
    * @param precisionUtility
    * @param sparkSession
    * @return
    */
  def apply(docWordCount: RDD[SpotLDAInput],
            precisionUtility: FloatPointPrecisionUtility,
            sparkSession: SparkSession): SpotLDAHelper = {

    import sparkSession.implicits._

    val docWordCountCache = docWordCount.cache()

    // Forcing an action to cache results.
    docWordCountCache.count()

    // Create word Map Word,Index for further usage
    val wordDictionary: Map[String, Int] = {
      val words = docWordCountCache
        .map({ case SpotLDAInput(_, word, _) => word })
        .distinct
        .collect
      words.zipWithIndex.toMap
    }

    val documentDictionary: DataFrame = docWordCountCache
      .map({ case SpotLDAInput(doc, _, _) => doc })
      .distinct
      .zipWithIndex
      .toDF(DocumentName, DocumentNumber)
      .cache

    new SpotLDAHelper(sparkSession, docWordCount, documentDictionary, wordDictionary, precisionUtility)
  }

}

/**
  * Spot LDA input case class
  *
  * @param doc   Document name.
  * @param word  Word.
  * @param count Times the word appears for the document.
  */
case class SpotLDAInput(doc: String, word: String, count: Int) extends Serializable
