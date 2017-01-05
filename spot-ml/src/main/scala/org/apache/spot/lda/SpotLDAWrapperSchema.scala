package org.apache.spot.lda

import org.apache.spark.sql.types.{LongType, StringType, StructField}

object SpotLDAWrapperSchema {

  // modelDF columns
  val DocumentName = "document_name"
  val DocumentNameField = StructField(DocumentName, StringType)

  val DocumentNumber = "document_number"
  val DocumentNumberField = StructField(DocumentNumber, LongType)

  val DocumentCount = "document_count"
  val DocumentNameWordNameWordCount = "document_word_count"

  val WordNameWordCount = "word_count"
  val WordName = "word_name"
  val WordNumber = "word_number"

  // documentResults
  val TopicProbabilityMix = "topic_prob_mix"
  val TopicProbabilityMixArray = "topic_prob_mix_array"

  // wordResults
  val _0_0_0_0_0 = "0_0_0_0_0"

}
