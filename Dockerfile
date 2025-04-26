FROM openjdk:11-jre-slim

WORKDIR /app

# Copier le JAR de l'application
COPY target/scala_template-2.0.9-jar-with-dependencies.jar /app/

# Exécuter directement l'application HDFSTest
CMD ["java", "-cp", "scala_template-2.0.9-jar-with-dependencies.jar", "fr.mosef.scala.template.HDFSTest", "hdfs://hdfs-simple.hadoop:9000"]