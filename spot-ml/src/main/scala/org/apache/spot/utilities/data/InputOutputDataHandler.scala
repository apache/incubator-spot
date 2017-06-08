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

package org.apache.spot.utilities.data

import org.apache.hadoop.fs.{FileSystem, LocatedFileStatus, Path, RemoteIterator, FileUtil => fileUtil}
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}

/**
  * Handles input and output data for every data set or pipep line implementation.
  * One method to read input records and one method to merge results in HDFS.
  */
object InputOutputDataHandler {

  /**
    *
    * @param sqlContext Application SqlContext.
    * @param inputPath  HDFS input folder for every execution; flow, dns or proxy.
    * @param logger     Application logger.
    * @return raw data frame.
    */
  def getInputDataFrame(sqlContext: SQLContext, inputPath: String, logger: Logger): Option[DataFrame] = {
    try {
      logger.info("Loading data from: " + inputPath)
      Some(sqlContext.read.parquet(inputPath))
    } catch {
      case _: Throwable => None
    }
  }

  /**
    *
    * @param sparkContext Application Spark Context.
    * @param feedbackFile Feedback file location.
    * @return new RDD[String] with feedback or empty if file does not exists.
    */
  def getFeedbackRDD(sparkContext: SparkContext, feedbackFile: String): RDD[String] = {

    val hadoopConfiguration = sparkContext.hadoopConfiguration
    val fs = FileSystem.get(hadoopConfiguration)

    // We need to pass a default value "file" if fileName is "" to avoid error
    // java.lang.IllegalArgumentException: Can not create a Path from an empty string
    // when trying to create a new Path object with empty string.
    val fileExists = fs.exists(new Path(if (feedbackFile == "") "file" else feedbackFile))

    if (fileExists) {

      // feedback file is a tab-separated file with a single header line. We need to remove the header
      val lines = sparkContext.textFile(feedbackFile)
      val header = lines.first()
      lines.filter(line => line != header)

    } else {
      sparkContext.emptyRDD[String]
    }
  }

  /**
    *
    * @param sparkContext      Application SparkContext.
    * @param hdfsScoredConnect HDFS output folder. The location where results were saved; flow, dns or proxy.
    * @param analysis          Data type to analyze.
    * @param logger            Application Logger.
    */
  def mergeResultsFiles(sparkContext: SparkContext, hdfsScoredConnect: String, analysis: String, logger: Logger) {
    val hadoopConfiguration = sparkContext.hadoopConfiguration
    val fileSystem = org.apache.hadoop.fs.FileSystem.get(hadoopConfiguration)

    val fileExists = fileSystem.exists(new org.apache.hadoop.fs.Path(hdfsScoredConnect))

    if (fileExists) {
      val srcDir = new Path(hdfsScoredConnect)
      val dstFile = new Path(hdfsScoredConnect + "/" + analysis + "_results.csv")
      fileUtil.copyMerge(fileSystem, srcDir, fileSystem, dstFile, false, hadoopConfiguration, "")

      val files: RemoteIterator[LocatedFileStatus] = fileSystem.listFiles(srcDir, false)
      while (files.hasNext) {
        val filePath = files.next.getPath
        if (filePath.toString.contains("part-")) {
          fileSystem.delete(filePath, false)
        }
      }
    }
    else logger.info(s"Couldn't find results in $hdfsScoredConnect." +
      s"Please check previous logs to see if there were errors.")
  }

}
