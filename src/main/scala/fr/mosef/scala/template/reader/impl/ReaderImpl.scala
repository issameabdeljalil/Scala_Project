package fr.mosef.scala.template.reader.impl

import org.apache.spark.sql.{DataFrame, SparkSession}
import fr.mosef.scala.template.reader.Reader

class ReaderImpl(sparkSession: SparkSession) extends Reader {

  def read(format: String, options: Map[String, String], path: String): DataFrame = {
    sparkSession
      .read
      .options(options)
      .format(format)
      .load(path)
  }

  def read(path: String): DataFrame = {
    // Détermine le format en fonction de l'extension du fichier
    val format = getFileFormat(path)

    format match {
      case "csv" =>
        sparkSession
          .read
          .option("sep", ",")
          .option("inferSchema", "true")
          .option("header", "true")
          .format("csv")
          .load(path)
      case "parquet" =>
        sparkSession
          .read
          .format("parquet")
          .load(path)
      case _ =>
        throw new IllegalArgumentException(s"Format non supporté: $format")
    }
  }

  def read(path: String, format: String): DataFrame = {
    format.toLowerCase match {
      case "csv" =>
        sparkSession
          .read
          .option("sep", ",")
          .option("inferSchema", "true")
          .option("header", "true")
          .format("csv")
          .load(path)
      case "parquet" =>
        sparkSession
          .read
          .format("parquet")
          .load(path)
      case _ =>
        throw new IllegalArgumentException(s"Format non supporté: $format")
    }
  }

  def read(path: String, format: String, delimiter: String, hasHeader: Boolean): DataFrame = {
    println(s"Lecture avec format: $format, délimiteur: '$delimiter', en-tête: $hasHeader")

    format.toLowerCase match {
      case "csv" =>
        sparkSession
          .read
          .option("sep", delimiter)
          .option("inferSchema", "true")
          .option("header", hasHeader)
          .format("csv")
          .load(path)
      case "parquet" =>
        sparkSession
          .read
          .format("parquet")
          .load(path)
      case _ =>
        throw new IllegalArgumentException(s"Format non supporté: $format")
    }
  }

  def read(): DataFrame = {
    sparkSession.sql("SELECT 'Empty DataFrame for unit testing implementation'")
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