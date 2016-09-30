package org.apache.spot.utilities

import scala.io.Source


object TopDomains {
  val alexaTop1MPath = "top-1m.csv"

  val TopDomains: Set[String] = Source.fromFile(alexaTop1MPath).getLines.map(line => {
    val parts = line.split(",")
    val l = parts.length
    parts(1).split('.')(0)
  }).toSet
}
