# Gym Management System - Architecture Microservices avec Kafka

## Vue d'ensemble
Application complète de gestion de salles de sport et de cours avec architecture microservices et communication asynchrone via Apache Kafka.

## Architecture

### Frontend
- **Angular 20** avec Material Design
- Interface responsive et moderne
- Services séparés pour chaque microservice
- **Localisation**: `frontend/`

### Microservices

#### 1. Gym Service (Port 8080)
- **Base de données**: MongoDB
- **Entités**: Salle de sport
- **Fonctionnalités**: CRUD complet, recherche par nom, adresse, capacité
- **Technologies**: Spring Boot, Spring Data MongoDB, MapStruct, Kafka Producer
- **Localisation**: `backend/gym-service/`

#### 2. Course Service (Port 8081)
- **Base de données**: MySQL
- **Entités**: Cours
- **Fonctionnalités**: CRUD complet, recherche par nom, instructeur, niveau, prix
- **Technologies**: Spring Boot, Spring Data JPA, MySQL, MapStruct, OpenFeign, Kafka Producer/Consumer
- **Localisation**: `backend/course-service/`

### Message Broker
- **Apache Kafka** pour la communication asynchrone
- **Topics**: `gym-events`, `course-events`
- **Kafka UI** pour le monitoring (Port 8090)

## Structure du Projet

```
.
├── frontend/                     # Application Angular
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   │   ├── gym/          # Composants Salle de Sport
│   │   │   │   └── course/       # Composants Cours
│   │   │   ├── models/           # Modèles TypeScript
│   │   │   └── services/         # Services Angular
│   │   └── global_styles.css
│   ├── angular.json
│   ├── package.json
│   └── tsconfig.json
├── backend/                      # Microservices Spring Boot
│   ├── gym-service/              # Microservice Salle de Sport
│   │   └── src/main/java/com/gym/
│   │       ├── controller/       # Controllers REST
│   │       ├── dto/              # Data Transfer Objects
│   │       ├── entity/           # Entités MongoDB
│   │       ├── event/            # Événements Kafka
│   │       ├── service/          # Services métier + Event Publisher
│   │       ├── mapper/           # MapStruct Mappers
│   │       ├── repository/       # Repositories MongoDB
│   │       └── config/           # Configuration Kafka
│   └── course-service/           # Microservice Cours
│       └── src/main/java/com/course/
│           ├── controller/       # Controllers REST
│           ├── dto/              # Data Transfer Objects
│           ├── entity/           # Entités JPA
│           ├── event/            # Événements Kafka
│           ├── service/          # Services métier + Event Publisher/Listener
│           ├── client/           # Clients OpenFeign
│           ├── mapper/           # MapStruct Mappers
│           ├── repository/       # Repositories JPA
│           └── config/           # Configuration Kafka + Feign
├── docker-compose.yml            # Infrastructure (Kafka, MongoDB, MySQL)
├── kafka-setup.sh               # Script de création des topics
└── README.md
```

## Communication entre Microservices

### 🔄 **Communication Synchrone (OpenFeign)**
- **Course Service → Gym Service**
- Validation de l'existence des salles lors de la création/modification de cours
- Enrichissement des données cours avec informations des salles
- Gestion des timeouts et retry automatique

### 📨 **Communication Asynchrone (Kafka)**

#### **Événements Gym Service**
- `GYM_CREATED`: Nouvelle salle créée
- `GYM_UPDATED`: Salle mise à jour
- `GYM_DELETED`: Salle supprimée
- `GYM_CAPACITY_CHANGED`: Capacité de la salle modifiée

#### **Événements Course Service**
- `COURSE_CREATED`: Nouveau cours créé
- `COURSE_UPDATED`: Cours mis à jour
- `COURSE_DELETED`: Cours supprimé
- `COURSE_ASSIGNED_TO_GYM`: Cours assigné à une salle
- `COURSE_UNASSIGNED_FROM_GYM`: Cours désassocié d'une salle

#### **Gestion des Événements Cross-Service**
- **Suppression de salle**: Désassociation automatique des cours
- **Changement de capacité**: Vérification de cohérence avec les cours
- **Audit et traçabilité**: Logs détaillés de tous les événements

## Fonctionnalités Avancées

### **Event-Driven Architecture**
- **Découplage**: Services indépendants communiquant via événements
- **Résilience**: Gestion des pannes et retry automatique
- **Scalabilité**: Traitement asynchrone des événements
- **Auditabilité**: Traçage complet des actions

### **Gestion des Données Cohérentes**
- **Saga Pattern**: Gestion des transactions distribuées
- **Compensation**: Actions correctives automatiques
- **Idempotence**: Prévention des doublons d'événements

### **Monitoring et Observabilité**
- **Kafka UI**: Interface de monitoring des topics et messages
- **Logs structurés**: Traçage détaillé des événements
- **Métriques**: Suivi des performances et erreurs

## Installation et Lancement

### Prérequis
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Maven

### 1. Démarrage de l'Infrastructure

