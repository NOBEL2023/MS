# Scénarios de Test - Communication Asynchrone Kafka

## Vue d'ensemble
Ce document présente les scénarios de test pour valider la communication asynchrone entre microservices via Apache Kafka, avec support des messages String et Objet.

## Architecture de Test

### Topics Kafka
- **gym-events** : Événements des salles (OBJET JSON)
- **gym-notifications** : Notifications des salles (STRING)
- **course-events** : Événements des cours (OBJET JSON)
- **course-notifications** : Notifications des cours (STRING)

### Services
- **Gym Service** (Port 8081) - Producer Kafka
- **Course Service** (Port 8082) - Producer et Consumer Kafka

## Types de Messages

### 1. Messages STRING
```bash
# Exemple de notification simple
Topic: gym-notifications
Key: "gym123"
Value: "Salle créée: Gym Central"
```

### 2. Messages OBJET (JSON)
```json
// Exemple d'événement GymEvent
{
  "gymId": "gym123",
  "gymName": "Gym Central",
  "gymEmail": "contact@gymcentral.com",
  "capacity": 100,
  "eventType": "GYM_CREATED",
  "timestamp": "2025-01-27T10:30:00.000",
  "description": "Nouvelle salle de sport créée"
}
```

## Prérequis

### 1. Démarrer l'Infrastructure
```bash
# Démarrer Kafka, MongoDB, MySQL
docker-compose up -d

# Créer les topics Kafka
./kafka-setup.sh

# Vérifier les topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

### 2. Démarrer les Services
```bash
# Gym Service
cd backend/gym-service
mvn spring-boot:run

# Course Service (dans un autre terminal)
cd backend/course-service
mvn spring-boot:run
```

## Scénarios de Test

### 1. Test des Messages STRING

#### 1.1 Envoi de Notification depuis Gym Service
```bash
# Envoyer une notification STRING
curl -X POST "http://localhost:8081/api/kafka-test/send-notification" \
  -d "gymId=test-gym-1" \
  -d "message=Test de notification STRING depuis Gym Service"

# Vérifier dans les logs du Course Service
# Rechercher: "Notification GYM reçue"
```

#### 1.2 Envoi de Message STRING Personnalisé
```bash
# Envoyer un message STRING vers un topic personnalisé
curl -X POST "http://localhost:8081/api/kafka-test/send-string" \
  -d "topic=gym-notifications" \
  -d "key=custom-key" \
  -d "message=Message STRING personnalisé"
```

### 2. Test des Messages OBJET

#### 2.1 Événement de Création de Salle
```bash
# Créer une salle (génère automatiquement des événements)
curl -X POST http://localhost:8081/gyms \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gym Test Kafka",
    "location": "Centre-ville",
    "phone": "0123456789",
    "email": "test@gym.com",
    "capacity": 50
  }'

# Observer dans les logs du Course Service:
# - Événement GYM_CREATED reçu
# - Notification STRING reçue
```

#### 2.2 Test Manuel d'Événement
```bash
# Envoyer un événement GYM_CREATED manuellement
curl -X POST "http://localhost:8081/api/kafka-test/send-gym-created" \
  -d "gymId=manual-test-gym" \
  -d "gymName=Gym Test Manuel" \
  -d "gymEmail=manuel@test.com" \
  -d "capacity=75"
```

### 3. Test de Changement de Capacité

#### 3.1 Modification de Salle
```bash
# Récupérer l'ID d'une salle existante
GYM_ID=$(curl -s http://localhost:8081/gyms | jq -r '.[0].id')

# Modifier la capacité de la salle
curl -X PUT "http://localhost:8081/gyms/$GYM_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gym Test Modifié",
    "location": "Centre-ville",
    "phone": "0123456789",
    "email": "test@gym.com",
    "capacity": 120
  }'

# Observer les événements:
# - GYM_UPDATED
# - GYM_CAPACITY_CHANGED
```

#### 3.2 Test Manuel de Changement de Capacité
```bash
# Simuler un changement de capacité
curl -X POST "http://localhost:8081/api/kafka-test/send-capacity-changed" \
  -d "gymId=test-gym" \
  -d "gymName=Gym Test" \
  -d "oldCapacity=50" \
  -d "newCapacity=100"
```

### 4. Test de Suppression et Désassociation

#### 4.1 Créer un Cours Associé à une Salle
```bash
# Créer une salle
GYM_RESPONSE=$(curl -s -X POST http://localhost:8081/gyms \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gym à Supprimer",
    "location": "Test Location",
    "capacity": 30
  }')

GYM_ID=$(echo $GYM_RESPONSE | jq -r '.id')

# Créer un cours associé à cette salle
curl -X POST http://localhost:8082/api/courses \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"Cours Test\",
    \"description\": \"Cours de test\",
    \"instructor\": \"Instructeur Test\",
    \"duration\": 60,
    \"maxParticipants\": 20,
    \"price\": 25.00,
    \"schedule\": \"Lundi 18h\",
    \"level\": \"Débutant\",
    \"gymId\": \"$GYM_ID\"
  }"
```

#### 4.2 Supprimer la Salle et Observer la Désassociation
```bash
# Supprimer la salle
curl -X DELETE "http://localhost:8081/gyms/$GYM_ID"

