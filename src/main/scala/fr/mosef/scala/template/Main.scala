package fr.mosef.scala.template

import fr.mosef.scala.template.job.Job
import fr.mosef.scala.template.processor.Processor
import fr.mosef.scala.template.processor.impl.ProcessorImpl
import fr.mosef.scala.template.reader.Reader
import fr.mosef.scala.template.reader.impl.ReaderImpl
import org.apache.spark.sql.{DataFrame, SparkSession}
import fr.mosef.scala.template.writer.Writer
import org.apache.spark.SparkConf
import com.globalmentor.apache.hadoop.fs.BareLocalFileSystem
import org.apache.hadoop.fs.FileSystem

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

  val cliArgs = args
  val MASTER_URL: String = try {
    cliArgs(0)
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => "local[1]"
  }
  val SRC_PATH: String = try {
    cliArgs(1)
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      print("No input defined")
      sys.exit(1)
    }
  }
  val DST_PATH: String = try {
    cliArgs(2)
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      "./default/output-writer"
    }
  }
  val TRANSFORMATION_TYPE: String = try {
    cliArgs(3)
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      "count" // Transformation par défaut
    }
  }

  // Vérifier d'abord si le 4ème argument pourrait être un format ou une chaîne "sep"
  val arg4Format = try {
    val arg = cliArgs(4)
    if (arg.startsWith("sep")) {
      // C'est un séparateur, pas un format
      getFileFormat(SRC_PATH)
    } else {
      // C'est probablement un format
      arg.toLowerCase
    }
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      // Détermine le format en fonction de l'extension du fichier
      val format = getFileFormat(SRC_PATH)
      println(s"Format d'entrée détecté à partir de l'extension: $format")
      format
    }
  }

  val INPUT_FORMAT: String = arg4Format
  println(s"Format d'entrée: $INPUT_FORMAT")

  val OUTPUT_FORMAT: String = try {
    val arg = cliArgs(5)
    if (arg != null && !arg.startsWith("sep")) {
      println(s"Format de sortie spécifié: $arg")
      arg.toLowerCase
    } else {
      // Détermine le format en fonction de l'extension du fichier
      val format = getFileFormat(DST_PATH)
      println(s"Format de sortie détecté à partir de l'extension: $format")
      format
    }
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      // Détermine le format en fonction de l'extension du fichier
      val format = getFileFormat(DST_PATH)
      println(s"Format de sortie détecté à partir de l'extension: $format")
      format
    }
  }

  // Déterminer si le 4ème ou le 6ème argument est un séparateur
  val separator = try {
    if (cliArgs(4).startsWith("sep")) {
      // Le 4ème argument est un séparateur
      extractSeparator(cliArgs(4))
    } else {
      // Vérifier le 6ème argument
      try {
        extractSeparator(cliArgs(6))
      } catch {
        case e: java.lang.ArrayIndexOutOfBoundsException => ","
      }
    }
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => ","
  }

  // Nouveau paramètre pour le séparateur CSV
  val CSV_DELIMITER: String = separator
  println(s"Séparateur CSV: '$CSV_DELIMITER'")

  // Nouveau paramètre pour indiquer si le fichier CSV a un en-tête
  val CSV_HEADER: Boolean = try {
    val hasHeader = cliArgs(7).toLowerCase
    println(s"Présence d'en-tête spécifiée: $hasHeader")
    hasHeader == "true"
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      println("Présence d'en-tête par défaut: true")
      true
    }
  }

  val conf = new SparkConf()
  conf.set("spark.driver.memory", "64M")
  conf.set("spark.testing.memory", "471859200")
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
  val csv_header = CSV_HEADER

  println(s"Configuration de l'exécution:")
  println(s"- URL Master: $MASTER_URL")
  println(s"- Chemin source: $src_path")
  println(s"- Chemin destination: $dst_path")
  println(s"- Type de transformation: $transformation_type")
  println(s"- Format d'entrée: $input_format")
  println(s"- Format de sortie: $output_format")
  println(s"- Séparateur CSV: '$csv_delimiter'")
  println(s"- CSV avec en-tête: $csv_header")

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
    writer.write(processedDF, "overwrite", dst_path, output_format, csv_delimiter, csv_header)
  } else {
    writer.write(processedDF, "overwrite", dst_path, output_format)
  }

  println(s"Traitement terminé avec succès!")
}