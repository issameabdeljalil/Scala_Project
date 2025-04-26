#!/bin/bash
set -e

echo "====== Suppression des ressources ======"
kubectl delete -f k8s/scala-app-deployment.yaml || true
kubectl delete -f k8s/hdfs-client-pod.yaml || true
kubectl delete -f k8s/hdfs-service.yaml || true
kubectl delete -f k8s/hdfs-deployment.yaml || true
kubectl delete -f k8s/hdfs-persistentvolume.yaml || true
kubectl delete -f k8s/hdfs-namespace.yaml || true

echo "====== Ressources supprimées ======"