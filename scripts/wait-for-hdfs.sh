#!/bin/sh
set -e

echo "Waiting for HDFS to be ready..."
# Attendre un maximum de 60 secondes
sleep 60

echo "Continuing regardless of HDFS status..."
echo "Executing command: $@"
exec "$@"