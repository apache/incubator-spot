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

name := "spot-ml"

version := "1.1"

scalaVersion := "2.11.8"

val sparkVersion = "2.1.0"

import sbtassembly.Plugin.AssemblyKeys._

assemblySettings

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6"
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
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case "about.html" => MergeStrategy.rename
  case meta(_) => MergeStrategy.discard
  case x => MergeStrategy.first
}
}

// super important with multiple tests running spark Contexts
parallelExecution in Test := false

lazy val getTop1MFileFromAlexa = taskKey[Seq[File]]("Download Amazon/alexa-static top-1m.csv file for DNS and Proxy")

getTop1MFileFromAlexa := {
  if (!java.nio.file.Files.exists(new File("top-1m/top-1m.csv").toPath())) {
    val location = url("http://s3.amazonaws.com/alexa-static/top-1m.csv.zip")
    println("Getting top-1m.csv from Amazon/alexa-static")
    IO.unzipURL(location, new File("top-1m")).toSeq
  }
  else {
    println("File top-1m.csv already provided")
    Seq.empty[File]
  }
}

resourceGenerators in Compile <+= getTop1MFileFromAlexa