```bash
# Démarrer Kafka, MongoDB, MySQL et Kafka UI
docker-compose up -d

# Créer les topics Kafka
./kafka-setup.sh

# Vérifier que tous les services sont démarrés
docker-compose ps
```

### 2. Lancement des Microservices

```bash
# Gym Service
cd backend/gym-service
mvn spring-boot:run
# API sur http://localhost:8080

# Course Service (dans un autre terminal)
cd backend/course-service
mvn spring-boot:run
# API sur http://localhost:8081
```

### 3. Frontend Angular

```bash
cd frontend
npm install
npm start
# Interface sur http://localhost:4200
```

### 4. Monitoring Kafka

- **Kafka UI**: http://localhost:8090
- Visualisation des topics, messages et consommateurs

## APIs REST

### Gym Service (localhost:8080)
- `GET /api/gyms` - Liste toutes les salles
- `POST /api/gyms` - Crée une nouvelle salle (+ événement Kafka)
- `PUT /api/gyms/{id}` - Met à jour une salle (+ événement Kafka)
- `DELETE /api/gyms/{id}` - Supprime une salle (+ événement Kafka)
- `GET /api/gyms/search/name?name=X` - Recherche par nom

### Course Service (localhost:8081)
- `GET /api/courses` - Liste tous les cours
- `POST /api/courses` - Crée un nouveau cours (+ événement Kafka)
- `PUT /api/courses/{id}` - Met à jour un cours (+ événement Kafka)
- `DELETE /api/courses/{id}` - Supprime un cours (+ événement Kafka)
- `GET /api/courses/with-gym-info` - Cours enrichis avec infos salles
- `GET /api/courses/search/gym?gymId=X` - Cours par salle
- `GET /api/courses/available-gyms` - Salles disponibles via OpenFeign

## Topics Kafka

### gym-events
```json
{
  "gymId": "string",
  "gymName": "string",
  "gymEmail": "string",
  "capacity": "integer",
  "eventType": "GYM_CREATED|GYM_UPDATED|GYM_DELETED|GYM_CAPACITY_CHANGED",
  "timestamp": "datetime",
  "description": "string"
}
```

### course-events
```json
{
  "courseId": "long",
  "courseName": "string",
  "instructor": "string",
  "gymId": "string",
  "gymName": "string",
  "maxParticipants": "integer",
  "price": "decimal",
  "level": "string",
  "eventType": "COURSE_CREATED|COURSE_UPDATED|COURSE_DELETED|COURSE_ASSIGNED_TO_GYM|COURSE_UNASSIGNED_FROM_GYM",
  "timestamp": "datetime",
  "description": "string"
}
```

## Scénarios de Test

### 1. Test de Communication Asynchrone
```bash
# 1. Créer une salle de sport
curl -X POST http://localhost:8080/api/gyms \
  -H "Content-Type: application/json" \
  -d '{"name":"Gym Test","address":"123 Rue Test","phone":"0123456789","email":"test@gym.com","capacity":50}'

# 2. Vérifier l'événement dans Kafka UI (http://localhost:8090)

# 3. Créer un cours associé à cette salle
curl -X POST http://localhost:8081/api/courses \
  -H "Content-Type: application/json" \
  -d '{"name":"Yoga","description":"Cours de yoga","instructor":"Marie","duration":60,"maxParticipants":20,"price":25.00,"schedule":"Lundi 18h","level":"Débutant","gymId":"SALLE_ID"}'

# 4. Supprimer la salle et observer la désassociation automatique des cours
curl -X DELETE http://localhost:8080/api/gyms/SALLE_ID
```

### 2. Test de Résilience
- Arrêter le Gym Service et tester le Course Service
- Vérifier la gestion des erreurs OpenFeign
- Redémarrer et observer la reprise des événements Kafka

## Technologies Utilisées

### Backend
- **Spring Boot 3.2.5**
- **Spring Data MongoDB & JPA**
- **Spring Cloud OpenFeign** (communication synchrone)
- **Spring Kafka** (communication asynchrone)
- **MapStruct 1.5.5** (mapping)
- **Bean Validation**
- **MySQL & MongoDB**

### Infrastructure
- **Apache Kafka 7.4.0** (message broker)
- **Zookeeper** (coordination Kafka)
- **Kafka UI** (monitoring)
- **Docker Compose** (orchestration)

### Frontend
- **Angular 20**
- **Angular Material**
- **RxJS**
- **TypeScript**

## Avantages de l'Architecture

### **Découplage**
- Services indépendants et autonomes
- Évolution séparée des microservices
- Résilience aux pannes

### **Scalabilité**
- Montée en charge indépendante
- Traitement asynchrone des événements
- Distribution des charges

### **Observabilité**
- Traçage complet des événements
- Monitoring en temps réel
- Audit et conformité

### **Flexibilité**
- Ajout facile de nouveaux services
- Intégration de systèmes externes
- Évolution de l'architecture

Cette architecture microservices avec Kafka offre une solution robuste, scalable et maintenable pour la gestion des salles de sport et des cours.