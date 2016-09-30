package org.apache.spot.utilities

import scala.math._

/**
  * Created by nlsegerl on 8/4/16.
  */
object Entropy {

  /**
    * Calculates the "entropy" in string, interpreted as a distribution on its constituent characters.
    *
    * @param v Input string
    * @return
    */
  def stringEntropy(v: String): Double = {
    if (v.length() > 0) {
      v.groupBy(a => a).values.map(i => i.length.toDouble / v.length).map(p => -p * log10(p) / log10(2)).sum
    } else {
      0
    }
  }
}
