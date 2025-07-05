#!/bin/bash

# Script pour créer les topics Kafka nécessaires

echo "Création des topics Kafka..."

# Attendre que Kafka soit prêt
sleep 10

# Créer le topic pour les événements des salles de sport
docker exec kafka kafka-topics --create \
  --topic gym-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# Créer le topic pour les événements des cours
docker exec kafka kafka-topics --create \
  --topic course-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# Lister les topics créés
echo "Topics créés:"
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

echo "Configuration Kafka terminée!"