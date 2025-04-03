package fr.mosef.scala.template

import fr.mosef.scala.template.job.Job
import fr.mosef.scala.template.processor.Processor
import fr.mosef.scala.template.processor.impl.ProcessorImpl
import fr.mosef.scala.template.reader.Reader
import fr.mosef.scala.template.reader.impl.ReaderImpl
import fr.mosef.scala.template.writer.Writer
import fr.mosef.scala.template.util.ConfigUtil
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.SparkConf
import com.globalmentor.apache.hadoop.fs.BareLocalFileSystem
import org.apache.hadoop.fs.FileSystem

import java.util.Properties

object Main extends App with Job {

  // Fonction utilitaire pour déterminer le format basé sur l'extension du fichier
  def getFileFormat(path: String): String = {
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

  // Fonction pour extraire le séparateur d'une chaîne "sep,"
  def extractSeparator(sepString: String): String = {
    if (sepString != null && sepString.startsWith("sep")) {
      sepString.substring(3)
    } else {
      sepString
    }
  }

  // Charger le fichier de configuration
  val configPath = if (args.length > 0 && args(0).endsWith(".properties")) args(0) else "application.properties"
  val properties = ConfigUtil.loadProperties(configPath)

  // Définir les chemins source et destination
  val SRC_PATH: String = if (args.length > 0) {
    args(0)
  } else {
    val msg = "Chemin source non spécifié. Veuillez fournir le chemin du fichier source en argument."
    println(msg)
    sys.exit(1)
  }

  val DST_PATH: String = if (args.length > 2) {
    args(2)
  } else {
    ConfigUtil.getProperty(properties, "app.output.path", "./default/output-writer")
  }

  // Lire les configurations depuis le fichier properties
  val MASTER_URL: String = ConfigUtil.getProperty(properties, "spark.master", "local[1]")
  val TRANSFORMATION_TYPE: String = if (args.length > 3) args(3) else ConfigUtil.getProperty(properties, "app.transformation", "count")
  val INPUT_FORMAT: String = if (args.length > 4) args(4) else ConfigUtil.getProperty(properties, "app.input.format", getFileFormat(SRC_PATH))
  val OUTPUT_FORMAT: String = if (args.length > 5) args(5) else ConfigUtil.getProperty(properties, "app.output.format", getFileFormat(DST_PATH))

  // Extraire le séparateur
  val CSV_DELIMITER: String = if (args.length > 6) {
    extractSeparator(args(6))
  } else {
    ConfigUtil.getProperty(properties, "app.input.delimiter", ",")
  }

  val CSV_OUTPUT_DELIMITER: String = if (args.length > 7) {
    extractSeparator(args(7))
  } else {
    ConfigUtil.getProperty(properties, "app.output.delimiter", CSV_DELIMITER)
  }

  val CSV_HEADER: Boolean = if (args.length > 8) {
    args(8).toLowerCase == "true"
  } else {
    ConfigUtil.getPropertyAsBoolean(properties, "app.input.header", true)
  }

  val CSV_OUTPUT_HEADER: Boolean = if (args.length > 9) {
    args(9).toLowerCase == "true"
  } else {
    ConfigUtil.getPropertyAsBoolean(properties, "app.output.header", true)
  }

  // Configuration Spark
  val conf = new SparkConf()
  conf.set("spark.driver.memory", ConfigUtil.getProperty(properties, "spark.driver.memory", "64M"))
  conf.set("spark.testing.memory", ConfigUtil.getProperty(properties, "spark.testing.memory", "471859200"))
  conf.set("spark.driver.host", "localhost")

  val sparkSession = SparkSession
    .builder
    .master(MASTER_URL)
    .config(conf)
    .appName("Scala Template")
    .enableHiveSupport()
    .getOrCreate()

  sparkSession
    .sparkContext
    .hadoopConfiguration
    .setClass("fs.file.impl", classOf[BareLocalFileSystem], classOf[FileSystem])

  val reader: Reader = new ReaderImpl(sparkSession)
  val processor: Processor = new ProcessorImpl()
  val writer: Writer = new Writer()
  val src_path = SRC_PATH
  val dst_path = DST_PATH
  val transformation_type = TRANSFORMATION_TYPE
  val input_format = INPUT_FORMAT
  val output_format = OUTPUT_FORMAT
  val csv_delimiter = CSV_DELIMITER
  val csv_output_delimiter = CSV_OUTPUT_DELIMITER
  val csv_header = CSV_HEADER
  val csv_output_header = CSV_OUTPUT_HEADER

  println(s"Configuration de l'exécution:")
  println(s"- URL Master: $MASTER_URL")
  println(s"- Chemin source: $src_path")
  println(s"- Chemin destination: $dst_path")
  println(s"- Type de transformation: $transformation_type")
  println(s"- Format d'entrée: $input_format")
  println(s"- Format de sortie: $output_format")
  println(s"- Séparateur CSV d'entrée: '$csv_delimiter'")
  println(s"- Séparateur CSV de sortie: '$csv_output_delimiter'")
  println(s"- CSV avec en-tête (entrée): $csv_header")
  println(s"- CSV avec en-tête (sortie): $csv_output_header")

  println(s"Lecture du fichier source: $src_path (format: $input_format)")

  // Utilisation des paramètres du séparateur et de l'en-tête pour la lecture CSV
  val inputDF: DataFrame = if (input_format == "csv") {
    reader.read(src_path, input_format, csv_delimiter, csv_header)
  } else {
    reader.read(src_path, input_format)
  }

  println(s"Application de la transformation: $transformation_type")
  val processedDF: DataFrame = processor.process(inputDF, transformation_type)

  println(s"Écriture des résultats dans: $dst_path (format: $output_format)")
  // Utilisation des paramètres du séparateur et de l'en-tête pour l'écriture CSV
  if (output_format == "csv") {
    writer.write(processedDF, "overwrite", dst_path, output_format, csv_output_delimiter, csv_output_header)
  } else {
    writer.write(processedDF, "overwrite", dst_path, output_format)
  }

  println(s"Traitement terminé avec succès!")
}