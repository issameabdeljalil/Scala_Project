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

  def getPropertyAsBoolean(properties: Properties, key: String, defaultValue: Boolean): Boolean = {
    Option(properties.getProperty(key))
      .map(_.toLowerCase == "true")
      .getOrElse(defaultValue)
  }
}