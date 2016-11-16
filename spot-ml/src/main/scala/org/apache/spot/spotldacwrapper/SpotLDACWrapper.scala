package org.apache.spot.spotldacwrapper

import java.io.File

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SQLContext}

import org.apache.spot.spotldacwrapper.SpotLDACSchema._

import scala.collection.immutable.Map
import scala.io.Source._
import scala.sys.process._

/**
  * Contains routines for LDA including pre and post operations
  * 1. Creates list of unique documents, words and model based on those two
  * 2. Processes the model calling MPI
  * 3. Reads MPI results: Topic distributions per document and words per topic
  * 4. Calculates and returns probability of word given topic: p(w|z)
  */

object SpotLDACWrapper {

  /**
    * runLDA receives a RDD of SpotLDACInput and converts to Spot-LDA-C input model, then calls system process to run
    * Spot-LDA-C. After that, reads local files final.gamma and final.beta to normalize ML results and
    * return as SpotLDACOutput object.
    *
    * @param docWordCount RDD containing a list of documents or ips, each word they are related to and the count of
    *                     each word.
    * @param modelFile Final destination (local file system) for model so Spot-LDA-C can pick it up.
    * @param hdfsModelFile HDFS temporary location for model.
    * @param topicDocumentFile Location of Spot-LDA-C results file final.gamma (local file system).
    * @param topicWordFile Location of Spot-LDA-C results file final.beta (local file system).
    * @param mpiPreparationCmd MPI preparation command if required.
    * @param mpiCmd MPI command.
    * @param mpiProcessCount MPI process count.
    * @param topicCount Number of topics for Spot-LDA-C
    * @param localPath Local path where installation is located.
    * @param ldaPath Spot-LDA-C local path.
    * @param localUser Spot local path.
    * @param dataSource Flow, DNS or Proxy.
    * @param nodes  List of Nodes to distribute model.dat (LDA input).
    * @param prgSeed Flag for LDA, seeded no seeded.
    * @param sparkContext Application Spark Context.
    * @param sqlContext Application SQL Context.
    * @param logger Application Logger.
    * @return
    */
  def runLDA(docWordCount: RDD[SpotLDACInput],
             modelFile: String,
             hdfsModelFile: String,
             topicDocumentFile: String,
             topicWordFile: String,
             mpiPreparationCmd: String,
             mpiCmd: String,
             mpiProcessCount: String,
             topicCount: Int,
             localPath: String,
             ldaPath: String,
             localUser: String,
             dataSource: String,
             nodes: String,
             prgSeed: Option[Long],
             sparkContext: SparkContext,
             sqlContext: SQLContext,
             logger: Logger): SpotLDACOutput =  {

    import sqlContext.implicits._
    val docWordCountCache = docWordCount.cache()

    // Create word Map Word,Index for further usage
    val wordDictionary: Map[String, Int] = {
      val words = docWordCountCache.map({case SpotLDACInput(doc, word, count) => word})
        .distinct
        .collect
      words.zipWithIndex.toMap
    }

    // Create model for LDA
    val modelDF = createModel(docWordCountCache, wordDictionary, sparkContext, sqlContext, logger)

    docWordCountCache.unpersist()

    val documentDictionary = modelDF.select(col(DocumentName))
      .rdd
      .map(
        x=>  x.toString().replaceAll("\\[","").replaceAll("\\]","")
      )
      .zipWithIndex.toDF(DocumentName, DocumentId)

    // Save model to HDFS and then getmerge to file system

    modelDF.select(col(DocumentNameWordNameWordCount)).rdd.map(_.mkString).saveAsTextFile(hdfsModelFile)
    sys.process.Process(Seq("hadoop", "fs", "-getmerge", hdfsModelFile + "/part-*", modelFile)).!
    sys.process.Process(Seq("hadoop", "fs", "-rm", "-r", "-skipTrash", hdfsModelFile)).!

    // Copy model.dat to each node in machinefile

    val nodeList = nodes.replace("'","").split(",")
    for (node <- nodeList){
      sys.process.Process(Seq("ssh", node, "mkdir -p " + localUser + "/ml/" + dataSource)).!
      sys.process.Process(Seq("scp", "-r", localPath, node + ":" + localUser + "/ml/" + dataSource )).!
    }

    // Execute Pre MPI command

    if(mpiPreparationCmd != "" && mpiPreparationCmd != null)
      stringToProcess(mpiPreparationCmd).!!

    // Execute MPI-Spot-LDA-C

    val prgSeedString = if (prgSeed.nonEmpty) prgSeed.get.toString() else ""

    sys.process.Process(Seq(mpiCmd, "-n", mpiProcessCount, "-f", "machinefile", "./lda", "est", "2.5",
      topicCount.toString(), "settings.txt",  modelFile, "random", localPath, prgSeedString),
      new java.io.File(ldaPath)) #> (System.out) !!

    // Read topic info per document

    val topicDocumentFileExists = if (topicDocumentFile != "") new File(topicDocumentFile).exists else false
    val topicWordFileExists = if (topicWordFile != "") new File(topicWordFile).exists() else false

    val documentTopicMixRawLines = {
      if (topicDocumentFileExists) {
        fromFile(topicDocumentFile).getLines().toArray
      }
      else Array[String]()
    }

    // Create document results

    val docToTopicMix = getDocumentResults(documentTopicMixRawLines, documentDictionary, topicCount, sparkContext, sqlContext)

    // Read words per topic

    val topicWordData = {
      if (topicWordFileExists) {
        fromFile(topicWordFile).getLines().toArray
      }
      else Array[String]()
    }

    // Create word results
    val wordResults = getWordToProbPerTopicMap(topicWordData, wordDictionary)

    SpotLDACOutput(docToTopicMix, wordResults)
  }

