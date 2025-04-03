package fr.mosef.scala.template.processor.impl

import fr.mosef.scala.template.processor.Processor
import org.apache.spark.sql.{DataFrame, functions => F}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.types.DoubleType

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
          // Convertir explicitement field1 en double pour s'assurer que sum fonctionne correctement
          inputDF.withColumn("field1", F.col("field1").cast(DoubleType))
            .groupBy("group_key")
            .sum("field1")
        } else {
          throw new IllegalArgumentException("La colonne 'field1' n'existe pas dans le DataFrame")
        }

      case "avg" =>
        // Vérifie si la colonne field1 existe
        if (inputDF.columns.contains("field1")) {
          inputDF.withColumn("field1", F.col("field1").cast(DoubleType))
            .groupBy("group_key")
            .avg("field1")
        } else {
          throw new IllegalArgumentException("La colonne 'field1' n'existe pas dans le DataFrame")
        }

      case "median" =>
        // Vérifie si la colonne field1 existe
        if (inputDF.columns.contains("field1")) {
          // Convertir field1 en double
          val dfWithDouble = inputDF.withColumn("field1", F.col("field1").cast(DoubleType))

          // Calcul de la médiane par groupe
          val windowSpec = Window.partitionBy("group_key").orderBy("field1")
          val dfWithRowNumber = dfWithDouble.withColumn("row_number", F.row_number().over(windowSpec))
          val dfWithCount = dfWithDouble.groupBy("group_key").count()

          // Joindre pour obtenir le nombre d'éléments par groupe
          val dfWithMedianIndex = dfWithRowNumber.join(dfWithCount, "group_key")
            .withColumn("median_index", F.ceil(F.col("count") / 2))

          // Filtrer pour garder uniquement les valeurs médianes
          val medianDF = dfWithMedianIndex
            .filter(F.col("row_number") === F.col("median_index"))
            .select("group_key", "field1")
            .withColumnRenamed("field1", "median")

          medianDF
        } else {
          throw new IllegalArgumentException("La colonne 'field1' n'existe pas dans le DataFrame")
        }

      case "distribution" =>
        // Distribution des valeurs en pourcentage par groupe
        if (inputDF.columns.contains("field1")) {
          val windowSpec = Window.partitionBy("group_key")
          inputDF
            .withColumn("total", F.count("*").over(windowSpec))
            .withColumn("percentage",
              F.round(F.count("*").over(windowSpec.partitionBy("group_key", "field1")) / F.col("total") * 100, 2))
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