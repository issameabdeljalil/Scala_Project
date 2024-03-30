import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
object Main extends App {
  println("Hello, World!")
  val sparkSession = SparkSession.builder.master("local[1]").enableHiveSupport().getOrCreate()
  print(sparkSession.sql("SELECT 'A'").show())
}