  /**
    * getWordProbabilitiesFromTopicLine
    *
    * @param topicLine A line of text encoding the probabilities of the word given a topic.
    * @return probability of each word conditioned on this topic
    */
  def getWordProbabilitesFromTopicLine(topicLine: String) : Array[Double] = {

    val logWordProbs: Array[Double] = topicLine.trim().split(" ").map(_.toDouble)

    val wordProbs: Array[Double] = logWordProbs.map(math.exp)

    // Normalize to account for any weirdness from the log/exp transformations
    val sumRawWord = wordProbs.sum
    wordProbs.map(_ / sumRawWord)
  }


  /**
    * getDocumentToTopicProbabilityArray returns an array with probability distribution given the number of topics
    * @param documentTopicProbabilityMix a line with probability distribution. Its index matches a document in document
    *                                    dictionary.
    * @param topicCount number of topics.
    * @return
    */
  def getDocumentToTopicProbabilityArray(documentTopicProbabilityMix: String, topicCount: Int) : Array[Double]  ={

    val topics = documentTopicProbabilityMix.split(" ").map(_.toDouble)
    val topicsSum = topics.sum

    val topicsProb = {
      if (topicsSum > 0) {
        topics.map(_ / topicsSum)
      }
      else {
        Array.fill(topicCount)(0d)
      }
    }
    topicsProb
  }

  /**
    * createModel creates a new data frame with document names and document-word-count for further use in Spot-LDA-C
    * @param documentWordData RDD with document-word data and the count of each word.
    * @param wordToIndex  word dictionary with numeric index
    * @param sparkContext Application Spark Context
    * @param sqlContext Application Sql Context
    * @param logger Application Logger
    * @return
    */
  def createModel(documentWordData: RDD[SpotLDACInput],
                  wordToIndex: Map[String, Int],
                  sparkContext: SparkContext,
                  sqlContext: SQLContext,
                  logger: Logger): DataFrame = {
    import sqlContext.implicits._

    val documentCount = documentWordData
      .map({case SpotLDACInput(doc, word, count) => doc})
      .map(document => (document, 1))
      .reduceByKey(_ + _)

    val wordIndexdocWordCount = documentWordData
      .map({case SpotLDACInput(doc, word, count) => (doc, wordToIndex(word) + ":" + count)})
      .groupByKey()
      .map(x => (x._1, x._2.mkString(" ")))

    val wordCountDF = wordIndexdocWordCount.toDF(DocumentName,WordNameWordCount)
    val distinctDocDF = documentCount.toDF(DocumentName,DocumentCount)

    val docWordCount = distinctDocDF.join(wordCountDF, wordCountDF(DocumentName).equalTo(distinctDocDF(DocumentName)))
                              .drop(wordCountDF(DocumentName))

    def concatDocWordCount = {udf( (a: String, b: String) => a.concat(" ").concat(b))}

    val modelDF = docWordCount.withColumn(DocumentNameWordNameWordCount,
              concatDocWordCount(
                  docWordCount(DocumentCount),
                  docWordCount(WordNameWordCount)))
                  .drop(col(DocumentCount)).drop(col(WordNameWordCount))
    modelDF
  }

  /**
    * getDocumentResults normalizes Spot-LDA-C results for further steps.
    * @param ldaResults Spot-LDA-C results.
    * @param docIndexToDocument data frame with information about document and its index. The position of each line in
    *                           ldaResults corresponds to the index of each element in docIndexToDocument.
    * @param topicCount number of topics.
    * @param sparkContext Application Spark Context.
    * @param sqlContext Application Sql Context.
    * @return
    */
  def getDocumentResults(ldaResults: Array[String],
                         docIndexToDocument: DataFrame,
                         topicCount: Int, sparkContext: SparkContext, sqlContext: SQLContext) : DataFrame = {

      import sqlContext.implicits._

      val topicDocumentData = sparkContext.parallelize(ldaResults.zipWithIndex)
        .map(
          {
            case (documentTopicProbabilityMix, documentTopicProbabilityMixId) =>
              (getDocumentToTopicProbabilityArray(documentTopicProbabilityMix, topicCount), documentTopicProbabilityMixId)
          }
        ).toDF(TopicProbabilityMix, DocumentId)

      val ipToTopicMix = topicDocumentData.join(docIndexToDocument,
        topicDocumentData(DocumentId).equalTo(docIndexToDocument(DocumentId)))
        .drop(topicDocumentData(DocumentId))
        .drop(docIndexToDocument(DocumentId)).select(col(DocumentName), col(TopicProbabilityMix))

      ipToTopicMix
    }

  /**
    * getWordToProbPerTopicMap normalizes Spot-LDA-C results for further steps.
    * @param topicWordData
    * @param wordToIndex
    * @return
    */
  def getWordToProbPerTopicMap(topicWordData: Array[String],
                               wordToIndex: Map[String, Int]): Map[String, Array[Double]] = {


    val probabilityOfWordGivenTopic = topicWordData.map(getWordProbabilitesFromTopicLine).transpose

    val indexToWord = {
      val addedIndex = wordToIndex.size
      val tempWordDictionary = wordToIndex + (_0_0_0_0_0 -> addedIndex)
      tempWordDictionary.map({
        case (k, v) => (v, k)
      })
    }

    probabilityOfWordGivenTopic.zipWithIndex.map({ case (probOfWordGivenTopic, index) => indexToWord(index) ->
      probOfWordGivenTopic}).toMap

  }
}
