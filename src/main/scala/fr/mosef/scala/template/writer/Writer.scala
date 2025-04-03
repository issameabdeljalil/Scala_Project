package fr.mosef.scala.template.writer

import org.apache.spark.sql.DataFrame

class Writer {

  def write(df: DataFrame, mode: String = "overwrite", path: String): Unit = {
    // Détermine le format en fonction de l'extension du fichier
    val format = getFileFormat(path)

    write(df, mode, path, format)
  }

  def write(df: DataFrame, mode: String, path: String, format: String): Unit = {
    format.toLowerCase match {
      case "csv" =>
        df
          .write
          .option("header", "true")
          .mode(mode)
          .csv(path)
      case "parquet" =>
        df
          .write
          .mode(mode)
          .parquet(path)
      case _ =>
        throw new IllegalArgumentException(s"Format d'écriture non supporté: $format")
    }
  }

  def write(df: DataFrame, mode: String, path: String, format: String, delimiter: String, writeHeader: Boolean): Unit = {
    println(s"Écriture avec format: $format, délimiteur: '$delimiter', en-tête: $writeHeader")

    format.toLowerCase match {
      case "csv" =>
        df
          .write
          .option("header", writeHeader)
          .option("sep", delimiter)
          .mode(mode)
          .csv(path)
      case "parquet" =>
        df
          .write
          .mode(mode)
          .parquet(path)
      case _ =>
        throw new IllegalArgumentException(s"Format d'écriture non supporté: $format")
    }
  }

  private def getFileFormat(path: String): String = {
    val lowerPath = path.toLowerCase
    if (lowerPath.endsWith(".csv")) {
      "csv"
    } else if (lowerPath.endsWith(".parquet")) {
      "parquet"
    } else {
      // Par défaut, on suppose que c'est un CSV
      "csv"
    }
  }
}