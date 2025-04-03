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
  val TRANSFORMATION_TYPE: String = try {
    cliArgs(3)
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      "count" // Transformation par défaut
    }
  }

  val INPUT_FORMAT: String = try {
    cliArgs(4)
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      // Détermine le format en fonction de l'extension du fichier
      getFileFormat(SRC_PATH)
    }
  }

  val OUTPUT_FORMAT: String = try {
    cliArgs(5)
  } catch {
    case e: java.lang.ArrayIndexOutOfBoundsException => {
      // Détermine le format en fonction de l'extension du fichier
      getFileFormat(DST_PATH)
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
    .setClass("fs.file.impl",  classOf[BareLocalFileSystem], classOf[FileSystem])


  val reader: Reader = new ReaderImpl(sparkSession)
  val processor: Processor = new ProcessorImpl()
  val writer: Writer = new Writer()
  val src_path = SRC_PATH
  val dst_path = DST_PATH
  val transformation_type = TRANSFORMATION_TYPE
  val input_format = INPUT_FORMAT
  val output_format = OUTPUT_FORMAT

  println(s"Lecture du fichier source: $src_path (format: $input_format)")
  val inputDF: DataFrame = reader.read(src_path, input_format)

  println(s"Application de la transformation: $transformation_type")
  val processedDF: DataFrame = processor.process(inputDF, transformation_type)

  println(s"Écriture des résultats dans: $dst_path (format: $output_format)")
  writer.write(processedDF, "overwrite", dst_path, output_format)

  // Fonction utilitaire pour déterminer le format basé sur l'extension du fichier
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