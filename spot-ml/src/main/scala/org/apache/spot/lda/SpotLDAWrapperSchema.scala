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

import org.apache.spark.sql.types.{LongType, StringType, StructField}

/**
  * Schemas and column names used in SpotLDAWrapper, FlowSuspiciousConnectsModel, DNSSuspiciousConnectsModel and
  * ProxySuspiciousConnectsModel
  */
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
