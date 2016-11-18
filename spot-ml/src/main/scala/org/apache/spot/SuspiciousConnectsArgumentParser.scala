package org.apache.spot


/**
  * Parses arguments for the suspicious connections analysis.
  */
object SuspiciousConnectsArgumentParser {

  case class SuspiciousConnectsConfig(analysis: String = "",
                                      inputPath: String = "",
                                      scoresFile: String = "",
                                      duplicationFactor: Int = 1,
                                      modelFile: String = "",
                                      topicDocumentFile: String = "",
                                      topicWordFile: String = "",
                                      mpiPreparationCmd: String = "",
                                      mpiCmd: String = "",
                                      mpiProcessCount: String = "",
                                      topicCount: Int = 20,
                                      localPath: String = "",
                                      localUser: String = "",
                                      ldaPath: String = "",
                                      nodes: String = "",
                                      hdfsScoredConnect: String = "",
                                      hdfsModelFile: String = "",
                                      threshold: Double = 1.0d,
                                      maxResults: Int = -1,
                                      outputDelimiter: String = "\t",
                                      ldaPRGSeed: Option[Long] = None)

  val parser: scopt.OptionParser[SuspiciousConnectsConfig] = new scopt.OptionParser[SuspiciousConnectsConfig]("LDA") {

    head("LDA Process", "1.1")

    opt[String]("analysis").required().valueName("< flow | proxy | dns >").
      action((x, c) => c.copy(analysis = x)).
      text("choice of suspicious connections analysis to perform")

    opt[String]("input").required().valueName("<hdfs path>").
      action((x, c) => c.copy(inputPath = x)).
      text("HDFS path to input")

    opt[String]("feedback").valueName("<local file>").
      action((x, c) => c.copy(scoresFile = x)).
      text("the local path of the file that contains the feedback scores")

    opt[Int]("dupfactor").valueName("<non-negative integer>").
      action((x, c) => c.copy(duplicationFactor = x)).
      text("duplication factor controlling how to downgrade non-threatening connects from the feedback file")

    opt[String]("model").required().valueName("<local file>").
      action((x, c) => c.copy(modelFile = x)).
      text("Model file location")

    opt[String]("topicdoc").required().valueName("<local file>").
      action((x, c) => c.copy(topicDocumentFile = x)).
      text("final.gamma file location")

    opt[String]("topicword").required().valueName("<local file>").
      action((x, c) => c.copy(topicWordFile = x)).
      text("final.beta file location")

    opt[String]("mpiprep").valueName("<mpi command>").
      action((x, c) => c.copy(mpiPreparationCmd = x)).
      text("MPI preparation command")

    opt[String]("mpicmd").required().valueName("<mpi command>").
      action((x, c) => c.copy(mpiCmd = x)).
      text("MPI command")

    opt[String]("proccount").required().valueName("<mpi param>").
      action((x, c) => c.copy(mpiProcessCount = x)).
      text("MPI process count")

    opt[String]("topiccount").required().valueName("number of topics in topic model").
      action((x, c) => c.copy(topicCount = x.toInt)).
      text("topic count")

    opt[String]("lpath").required().valueName("<local path>").
      action((x, c) => c.copy(localPath = x)).
      text("Local Path")

    opt[String]("ldapath").required().valueName("<local path>").
      action((x, c) => c.copy(ldaPath = x)).
      text("LDA Path")

    opt[String]("luser").required().valueName("<local path>").
      action((x, c) => c.copy(localUser = x)).
      text("Local user path")

    opt[String]("nodes").required().valueName("<input param>").
      action((x, c) => c.copy(nodes = x)).
      text("Node list")

    opt[String]("scored").required().valueName("<hdfs path>").
      action((x, c) => c.copy(hdfsScoredConnect = x)).
      text("HDFS path for results")

    opt[String]("tempmodel").required().valueName("<hdfs path>").
      action((x, c) => c.copy(hdfsModelFile = x)).
      text("HDFS path for model (temporary location)")

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
  }
}
