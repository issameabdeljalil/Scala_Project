package fr.mosef.scala.template.config

import java.util.Properties

object ConfigLoader {
  private val properties = new Properties()

  def load(): Unit = {
    val stream = getClass.getClassLoader.getResourceAsStream("application.properties")
    if (stream != null) {
      properties.load(stream)
      stream.close()
    }
  }

  def get(key: String, default: String = ""): String = {
    properties.getProperty(key, default)
  }
}
