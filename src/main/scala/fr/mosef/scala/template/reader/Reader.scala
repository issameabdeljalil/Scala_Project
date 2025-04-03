package fr.mosef.scala.template.reader

import org.apache.spark.sql.DataFrame

trait Reader {

  def read(format: String, options: Map[String, String], path: String): DataFrame

  def read(path: String): DataFrame

  def read(path: String, format: String): DataFrame

  def read(path: String, format: String, delimiter: String, hasHeader: Boolean): DataFrame

  def read(): DataFrame

}