# Observer dans les logs du Course Service:
# - Événement GYM_DELETED reçu
# - Désassociation automatique des cours
# - Événements COURSE_UNASSIGNED_FROM_GYM générés
```

### 5. Test des Événements de Cours

#### 5.1 Création de Cours avec Événements
```bash
# Créer un cours (génère des événements automatiquement)
curl -X POST http://localhost:8082/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Yoga Avancé",
    "description": "Cours de yoga pour niveau avancé",
    "instructor": "Marie Dupont",
    "duration": 90,
    "maxParticipants": 15,
    "price": 35.00,
    "schedule": "Mercredi 19h",
    "level": "Avancé"
  }'

# Observer les événements générés:
# - COURSE_CREATED
# - Notification STRING
```

#### 5.2 Test Manuel d'Événement de Cours
```bash
# Envoyer un événement COURSE_CREATED manuellement
curl -X POST "http://localhost:8082/api/kafka-test/send-course-created" \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 999,
    "courseName": "Cours Test Manuel",
    "instructor": "Test Instructor",
    "gymId": "test-gym",
    "gymName": "Test Gym",
    "maxParticipants": 20,
    "price": 30.00,
    "level": "Intermédiaire"
  }'
```

### 6. Test de Monitoring Kafka

#### 6.1 Kafka UI
```bash
# Accéder à Kafka UI
open http://localhost:8090

# Vérifier:
# - Topics créés
# - Messages dans les topics
# - Consommateurs actifs
# - Partitions et offsets
```

#### 6.2 Commandes Kafka CLI
```bash
# Lister les topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Consommer les messages d'un topic
docker exec kafka kafka-console-consumer \
  --topic gym-events \
  --bootstrap-server localhost:9092 \
  --from-beginning

# Consommer les notifications STRING
docker exec kafka kafka-console-consumer \
  --topic gym-notifications \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

### 7. Test de Résilience

#### 7.1 Arrêt du Consumer
```bash
# Arrêter le Course Service
# Ctrl+C dans le terminal du course-service

# Continuer à envoyer des messages depuis Gym Service
curl -X POST "http://localhost:8081/api/kafka-test/send-notification" \
  -d "gymId=test" \
  -d "message=Message pendant arrêt du consumer"

# Redémarrer le Course Service
cd backend/course-service && mvn spring-boot:run

# Observer la reprise des messages en attente
```

#### 7.2 Test de Panne Kafka
```bash
# Arrêter Kafka
docker-compose stop kafka

# Tenter d'envoyer des messages (échec attendu)
curl -X POST "http://localhost:8081/api/kafka-test/send-notification" \
  -d "gymId=test" \
  -d "message=Test pendant panne Kafka"

# Redémarrer Kafka
docker-compose start kafka

# Vérifier la reconnexion automatique
```

## Validation des Résultats

### 1. Logs à Surveiller

#### Gym Service
```
INFO  - Envoi événement GYM: GYM_CREATED pour salle gym123
INFO  - Événement GYM envoyé avec succès: offset=5, partition=0
INFO  - Envoi notification pour salle gym123: Salle créée: Gym Test
```

#### Course Service
```
INFO  - Événement GYM reçu - Topic: gym-events, Key: gym123, Event: GymEvent{...}
INFO  - Notification GYM reçue - Topic: gym-notifications, Key: gym123, Message: Salle créée: Gym Test
INFO  - Traitement événement GYM_CREATED pour salle gym123
INFO  - Désassociation de 2 cours de la salle supprimée
```

### 2. Vérifications dans Kafka UI

#### Topics
- **gym-events** : Messages JSON avec événements structurés
- **gym-notifications** : Messages STRING simples
- **course-events** : Messages JSON des événements de cours
- **course-notifications** : Messages STRING des notifications de cours

#### Métriques
- Nombre de messages produits/consommés
- Lag des consommateurs
- Partitioning des messages
- Offsets et timestamps

### 3. Vérifications Métier

#### Base de Données
```bash
# Vérifier la désassociation des cours après suppression de salle
curl http://localhost:8082/api/courses | jq '.[] | select(.gymId == null)'

# Vérifier les cours enrichis avec infos salles
curl http://localhost:8082/api/courses/with-gym-info
```

## Endpoints de Test

### Gym Service (8081)
```bash
# Informations sur les topics
GET /api/kafka-test/topics-info

# Tests manuels
POST /api/kafka-test/send-string
POST /api/kafka-test/send-notification
POST /api/kafka-test/send-gym-created
POST /api/kafka-test/send-capacity-changed
POST /api/kafka-test/send-gym-deleted
```

### Course Service (8082)
```bash
# Informations sur les topics
GET /api/kafka-test/topics-info

# Tests manuels
POST /api/kafka-test/send-string
POST /api/kafka-test/send-notification
POST /api/kafka-test/send-course-created
POST /api/kafka-test/send-course-assigned
```

## Bonnes Pratiques Observées

### 1. Sérialisation
- **STRING** : Messages simples, notifications, logs
- **JSON** : Événements structurés, données métier

### 2. Partitioning
- Utilisation de l'ID de l'entité comme clé
- Distribution équilibrée des messages

### 3. Gestion des Erreurs
- Acquittement manuel des messages
- Retry automatique en cas d'échec
- Logs détaillés pour le debugging

### 4. Monitoring
- Métriques de production/consommation
- Surveillance du lag des consommateurs
- Alertes sur les échecs de traitement

Cette implémentation démontre une communication asynchrone robuste entre microservices, avec support complet des messages String et Objet via Apache Kafka.