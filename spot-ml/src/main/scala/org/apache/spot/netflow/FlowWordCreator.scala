package org.apache.spot.netflow

import org.apache.spark.sql.functions._
import org.apache.spot.utilities.Quantiles
import org.apache.spot.utilities.data.validation.InvalidDataHandler

import scala.util.{Failure, Success, Try}


/**
  * Pair of netflow words extracted from a netflow record - one for the source IP and one for the destination IP.
  *
  * @param srcWord The word summarizing the communication from the POV of the source IP.
  * @param dstWord The word summarizing the communication from the POV of the destination IP.
  */
case class FlowWords(srcWord: String, dstWord: String)

/**
  * Contains methods and Spark SQL udf objects for calculation of netflow words from netflow records.
  *
  * @param timeCuts Quantile cut-offs for the time of day. Time of day is a floating point number
  *                 >= 0.0 and < 24.0
  * @param ibytCuts Quantile cut-offs for the inbytes.
  * @param ipktCuts Quantile cut-offs if the incoming packet counts.
  */
class FlowWordCreator(timeCuts: Array[Double],
                      ibytCuts: Array[Double],
                      ipktCuts: Array[Double]) extends Serializable {


  /**
    * Spark SQL UDF for calculating the word summarizing a netflow transaction at the source IP
    * @return String "word" summarizing a netflow connection.
    */
  def srcWordUDF = udf((hour: Int,
                        minute: Int,
                        second: Int,
                        srcIP: String,
                        dstIP: String,
                        srcPort: Int,
                        dstPort: Int,
                        ipkt: Long,
                        ibyt: Long) =>
    flowWords(hour, minute, second, srcPort, dstPort, ipkt, ibyt).srcWord)


  /**
    * Spark SQL UDF for calculating the word summarizing a netflow transaction at the destination IP
    * @return String "word" summarizing a netflow connection.
    */
  def dstWordUDF = udf((hour: Int,
                        minute: Int,
                        second: Int,
                        srcIP: String,
                        dstIP: String,
                        srcPort: Int,
                        dstPort: Int,
                        ipkt: Long,
                        ibyt: Long) =>
    flowWords(hour, minute, second, srcPort, dstPort, ipkt, ibyt).dstWord)


  /**
    * Calculate the source and destination words summarizing a netflow record.
    *
    * @param hour
    * @param minute
    * @param second
    * @param srcPort
    * @param dstPort
    * @param ipkt
    * @param ibyt
    * @return [[FlowWords]] containing source and destination words.
    */
  def flowWords(hour: Int, minute: Int, second: Int, srcPort: Int, dstPort: Int, ipkt: Long, ibyt: Long): FlowWords = {

    Try {
      val timeOfDay: Double = hour.toDouble + minute.toDouble / 60 + second.toDouble / 3600

      val timeBin = Quantiles.bin(timeOfDay, timeCuts)
      val ibytBin = Quantiles.bin(ibyt, ibytCuts)
      val ipktBin = Quantiles.bin(ipkt, ipktCuts)


      val LowToLowPortEncoding = 111111
      val HighToHighPortEncoding = 333333

      if (dstPort == 0 && srcPort == 0) {

        val baseWord = Array("0", timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)

      } else if (dstPort == 0 && srcPort > 0) {

        val baseWord = Array(srcPort.toString(), timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = "-1_" + baseWord, dstWord = baseWord)

      } else if (srcPort == 0 && dstPort > 0) {

        val baseWord = Array(dstPort.toString(), timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = "-1_" + baseWord)

      } else if (srcPort <= 1024 && dstPort <= 1024) {

        val baseWord = Array(LowToLowPortEncoding, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)

      } else if (srcPort <= 1024 && dstPort > 1024) {

        val baseWord = Array(srcPort.toString(), timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = "-1_" + baseWord, dstWord = baseWord)

      } else if (srcPort > 1024 && dstPort <= 1024) {

        val baseWord = Array(dstPort.toString(), timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = "-1_" + baseWord)

      } else {

        // this is the srcPort > 1024 && dstPort > 1024 case

        val baseWord = Array(HighToHighPortEncoding, timeBin, ibytBin, ipktBin).mkString("_")
        FlowWords(srcWord = baseWord, dstWord = baseWord)
      }

    } match {
      case Success(flowWords) => flowWords
      case _ => FlowWords(InvalidDataHandler.WordError, InvalidDataHandler.WordError)
    }
  }

}

