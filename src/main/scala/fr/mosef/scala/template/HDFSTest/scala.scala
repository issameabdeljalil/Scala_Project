// src/main/scala/fr/mosef/scala/template/HDFSTest.scala
package fr.mosef.scala.template

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

object HDFSTest {
  def main(args: Array[String]): Unit = {
    val hdfsUrl = if (args.length > 0) args(0) else "hdfs://hdfs-simple.hadoop:9000"
    println(s"Connexion à HDFS: $hdfsUrl")

    val conf = new Configuration()
    conf.set("fs.defaultFS", hdfsUrl)

    try {
      val fs = FileSystem.get(conf)
      println("Connexion réussie!")

      // Lister les fichiers
      println("Contenu de HDFS /:")
      val status = fs.listStatus(new Path("/"))
      for (s <- status) {
        println(s.getPath.toString)
      }

      // Créer un répertoire test
      val testDir = new Path("/test-deploy")
      if (!fs.exists(testDir)) {
        fs.mkdirs(testDir)
        println(s"Répertoire $testDir créé")
      } else {
        println(s"Répertoire $testDir existe déjà")
      }

      fs.close()
      println("Opération terminée avec succès")
    } catch {
      case e: Exception =>
        println(s"Erreur: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}