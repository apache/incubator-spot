package org.apache.spot.spotldacwrapper

import org.apache.spark.sql.DataFrame
/**
  * Created by rabarona on 11/14/16.
  */
case class SpotLDACOutput (docToTopicMix: DataFrame, wordResults: Map[String, Array[Double]])
