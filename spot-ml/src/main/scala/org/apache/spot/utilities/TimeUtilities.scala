package org.apache.spot.utilities


object TimeUtilities {

  // convert HH:MM:SS string to seconds

  def getTimeAsDouble(timeStr: String) : Double = {
    val s = timeStr.split(":")
    val hours = s(0).toInt
    val minutes = s(1).toInt
    val seconds = s(2).toInt

    (3600*hours + 60*minutes + seconds).toDouble
  }
}
