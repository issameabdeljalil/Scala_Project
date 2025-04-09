package fr.mosef.scala.template.reader.impl

import org.apache.spark.sql.{DataFrame, SparkSession}
import fr.mosef.scala.template.reader.Reader
import java.nio.file.{Paths, Files}

class ReaderImpl(sparkSession: SparkSession) extends Reader {

  // Fonction pour extraire l'extension du fichier
  private def getFileExtension(path: String): String = {
    val extension = Paths.get(path).toString.split("\\.").lastOption.getOrElse("")
    extension.toLowerCase
  }

  // Implémentation de la méthode read(format, options, path) définie dans le trait Reader
  def read(format: String, options: Map[String, String], path: String): DataFrame = {
    sparkSession
      .read
      .options(options)
      .format(format)
      .load(path)
  }

  // Implémentation de la méthode read(path, csvSeparator) pour détecter l'extension et lire le fichier CSV avec un séparateur
  def read(path: String, csvSeparator: String = ","): DataFrame = {
    // Extraire l'extension du fichier
    val extension = getFileExtension(path)

    extension match {
      case "csv" =>
        sparkSession
          .read
          .option("sep", csvSeparator)  // Utilisation du séparateur CSV passé en argument
          .option("inferSchema", "true")
          .option("header", "true")
          .format("csv")
          .load(path)

      case "json" =>
        sparkSession
          .read
          .option("inferSchema", "true")
          .option("multiLine", "true") // Pour les fichiers JSON multi-lignes
          .format("json")
          .load(path)

      case "parquet" =>
        sparkSession
          .read
          .format("parquet")
          .load(path)

      case _ =>
        throw new IllegalArgumentException(s"Format inconnu pour l'extension '$extension'. Les formats supportés sont: csv, json, parquet.")
    }
  }

  // Implémentation de la méthode read() pour un DataFrame vide pour les tests
  def read(): DataFrame = {
    sparkSession.sql("SELECT 'Empty DataFrame for unit testing'")
  }
}
