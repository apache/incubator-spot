package org.apache.spot

import org.apache.spark.rdd.RDD
import java.io.PrintWriter
import java.io.File

import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._

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


  case class SpotLDACInput(doc: String, word: String, count: Int) extends Serializable

  case class SpotLDACOutput(docToTopicMix: Map[String, Array[Double]], wordResults: Map[String, Array[Double]])

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
             logger: Logger):   SpotLDACOutput =  {

    import sqlContext.implicits._
    val docWordCountCache = docWordCount.cache()

    // Create word Map Word,Index for further usage
    val wordDictionary: Map[String, Int] = {
      val words = docWordCountCache.map({case SpotLDACInput(doc, word, count) => word})
        .distinct
        .collect
      words.zipWithIndex.toMap
    }

    // Create model for MPI
    val modelDF = createModel(docWordCountCache, wordDictionary, sparkContext, sqlContext, logger)

    docWordCountCache.unpersist()

    val documentDictionary = modelDF.select(col("docID"))
      .rdd
      .map(
        x=>  x.toString().replaceAll("\\[","").replaceAll("\\]","")
      )
      .zipWithIndex.toDF("docName", "docIdx")



//    val model = modelDF.select(col("docWordCount"))
//      .rdd
//      .map(
//        x=>  x.toString().replaceAll("\\[","").replaceAll("\\]","")
//      ).collect
//
//    // Persist model.dat
//    val modelWriter = new PrintWriter(new File(modelFile))
//    model foreach (row => modelWriter.write("%s\n".format(row)))
//    modelWriter.close()

    modelDF.rdd.map(_.mkString).saveAsTextFile(hdfsModelFile)

    sys.process.Process(Seq("hadoop", "fs", "-getmerge", hdfsModelFile + "/part-*", modelFile)).!
    sys.process.Process(Seq("hadoop", "fs", "-rm", "-r", "-skipTrash", hdfsModelFile)).!


    // Copy model.dat to each machinefile node
    val nodeList = nodes.replace("'","").split(",")
    for (node <- nodeList){
      sys.process.Process(Seq("ssh", node, "mkdir -p " + localUser + "/ml/" + dataSource)).!
      sys.process.Process(Seq("scp", "-r", localPath, node + ":" + localUser + "/ml/" + dataSource )).!
    }

    // Execute Pre MPI command
    if(mpiPreparationCmd != "" && mpiPreparationCmd != null)
      stringToProcess(mpiPreparationCmd).!!

    // Execute MPI

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

    // Read words per topic
    val topicWordData = {
      if (topicWordFileExists) {
        fromFile(topicWordFile).getLines().toArray
      }
      else Array[String]()
    }

    // Create document results
    val docToTopicMix = getDocumentResults(documentTopicMixRawLines, documentDictionary, topicCount, sparkContext)

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

  def getTopicDocument(document: String, line: String, topicCount: Int) : (String, Array[Double])  = {
    val topics = line.split(" ").map(_.toDouble)
    val topicsSum = topics.sum

    if (topicsSum > 0) {
      val topicsProb = topics.map(_ / topicsSum)
      document -> topicsProb
    }
    else {
      val topicsProb = Array.fill(topicCount)(0d)
      document ->  topicsProb
    }
  }

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

    val wordCountDF = wordIndexdocWordCount.toDF("docID","wordIDCount")
    val distinctDocDF = documentCount.toDF("docID","docCount")

    distinctDocDF.show()

    val docWordCount = distinctDocDF.join(wordCountDF, wordCountDF("docID").equalTo(distinctDocDF("docID")))
                              .drop(wordCountDF("docID"))

    docWordCount.show()

    def concatDocWordCount = {udf( (a: String, b: String) => a.concat(" ").concat(b))}
    val modelDF = docWordCount.withColumn("docWordCount",
              concatDocWordCount(
                  docWordCount("docCount"),
                  docWordCount("wordIDCount")))
                  .drop(col("docCount")).drop(col("wordIDCount"))
    modelDF.show()
    modelDF.count()
    modelDF
  }

  def getDocumentResults(ldaResults: Array[String],
                         docIndexToDocument: DataFrame,// Map[Int, String],
                         topicCount: Int, sc: SparkContext) : Map[String, Array[Double]] = {

      val sqlContext = new org.apache.spark.sql.SQLContext(sc)
      import sqlContext.implicits._

      val topicDocumentData = sc.parallelize(ldaResults.zipWithIndex)
        .map(
          {
            case (topic, docIdx) => (getTopicDocumentArray(topic, topicCount), docIdx)
          }
        ).toDF("topicMix", "docIdx")

      val docTopicDF = topicDocumentData.join(docIndexToDocument,
        topicDocumentData("docIdx").equalTo(docIndexToDocument("docIdx")))
        .drop(topicDocumentData("docIdx"))
        .drop(docIndexToDocument("docIdx")).select(col("docName"), col("topicMix"))


    val results = docTopicDF.rdd.map({case(a: Row) => (a.toSeq.toArray)})
                    .map({case (a) => (a(0).asInstanceOf[String], a(1).asInstanceOf[Seq[Double]].toArray)})
                    .collectAsMap().toMap
    results
    }

  def getTopicDocumentArray(line: String, topicCount: Int) : Array[Double]  ={

    val topics = line.split(" ").map(_.toDouble)
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

  def getWordToProbPerTopicMap(topicWordData: Array[String],
                               wordToIndex: Map[String, Int]): Map[String, Array[Double]] = {


    val probabilityOfWordGivenTopic = topicWordData.map(getWordProbabilitesFromTopicLine).transpose

    val indexToWord = {
      val addedIndex = wordToIndex.size
      val tempWordDictionary = wordToIndex + ("0_0_0_0_0" -> addedIndex)
      tempWordDictionary.map({
        case (k, v) => (v, k)
      })
    }

    probabilityOfWordGivenTopic.zipWithIndex.map({ case (probOfWordGivenTopic, index) => indexToWord(index) ->
      probOfWordGivenTopic}).toMap

  }
}



