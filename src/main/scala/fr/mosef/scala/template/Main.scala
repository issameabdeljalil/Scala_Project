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
import java.nio.file.{Files, StandardCopyOption, Paths}

object Main extends App with Job {

  def getFileFormat(path: String): String = {
    val lowerPath = path.toLowerCase
    if (lowerPath.endsWith(".csv")) "csv"
    else if (lowerPath.endsWith(".parquet")) "parquet"
    else "csv"
  }

  def extractSeparator(sepString: String): String = {
    if (sepString != null && sepString.startsWith("sep")) sepString.substring(3) else sepString
  }

  // Charger les propriétés
  val properties = ConfigUtil.loadPropertiesFromClasspath("application.properties")

  // Lire tous les paramètres depuis le fichier properties
  val rawSrcPath = ConfigUtil.getProperty(properties, "app.input.path", "src/main/resources/test_file.csv")
  override val src_path: String = {
    if (rawSrcPath.startsWith("classpath:")) {
      val resourceName = rawSrcPath.stripPrefix("classpath:")
      val stream = getClass.getClassLoader.getResourceAsStream(resourceName)
      if (stream == null) {
        println(s"Ressource introuvable dans le classpath : $resourceName")
        sys.exit(1)
      } else {
        val tempFile = Files.createTempFile("scala_input_", "_" + resourceName)
        Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING)
        stream.close()
        println(s"Fichier temporaire créé depuis le classpath : ${tempFile.toAbsolutePath}")
        tempFile.toAbsolutePath.toString
      }
    } else {
      rawSrcPath
    }
  }

  override val dst_path: String = ConfigUtil.getProperty(properties, "app.output.path", "./default/output-writer")
  val MASTER_URL: String = ConfigUtil.getProperty(properties, "spark.master", "local[1]")
  val TRANSFORMATION_TYPE: String = ConfigUtil.getProperty(properties, "app.transformation", "count")
  val INPUT_FORMAT: String = ConfigUtil.getProperty(properties, "app.input.format", getFileFormat(src_path))
  val OUTPUT_FORMAT: String = ConfigUtil.getProperty(properties, "app.output.format", getFileFormat(dst_path))
  val CSV_DELIMITER: String = extractSeparator(ConfigUtil.getProperty(properties, "app.input.delimiter", ","))
  val CSV_OUTPUT_DELIMITER: String = extractSeparator(ConfigUtil.getProperty(properties, "app.output.delimiter", CSV_DELIMITER))
  val CSV_HEADER: Boolean = ConfigUtil.getPropertyAsBoolean(properties, "app.input.header", true)
  val CSV_OUTPUT_HEADER: Boolean = ConfigUtil.getPropertyAsBoolean(properties, "app.output.header", true)

  // Vérifie que le fichier source existe
  if (!new java.io.File(src_path).exists()) {
    println(s"Le fichier spécifié n'existe pas : $src_path")
    sys.exit(1)
  }

  // Configuration Spark
  val conf = new SparkConf()
  conf.set("spark.driver.memory", ConfigUtil.getProperty(properties, "spark.driver.memory", "64M"))
  conf.set("spark.testing.memory", ConfigUtil.getProperty(properties, "spark.testing.memory", "471859200"))
  conf.set("spark.driver.host", "localhost")

  val sparkSession = SparkSession
    .builder()
    .master(MASTER_URL)
    .config(conf)
    .appName("Scala Template")
    .enableHiveSupport()
    .getOrCreate()

  sparkSession.sparkContext.hadoopConfiguration
    .setClass("fs.file.impl", classOf[BareLocalFileSystem], classOf[FileSystem])

  override val reader: Reader = new ReaderImpl(sparkSession)
  override val processor: Processor = new ProcessorImpl()
  override val writer: Writer = new Writer()

  println(s"Lecture du fichier source : $src_path (format: $INPUT_FORMAT)")
  override val inputDF: DataFrame = if (INPUT_FORMAT == "csv") {
    reader.read(src_path, INPUT_FORMAT, CSV_DELIMITER, CSV_HEADER)
  } else {
    reader.read(src_path, INPUT_FORMAT)
  }

  println(s"🔧 Application de la transformation : $TRANSFORMATION_TYPE")
  override val processedDF: DataFrame = processor.process(inputDF, TRANSFORMATION_TYPE)

  println(s"Écriture dans : $dst_path (format: $OUTPUT_FORMAT)")
  if (OUTPUT_FORMAT == "csv") {
    writer.write(processedDF, "overwrite", dst_path, OUTPUT_FORMAT, CSV_OUTPUT_DELIMITER, CSV_OUTPUT_HEADER)
  } else {
    writer.write(processedDF, "overwrite", dst_path, OUTPUT_FORMAT)
  }

  println("✅ Traitement terminé avec succès !")
}
