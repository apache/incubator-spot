name := "spot-ml"

version := "1.1"

scalaVersion := "2.10.6"

import AssemblyKeys._

assemblySettings

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.0"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.6.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.6.0"
libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.2.6"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"

resolvers += Resolver.sonatypeRepo("public")

val meta = """META.INF(.)*""".r

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case PathList("org", "apache", "commons", xs@_*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", "minlog", xs@_*) => MergeStrategy.last
  case PathList("com", "google", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", "hadoop", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", "spark", xs@_*) => MergeStrategy.last
  case PathList("javax", "xml", xs@_*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case meta(_) => MergeStrategy.discard
  case x => old(x)
}
}

