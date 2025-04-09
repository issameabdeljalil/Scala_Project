package fr.mosef.scala.template.processor.impl

import fr.mosef.scala.template.processor.Processor
import org.apache.spark.sql.{DataFrame, functions => F}

class ProcessorImpl(transformations: String) extends Processor {

  override def process(inputDF: DataFrame): DataFrame = {
    transformations.toLowerCase match {
      case "sum" =>
        inputDF.agg(F.sum("prix").alias("somme_prix"))

      case "groupby" =>
        inputDF.groupBy("group_key").agg(F.sum("prix").alias("somme_prix"))

      case "sort" =>
        inputDF.orderBy(F.asc("prix"))

      case "groupby_avg" =>
        inputDF.groupBy("group_key").agg(F.avg("prix").alias("moyenne_prix"))

      case other =>
        println(s"Transformation inconnue: '$other'. Aucune modification appliquée.")
        inputDF
    }
  }
}
