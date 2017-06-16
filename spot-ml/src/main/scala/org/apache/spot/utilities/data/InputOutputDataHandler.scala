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
    * @param spark     Spark Session.
    * @param inputPath HDFS input folder for every execution; flow, dns or proxy.
    * @param logger    Application logger.
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
    * @param spark             Spark Session.
    * @param hdfsScoredConnect HDFS output folder. The location where results were saved; flow, dns or proxy.
    * @param analysis          Data type to analyze.
    * @param logger            Application Logger.
    */
  def mergeResultsFiles(sparkSession: SparkSession, hdfsScoredConnect: String, analysis: String, logger: Logger) {
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

}
