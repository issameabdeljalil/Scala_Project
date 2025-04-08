package fr.mosef.scala.template

import fr.mosef.scala.template.job.Job
import fr.mosef.scala.template.processor.Processor
import fr.mosef.scala.template.processor.impl.ProcessorImpl
import fr.mosef.scala.template.reader.Reader
import fr.mosef.scala.template.reader.impl.ReaderImpl
import fr.mosef.scala.template.writer.Writer
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.SparkConf
import com.globalmentor.apache.hadoop.fs.BareLocalFileSystem
import org.apache.hadoop.fs.FileSystem
import fr.mosef.scala.template.config.ConfigLoader

object Main extends App with Job {

  ConfigLoader.load()

  val cliArgs = args
  val MASTER_URL: String = if (cliArgs.length > 0) cliArgs(0) else "local[1]"
  override val src_path: String = if (cliArgs.length > 1) cliArgs(1) else {
    println("No input defined")
    sys.exit(1)
  }
  override val dst_path: String = if (cliArgs.length > 2) cliArgs(2) else "./default/output-writer"
  val CSV_SEPARATOR: String = if (cliArgs.length > 3) cliArgs(3) else ","
  val TRANSFORMATIONS: String = if (cliArgs.length > 4) cliArgs(4) else ""


  val OUTPUT_FORMAT: String = ConfigLoader.get("output.format", "csv")

  val conf = new SparkConf()
    .set("spark.driver.memory", "64M")
    .set("spark.testing.memory", "471859200")
    .set("spark.driver.host", "localhost")

  val sparkSession: SparkSession = SparkSession.builder
    .master(MASTER_URL)
    .config(conf)
    .appName("Scala Template")
    .enableHiveSupport()
    .getOrCreate()

  sparkSession.sparkContext.hadoopConfiguration
    .setClass("fs.file.impl", classOf[BareLocalFileSystem], classOf[FileSystem])

  override val reader: Reader = new ReaderImpl(sparkSession)
  override val processor: Processor = new ProcessorImpl(TRANSFORMATIONS)
  override val writer: Writer = new Writer()

  override val inputDF: DataFrame = reader.read(src_path, CSV_SEPARATOR)
  override val processedDF: DataFrame = processor.process(inputDF)

  writer.write(processedDF, format = OUTPUT_FORMAT, mode = "overwrite", path = dst_path)
}
