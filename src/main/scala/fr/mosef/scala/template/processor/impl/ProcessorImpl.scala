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
        // Vérifie si la colonne price existe
        if (inputDF.columns.contains("price")) {
          // Convertir explicitement price en double pour s'assurer que sum fonctionne correctement
          inputDF.withColumn("price", F.col("price").cast(DoubleType))
            .groupBy("group_key")
            .sum("price")
        } else {
          throw new IllegalArgumentException("La colonne 'price' n'existe pas dans le DataFrame")
        }

      case "avg" =>
        // Vérifie si la colonne price existe
        if (inputDF.columns.contains("price")) {
          inputDF.withColumn("price", F.col("price").cast(DoubleType))
            .groupBy("group_key")
            .avg("price")
        } else {
          throw new IllegalArgumentException("La colonne 'price' n'existe pas dans le DataFrame")
        }

      case "median" =>
        // Vérifie si la colonne price existe
        if (inputDF.columns.contains("price")) {
          // Convertir price en double
          val dfWithDouble = inputDF.withColumn("price", F.col("price").cast(DoubleType))

          // Calcul de la médiane par groupe
          val windowSpec = Window.partitionBy("group_key").orderBy("price")
          val dfWithRowNumber = dfWithDouble.withColumn("row_number", F.row_number().over(windowSpec))
          val dfWithCount = dfWithDouble.groupBy("group_key").count()

          // Joindre pour obtenir le nombre d'éléments par groupe
          val dfWithMedianIndex = dfWithRowNumber.join(dfWithCount, "group_key")
            .withColumn("median_index", F.ceil(F.col("count") / 2))

          // Filtrer pour garder uniquement les valeurs médianes
          val medianDF = dfWithMedianIndex
            .filter(F.col("row_number") === F.col("median_index"))
            .select("group_key", "price")
            .withColumnRenamed("price", "median")

          medianDF
        } else {
          throw new IllegalArgumentException("La colonne 'price' n'existe pas dans le DataFrame")
        }

      case "sort" =>
        if (inputDF.columns.contains("price")) {
          inputDF
            .withColumn("price", F.col("price").cast(DoubleType))
            .orderBy(F.col("group_key"), F.col("price"))
        } else {
          throw new IllegalArgumentException("La colonne 'price' n'existe pas dans le DataFrame")
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