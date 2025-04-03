package fr.mosef.scala.template.writer

import org.apache.spark.sql.DataFrame

class Writer {

  def write(df: DataFrame, format: String = "csv", mode: String = "overwrite", path: String): Unit = {
    format.toLowerCase match {
      case "csv" =>
        df.write
          .option("header", "true")
          .mode(mode)
          .csv(path)

      case "parquet" =>
        df.write
          .mode(mode)
          .parquet(path)

      case _ =>
        throw new IllegalArgumentException(s"❌ Format '$format' non pris en charge. Les formats pris en charge sont: csv, parquet.")
    }
  }
}
