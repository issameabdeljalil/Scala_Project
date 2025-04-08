package fr.mosef.scala.template.util

import java.io.{File, FileInputStream}
import java.util.Properties

object ConfigUtil {

  private val DEFAULT_CONFIG_PATH = "application.properties"

  def loadProperties(configPath: String = DEFAULT_CONFIG_PATH): Properties = {
    val properties = new Properties()
    try {
      val file = new File(configPath)
      if (file.exists()) {
        val propertiesStream = new FileInputStream(file)
        properties.load(propertiesStream)
        propertiesStream.close()
        println(s"Configuration chargée depuis : $configPath")
      } else {
        println(s"Fichier de configuration introuvable : $configPath, utilisation des valeurs par défaut")
      }
    } catch {
      case e: Exception =>
        println(s"Erreur lors du chargement du fichier de configuration : ${e.getMessage}")
        println("Utilisation des valeurs par défaut")
    }

    properties
  }

  def getProperty(properties: Properties, key: String, defaultValue: String): String = {
    Option(properties.getProperty(key)).getOrElse(defaultValue)
  }

  def loadPropertiesFromClasspath(fileName: String = "application.properties"): Properties = {
    val properties = new Properties()
    try {
      val stream = getClass.getClassLoader.getResourceAsStream(fileName)
      if (stream != null) {
        properties.load(stream)
        stream.close()
        println(s"Configuration chargée depuis le classpath : $fileName")
      } else {
        println(s"Fichier de configuration introuvable dans le classpath : $fileName")
      }
    } catch {
      case e: Exception =>
        println(s"Erreur lors du chargement depuis le classpath : ${e.getMessage}")
    }
    properties
  }

  def getPropertyAsBoolean(properties: Properties, key: String, defaultValue: Boolean): Boolean = {
    Option(properties.getProperty(key))
      .map(_.toLowerCase == "true")
      .getOrElse(defaultValue)
  }
}