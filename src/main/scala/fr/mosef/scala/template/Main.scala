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

  // Ajout de l'argument pour le format de sortie (par défaut "csv")
  val OUTPUT_FORMAT: String = try {
    cliArgs(3).toLowerCase
  } catch {
    case _: ArrayIndexOutOfBoundsException => "csv" // Par défaut en CSV si l'argument n'est pas fourni
  }

  // Ajout de l'argument pour le séparateur CSV (par défaut ",")
  val CSV_SEPARATOR: String = try {
    cliArgs(4) // Le séparateur est le 5ème argument
  } catch {
    case _: ArrayIndexOutOfBoundsException => "," // Par défaut la virgule
  }

  val TRANSFORMATIONS: String = try {
    cliArgs(5)
  } catch {
    case _: ArrayIndexOutOfBoundsException => ""
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
    .setClass("fs.file.impl",  classOf[BareLocalFileSystem], classOf[FileSystem])

  val reader: Reader = new ReaderImpl(sparkSession)
  val processor: Processor = new ProcessorImpl(TRANSFORMATIONS)
  val writer: Writer = new Writer()
  val src_path = SRC_PATH
  val dst_path = DST_PATH

  // Passer le séparateur CSV au lecteur
  val inputDF: DataFrame = reader.read(src_path, CSV_SEPARATOR)
  val processedDF: DataFrame = processor.process(inputDF)

  // Appel à la méthode write avec le format choisi
  writer.write(processedDF, format = OUTPUT_FORMAT, mode = "overwrite", path = dst_path)
}
