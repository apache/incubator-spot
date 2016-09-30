package org.apache.spot.netflow

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spot.netflow.{FlowSchema => Schema}
import org.apache.spot.utilities.Quantiles

object FlowWordCreation {

  def flowWordCreation(totalDataDF: DataFrame, sc: SparkContext, logger: Logger, sqlContext: SQLContext): DataFrame = {

    var ibytCuts = new Array[Double](10)
    var ipktCuts = new Array[Double](5)
    var timeCuts = new Array[Double](10)

    val udfAddTime = this.udfAddTime()
    val dataWithTime = totalDataDF.withColumn(Schema.NumTime,
      udfAddTime(totalDataDF(Schema.Hour), totalDataDF(Schema.Minute), totalDataDF(Schema.Second)))

    logger.info("calculating time cuts ...")

    timeCuts = Quantiles.computeDeciles(dataWithTime
      .select(Schema.NumTime)
      .rdd
      .map({ case Row(numTime: Double) => numTime }))

    logger.info(timeCuts.mkString(","))

    logger.info("calculating byte cuts ...")

    ibytCuts = Quantiles.computeDeciles(dataWithTime
      .select(Schema.ibyt)
      .rdd
      .map({ case Row(ibyt: Long) => ibyt.toDouble }))

    logger.info(ibytCuts.mkString(","))

    logger.info("calculating pkt cuts")

    ipktCuts = Quantiles.computeQuintiles(dataWithTime
      .select(Schema.ipkt)
      .rdd
      .map({ case Row(ipkt: Long) => ipkt.toDouble }))


    logger.info(ipktCuts.mkString(","))

    var udfBin = this.udfBin(ibytCuts)
    val dataWithIBYTBinned = dataWithTime.withColumn(Schema.IBYTBin, udfBin(dataWithTime(Schema.ibyt).cast(DoubleType).as(Schema.ibyt)))

    udfBin = this.udfBin(ipktCuts)
    val dataWithIPKTBinned = dataWithIBYTBinned.withColumn(Schema.IPKTBin, udfBin(dataWithIBYTBinned(Schema.ipkt).cast(DoubleType)).as(Schema.ipkt))

    udfBin = this.udfBin(timeCuts)
    val dataWithTimeBinned = dataWithIPKTBinned.withColumn(Schema.TimeBin, udfBin(dataWithIPKTBinned.col(Schema.NumTime)))

    val fieldNamesIndex = {
      dataWithTimeBinned.schema.fieldNames.zipWithIndex.toMap
    }

    val dataWithWordRDD: RDD[Row] = dataWithTimeBinned.rdd.map(row =>
      Row.fromSeq {
        row.toSeq ++
          adjustPort(sourceIp = row.getString(fieldNamesIndex(Schema.SourceIP)),
            destinationIp = row.getString(fieldNamesIndex(Schema.DestinationIP)),
            destinationPort = row.getInt(fieldNamesIndex(Schema.DestinationPort)),
            sourcePort = row.getInt(fieldNamesIndex(Schema.SourcePort)),
            ipktBin = row.getInt(fieldNamesIndex(Schema.IPKTBin)),
            ibytBin = row.getInt(fieldNamesIndex(Schema.IBYTBin)),
            timeBin = row.getInt(fieldNamesIndex(Schema.TimeBin)))
      })

    val schemaWithWord = {
      StructType(dataWithTimeBinned.schema.fields ++
        refArrayOps(Array(StructField(Schema.PortWord, StringType),
          StructField(Schema.IpPair, StringType),
          StructField(Schema.SourceWord, StringType),
          StructField(Schema.DestinationWord, StringType)))
      )
    }

    sqlContext.createDataFrame(dataWithWordRDD, schemaWithWord)
  }

  def udfAddTime() = udf((hour: Int, minute: Int, second: Int) => addTime(hour, minute, second))

  def addTime(hour: Int, minute: Int, second: Int): Double = {
    hour.toDouble + minute.toDouble / 60 + second.toDouble / 3600
  }

  def adjustPort(sourceIp: String,
                 destinationIp: String,
                 destinationPort: Int,
                 sourcePort: Int,
                 ipktBin: Int,
                 ibytBin: Int,
                 timeBin: Int) = {
    var wordPort : Int= 111111
    var portCase : Int = 0

    var ipPair = destinationIp + " " + sourceIp
    if (sourceIp < destinationIp && sourceIp != 0) {
      ipPair = sourceIp + " " + destinationIp
    }

    if ((destinationPort <= 1024 | sourcePort <= 1024) & (destinationPort > 1024 | sourcePort > 1024) & scala.math.min(destinationPort, sourcePort) != 0) {
      portCase = 2
      wordPort = scala.math.min(destinationPort, sourcePort)
    } else if (destinationPort > 1024 & sourcePort > 1024) {
      portCase = 3
      wordPort = 333333
    } else if (destinationPort == 0 & sourcePort != 0) {
      wordPort = sourcePort
      portCase = 4
    } else if (sourcePort == 0 & destinationPort != 0) {
      wordPort = destinationPort
      portCase = 4
    } else {
      portCase = 1
      if (scala.math.min(destinationPort, sourcePort) == 0) {
        wordPort = scala.math.max(destinationPort, sourcePort)
      } else {
        wordPort = 111111
      }
    }

    val word = Array(wordPort.toString, timeBin.toString, ibytBin.toString, ipktBin.toString).mkString("_")
    var sourceWord = word
    var destinationWord = word

    if (portCase == 2 & destinationPort < sourcePort) {
      destinationWord = "-1_" + destinationWord
    } else if (portCase == 2 & sourcePort < destinationPort) {
      sourceWord = "-1_" + sourceWord
    } else if (portCase == 4 & destinationPort == 0) {
      sourceWord = "-1_" + sourceWord
    } else if (portCase == 4 & sourcePort == 0) {
      destinationWord = "-1_" + destinationWord
    }
    Array(wordPort.toString, ipPair, sourceWord, destinationWord)
  }

  def udfBin(cuts: Array[Double]) = udf((column: Double) => Quantiles.bin(column, cuts))

}

