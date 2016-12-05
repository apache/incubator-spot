package org.apache.spot.lda

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.{DistributedLDAModel, EMLDAOptimizer, LDA}
import org.apache.spark.mllib.linalg.{Matrix, Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SQLContext}

import SpotLDAWrapperSchema._

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

  case class SpotLDAInput(doc: String, word: String, count: Int) extends Serializable

  case class SpotLDAOutput(docToTopicMix: DataFrame, wordResults: Map[String, Array[Double]])


  def runLDA(sparkContext: SparkContext,
             sqlContext: SQLContext,
             docWordCount: RDD[SpotLDAInput],
             topicCount: Int,
             logger: Logger,
             ldaSeed: Option[Long],
             ldaAlpha: Double,
             ldaBeta: Double,
             maxIterations: Int): SpotLDAOutput = {

    import sqlContext.implicits._

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

    // Forcing an action to cache results
    documentDictionary.count()

    //Structure corpus so that the index is the docID, values are the vectors of word occurrences in that doc
    val ldaCorpus: RDD[(Long, Vector)] =
      formatSparkLDAInput(docWordCountCache,
        documentDictionary,
        wordDictionary,
        sqlContext)

    docWordCountCache.unpersist()

    //Instantiate optimizer based on input
    val optimizer = new EMLDAOptimizer

    logger.info(s"Running Spark LDA with params alpha = $ldaAlpha beta = $ldaBeta " +
      s"Max iterations = $maxIterations Optimizer = em")
    //Set LDA params from input args

    val lda =
      new LDA()
        .setK(topicCount)
        .setMaxIterations(maxIterations)
        .setAlpha(ldaAlpha)
        .setBeta(ldaBeta)
        .setOptimizer(optimizer)

    // If caller does not provide seed to lda, ie. ldaSeed is empty, lda is seeded automatically set to hash value of class name

    if (ldaSeed.nonEmpty) {
      lda.setSeed(ldaSeed.get)
    }


    //Create LDA model
    val ldaModel = lda.run(ldaCorpus)

    //Convert to DistributedLDAModel to expose info about topic distribution
    val distLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]

    //Get word topic mix: columns = topic (in no guaranteed order), rows = words (# rows = vocab size)
    val wordTopicMat: Matrix = distLDAModel.topicsMatrix

    //Topic distribution: for each document, return distribution (vector) over topics for that docs
    val docTopicDist: RDD[(Long, Vector)] = distLDAModel.topicDistributions

    //Create doc results from vector: convert docID back to string, convert vector of probabilities to array
    val docToTopicMixDF =
      formatSparkLDADocTopicOutput(docTopicDist, documentDictionary, sqlContext)

    documentDictionary.unpersist()

    //Create word results from matrix: convert matrix to sequence, wordIDs back to strings, sequence of probabilities to array
    val revWordMap: Map[Int, String] = wordDictionary.map(_.swap)

    val wordResults = formatSparkLDAWordOutput(wordTopicMat, revWordMap)

    //Create output object
    SpotLDAOutput(docToTopicMixDF, wordResults)
  }

  def formatSparkLDAInput(docWordCount: RDD[SpotLDAInput],
                          documentDictionary: DataFrame,
                          wordDictionary: Map[String, Int],
                          sqlContext: SQLContext): RDD[(Long, Vector)] = {

    import sqlContext.implicits._

    val getWordId = {
      udf((word: String) => (wordDictionary(word)))
    }

    val docWordCountDF = docWordCount
      .map({ case SpotLDAInput(doc, word, count) => (doc, word, count) })
      .toDF(DocumentName, WordName, WordNameWordCount)

    //Convert SpotSparkLDAInput into desired format for Spark LDA: (doc, word, count) -> word count per doc, where RDD
    //is indexed by DocID
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

    //Sum of distinct words in each doc (words will be repeated between different docs), used for sparse vec size
    val numUniqueWords = wordDictionary.size
    val ldaInput: RDD[(Long, Vector)] = wordCountsPerDoc
      .mapValues({ case vs => Vectors.sparse(numUniqueWords, vs.toSeq) })

    ldaInput
  }

  def formatSparkLDAWordOutput(wordTopMat: Matrix, wordMap: Map[Int, String]): scala.Predef.Map[String, Array[Double]] = {

    // incoming word top matrix is in column-major order and the columns are unnormalized
    val m = wordTopMat.numRows
    val n = wordTopMat.numCols
    val columnSums: Array[Double] = Range(0, n).map(j => (Range(0, m).map(i => wordTopMat(i, j)).sum)).toArray

    val wordProbs: Seq[Array[Double]] = wordTopMat.transpose.toArray.grouped(n).toSeq
      .map(unnormProbs => unnormProbs.zipWithIndex.map({ case (u, j) => u / columnSums(j) }))

    wordProbs.zipWithIndex.map({ case (topicProbs, wordInd) => (wordMap(wordInd), topicProbs) }).toMap
  }

  def formatSparkLDADocTopicOutput(docTopDist: RDD[(Long, Vector)], documentDictionary: DataFrame, sqlContext: SQLContext):
  DataFrame = {
    import sqlContext.implicits._

    val topicDistributionToArray = udf((topicDistribution: Vector) => topicDistribution.toArray)
    val documentToTopicDistributionDF = docTopDist.toDF(DocumentNumber, TopicProbabilityMix)

    documentToTopicDistributionDF
      .join(documentDictionary, documentToTopicDistributionDF(DocumentNumber) === documentDictionary(DocumentNumber))
      .drop(documentDictionary(DocumentNumber))
      .drop(documentToTopicDistributionDF(DocumentNumber))
      .select(DocumentName, TopicProbabilityMix)
      .withColumn(TopicProbabilityMixArray, topicDistributionToArray(documentToTopicDistributionDF(TopicProbabilityMix)))
      .selectExpr(s"$DocumentName  AS $DocumentName", s"$TopicProbabilityMixArray AS $TopicProbabilityMix")
  }

}