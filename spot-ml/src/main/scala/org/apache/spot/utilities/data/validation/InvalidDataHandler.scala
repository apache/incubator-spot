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

package org.apache.spot.utilities.data.validation

import org.apache.log4j.Logger
import org.apache.spark.sql.DataFrame

/**
  * Handles invalid and corrupt records.
  * One method for each kind of invalid data, this object prints the total errors and saves the invalid/corrupt records.
  */
object InvalidDataHandler {

  val WordError = "word_error"
  val ScoreError = -1d

  /**
    *
    * @param invalidRecords Records with null or invalid values in key columns.
    * @param outputPath HDFS output folder for invalid records; scored_results/date/scores/invalid
    * @param logger Application logger.
    */
  def showAndSaveInvalidRecords(invalidRecords: DataFrame, outputPath: String, logger: Logger) {

    if (invalidRecords.count > 0) {

      val invalidRecordsFile = outputPath + "/invalid_records"
      logger.warn("Saving invalid records to " + invalidRecordsFile)

      invalidRecords.write.mode("overwrite").parquet(invalidRecordsFile)

      logger.warn("Total records discarded due to NULL values in key fields: " + invalidRecords.count +
        " . Please go to " + invalidRecordsFile + " for more details.")
    }
  }

  /**
    *
    * @param corruptRecords Records with Score = -1. This means that during word creation these records throw an exception
    *                       and they got assigned the word word_error and hence during scoring they got a score -1.
    * @param outputPath HDFS output folder for corrupt records; scored_results/date/scores/corrupt
    * @param logger Application logger.
    */
  def showAndSaveCorruptRecords(corruptRecords: DataFrame, outputPath: String, logger: Logger) {
    if(corruptRecords.count > 0){

      val corruptRecordsFile = outputPath + "/corrupt_records"

      logger.warn("Saving corrupt records to " + corruptRecordsFile)

      corruptRecords.write.mode("overwrite").parquet(corruptRecordsFile)

      logger.warn("Total records discarded due to invalid values in key fields: " + corruptRecords.count +
        "Please go to " + corruptRecordsFile + " for more details.")
    }
  }

}
