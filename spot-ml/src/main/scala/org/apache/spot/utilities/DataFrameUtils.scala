package org.apache.spot.utilities

import org.apache.spark.sql.{DataFrame, Row}

/**
  * Some handy operations on dataframes not provided by Apache Spark.
  */
object  DataFrameUtils {

  /**
    * Returns the rows of a dataframe whose values in a provided column are in the first k
    * (least to greatest) values.  If strictly fewer than k rows are in the dataframe, all rows are returned.
    *
    * Dataframe analog to takeOrdered.
    *
    * @param df Input dataframe.
    * @param colName Column to consider.
    * @param k Maximum number of rows to return.
    * @return Array of (at most k) rows.
    */
  def dfTakeOrdered(df: DataFrame, colName: String, k: Int) : Array[Row] = {
    val count = df.count

    val takeCount  = if (k == -1 || count < k) {
      count.toInt
    } else {
      k
    }

    val colIndex = df.schema.fieldNames.indexOf(colName)

    class DataOrdering() extends Ordering[Row] {
      def compare(row1: Row, row2: Row) = row1.getDouble(colIndex).compare(row2.getDouble(colIndex))
    }

    implicit val rowOrdering = new DataOrdering()
    df.rdd.takeOrdered(takeCount)
  }
}
