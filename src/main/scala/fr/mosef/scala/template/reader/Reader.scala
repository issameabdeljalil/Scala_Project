package fr.mosef.scala.template.reader

import org.apache.spark.sql.DataFrame

trait Reader {

  def read(format: String, options: Map[String, String], path: String): DataFrame

  def read(path: String, csvSeparator: String = ","): DataFrame  // Ajout du paramètre csvSeparator

  def read(): DataFrame

}
