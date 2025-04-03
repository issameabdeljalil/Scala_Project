package fr.mosef.scala.template.processor.impl

import fr.mosef.scala.template.processor.Processor
import org.apache.spark.sql.{DataFrame, functions => F}
import org.apache.spark.sql.expressions.Window

class ProcessorImpl() extends Processor {

  def process(inputDF: DataFrame, transformationType: String = "count"): DataFrame = {
    // Vérifie si le DataFrame est vide
    if (inputDF.isEmpty) {
      return inputDF
    }

    // Applique la transformation en fonction du type spécifié
    transformationType.toLowerCase match {
      case "count" =>
        inputDF.groupBy("group_key").count()

      case "sum" =>
        // Vérifie si la colonne field1 existe
        if (inputDF.columns.contains("field1")) {
          inputDF.groupBy("group_key").sum("field1")
        } else {
          throw new IllegalArgumentException("La colonne 'field1' n'existe pas dans le DataFrame")
        }

      case "avg" =>
        // Vérifie si la colonne field1 existe
        if (inputDF.columns.contains("field1")) {
          inputDF.groupBy("group_key").avg("field1")
        } else {
          throw new IllegalArgumentException("La colonne 'field1' n'existe pas dans le DataFrame")
        }

      case "distribution" =>
        // Distribution des valeurs en pourcentage par groupe
        if (inputDF.columns.contains("field1")) {
          val windowSpec = Window.partitionBy("group_key")
          inputDF
            .withColumn("total", F.count("*").over(windowSpec))
            .withColumn("percentage", F.round(F.count("*").over(windowSpec.partitionBy("group_key", "field1")) / F.col("total") * 100, 2))
            .select("group_key", "field1", "percentage")
            .distinct()
        } else {
          throw new IllegalArgumentException("La colonne 'field1' n'existe pas dans le DataFrame")
        }

      case _ =>
        throw new IllegalArgumentException(s"Type de transformation non supporté: $transformationType")
    }
  }

  // Version surchargée pour prendre en compte d'autres paramètres si nécessaire
  def process(inputDF: DataFrame): DataFrame = {
    process(inputDF, "count")
  }
}