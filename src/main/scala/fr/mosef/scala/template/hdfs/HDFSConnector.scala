package fr.mosef.scala.template.hdfs

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.{DataFrame, SparkSession}

class HDFSConnector(hdfsUrl: String) {
  private val conf = new Configuration()
  conf.set("fs.defaultFS", hdfsUrl)
  private val fs = FileSystem.get(conf)

  // Vérifier si un fichier existe sur HDFS
  def fileExists(path: String): Boolean = {
    fs.exists(new Path(path))
  }

  // Lire un fichier depuis HDFS avec Spark
  def readWithSpark(spark: SparkSession, path: String, format: String): DataFrame = {
    format.toLowerCase match {
      case "csv" =>
        spark.read
          .option("header", "true")
          .option("inferSchema", "true")
          .csv(path)
      case "parquet" =>
        spark.read.parquet(path)
      case "json" =>
        spark.read.json(path)
      case _ =>
        throw new IllegalArgumentException(s"Format '$format' non pris en charge pour HDFS")
    }
  }

  // Écrire un DataFrame sur HDFS
  def writeWithSpark(df: DataFrame, path: String, format: String, mode: String = "overwrite"): Unit = {
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
      case "json" =>
        df.write
          .mode(mode)
          .json(path)
      case _ =>
        throw new IllegalArgumentException(s"Format '$format' non pris en charge pour HDFS")
    }
  }

  // Transférer un fichier local vers HDFS
  def copyFromLocal(localPath: String, hdfsPath: String): Unit = {
    fs.copyFromLocalFile(new Path(localPath), new Path(hdfsPath))
  }

  // Télécharger un fichier de HDFS vers le système local
  def copyToLocal(hdfsPath: String, localPath: String): Unit = {
    fs.copyToLocalFile(new Path(hdfsPath), new Path(localPath))
  }

  // Supprimer un fichier sur HDFS
  def deleteFile(path: String, recursive: Boolean = false): Boolean = {
    fs.delete(new Path(path), recursive)
  }

  // Fermer la connexion HDFS
  def close(): Unit = {
    fs.close()
  }
}