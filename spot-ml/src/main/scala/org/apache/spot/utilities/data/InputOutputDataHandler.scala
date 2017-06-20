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

import org.apache.hadoop.fs.{LocatedFileStatus, Path, RemoteIterator, FileUtil => fileUtil}
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SparkSession}


/**
  * Handles input and output data for every data set or pipep line implementation.
  * One method to read input records and one method to merge results in HDFS.
  */
object InputOutputDataHandler {

  /**
    *
    * @param sparkSession Spark Session.
    * @param inputPath    HDFS input folder for every execution; flow, dns or proxy.
    * @param logger       Application logger.
    * @return raw data frame.
    */
  def getInputDataFrame(sparkSession: SparkSession, inputPath: String, logger: Logger): Option[DataFrame] = {
    try {
      logger.info("Loading data from: " + inputPath)
      Some(sparkSession.read.parquet(inputPath))
    } catch {
      case _: Throwable => None
    }
  }

  /**
    *
    * @param sparkSession      Spark Session.
    * @param hdfsScoredConnect HDFS output folder. The location where results were saved; flow, dns or proxy.
    * @param analysis          Data type to analyze.
    * @param logger            Application Logger.
    */
  def mergeResultsFileUtil(sparkSession: SparkSession,
                           hdfsScoredConnect: String,
                           analysis: String,
                           logger: Logger): Unit = {
    val hadoopConfiguration = sparkSession.sparkContext.hadoopConfiguration
    val fileSystem = org.apache.hadoop.fs.FileSystem.get(hadoopConfiguration)

    val exists = fileSystem.exists(new org.apache.hadoop.fs.Path(hdfsScoredConnect))

    if (exists) {
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

  def mergeResultsFileSystem(sparkSession: SparkSession,
                             hdfsScoredConnect: String,
                             analysis: String,
                             logger: Logger): Unit = {

    val hadoopConfiguration = sparkSession.sparkContext.hadoopConfiguration
    val fileSystem = org.apache.hadoop.fs.FileSystem.get(hadoopConfiguration)

    val exists = fileSystem.exists(new org.apache.hadoop.fs.Path(hdfsScoredConnect))

    if (exists) {
      // SPOT-172. Seems like FilUtil.copyMerge is not behaving well with Proxy data. Besides, it's deprecated after
      // Hadoop 2.7.x
      // Using spark to merge result files
      val tmpFileStr = s"${hdfsScoredConnect}/tmp"
      val tmpPath = new Path(s"${hdfsScoredConnect}/tmp")

      // File name after sparkContext.textFile.coalesce(1)
      val singleFileName = "part-00000"

      val srcPath = new Path(hdfsScoredConnect)
      val srcPartsStr = s"${hdfsScoredConnect}/part-*"

      val srcTmpPath = new Path(s"${tmpFileStr}/${singleFileName}")
      val dstTmpPath = new Path(s"${hdfsScoredConnect}/${analysis}_results.csv")

      // Merge all part-* into a single file
      sparkSession.sparkContext.textFile(srcPartsStr).coalesce(1).saveAsTextFile(tmpFileStr)

      // Rename the single file generated part-00000 to ${analysis}_results.csv
      fileSystem.rename(srcTmpPath, dstTmpPath)

      // Delete tmp folder
      fileSystem.delete(tmpPath, true)

      // Delete all part-* files in the same level as ${analysis}_results.csv
      val files: RemoteIterator[LocatedFileStatus] = fileSystem.listFiles(srcPath, false)
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
