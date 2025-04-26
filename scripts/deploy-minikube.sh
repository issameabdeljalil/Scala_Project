#!/bin/bash
set -e

echo "====== Vérification des prérequis ======"
# Vérifier si Docker est en cours d'exécution
if ! docker info > /dev/null 2>&1; then
  echo "Docker n'est pas en cours d'exécution. Démarrez Docker Desktop et réessayez."
  exit 1
fi

# Vérifier si Maven est installé
if ! command -v mvn > /dev/null; then
  echo "Maven n'est pas installé. Installez-le avec 'brew install maven'."
  exit 1
fi

echo "====== Démarrage de Minikube ======"
minikube start --driver=docker --memory=4096 --cpus=2
eval $(minikube docker-env)

echo "====== Création du namespace Hadoop ======"
kubectl apply -f k8s/hdfs-namespace.yaml
kubectl get namespaces

echo "====== Déploiement des composants HDFS ======"
kubectl apply -f k8s/simple-hdfs.yaml
echo "Attente du démarrage des pods HDFS..."
sleep 30
kubectl get pods -n hadoop

echo "====== Construction de l'application Scala ======"
mvn clean package

echo "====== Construction de l'image Docker ======"
docker build -t scala-hdfs-app:latest .

echo "====== Déploiement de l'application Scala ======"
kubectl apply -f k8s/scala-app-deployment.yaml
echo "Attente du démarrage de l'application..."
sleep 10
kubectl get pods -n hadoop

echo "====== Préparation des données de test ======"
kubectl apply -f k8s/hdfs-client-pod.yaml
echo "Attente du démarrage du client HDFS..."
kubectl wait --for=condition=Ready pod/hdfs-client -n hadoop
kubectl cp src/main/resources/test_file.csv hadoop/hdfs-client:/tmp/
kubectl exec -n hadoop hdfs-client -- sh -c "hadoop fs -mkdir -p /test"
kubectl exec -n hadoop hdfs-client -- sh -c "hadoop fs -put /tmp/test_file.csv /test/"

echo "====== Vérification des logs de l'application ======"
kubectl logs -l app=scala-app -n hadoop

echo "====== Interface web HDFS ======"
minikube service hdfs-simple -n hadoop