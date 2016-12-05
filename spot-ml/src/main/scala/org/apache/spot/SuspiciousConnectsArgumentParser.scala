package org.apache.spot


/**
  * Parses arguments for the suspicious connections analysis.
  */
object SuspiciousConnectsArgumentParser {

  case class SuspiciousConnectsConfig(analysis: String = "",
                                      inputPath: String = "",
                                      feedbackFile: String = "",
                                      duplicationFactor: Int = 1,
                                      topicCount: Int = 20,
                                      hdfsScoredConnect: String = "",
                                      threshold: Double = 1.0d,
                                      maxResults: Int = -1,
                                      outputDelimiter: String = "\t",
                                      ldaPRGSeed: Option[Long] = None,
                                      ldaMaxiterations: Int = 20,
                                      ldaAlpha: Double = 1.02,
                                      ldaBeta: Double = 1.001)

  val parser: scopt.OptionParser[SuspiciousConnectsConfig] = new scopt.OptionParser[SuspiciousConnectsConfig]("LDA") {

    head("LDA Process", "1.1")

    opt[String]("analysis").required().valueName("< flow | proxy | dns >").
      action((x, c) => c.copy(analysis = x)).
      text("choice of suspicious connections analysis to perform")

    opt[String]("input").required().valueName("<hdfs path>").
      action((x, c) => c.copy(inputPath = x)).
      text("HDFS path to input")

    opt[String]("feedback").valueName("<local file>").
      action((x, c) => c.copy(feedbackFile = x)).
      text("the local path of the file that contains the feedback scores")

    opt[Int]("dupfactor").valueName("<non-negative integer>").
      action((x, c) => c.copy(duplicationFactor = x)).
      text("duplication factor controlling how to downgrade non-threatening connects from the feedback file")


    opt[String]("ldatopiccount").required().valueName("number of topics in topic model").
      action((x, c) => c.copy(topicCount = x.toInt)).
      text("topic count")

    opt[String]("scored").required().valueName("<hdfs path>").
      action((x, c) => c.copy(hdfsScoredConnect = x)).
      text("HDFS path for results")

    opt[Double]("threshold").required().valueName("float64").
      action((x, c) => c.copy(threshold = x)).
      text("probability threshold for declaring anomalies")

    opt[Int]("maxresults").required().valueName("integer").
      action((x, c) => c.copy(maxResults = x)).
      text("number of most suspicious connections to return")

    opt[String]("delimiter").optional().valueName("character").
      action((x, c) => c.copy(outputDelimiter = x)).
      text("number of most suspicious connections to return")

    opt[String]("prgseed").optional().valueName("long").
      action((x, c) => c.copy(ldaPRGSeed = Some(x.toLong))).
      text("seed for the pseudorandom generator")

    opt[Int]("ldamaxiterations").optional().valueName("int").
      action((x, c) => c.copy(ldaMaxiterations = x)).
      text("maximum number of iterations used in LDA")


    opt[Double]("ldaalpha").optional().valueName("float64").
      action((x, c) => c.copy(ldaAlpha = x)).
      text("document concentration for lda, default 1.02")

    opt[Double]("ldabeta").optional().valueName("float64").
      action((x, c) => c.copy(ldaBeta = x)).
      text("topic concentration for lda, default 1.001")
  }
}
