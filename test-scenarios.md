# Scénarios de Test - Communication OpenFeign avec et sans Circuit Breaker

## Vue d'ensemble
Ce document présente les scénarios de test pour valider la communication synchrone entre microservices avec OpenFeign, en comparant les comportements avec et sans Circuit Breaker.

## Architecture de Test

### Services
- **Course Service** (Port 8082) - Client Feign
- **Gym Service** (Port 8081) - Service Provider

### Endpoints de Test

#### Course Service
- `GET /api/courses/test/gyms-without-cb` - Test SANS Circuit Breaker
- `GET /api/courses/test/gyms-with-cb` - Test AVEC Circuit Breaker
- `GET /api/courses/circuit-breaker/state` - État du Circuit Breaker
- `GET /api/courses/circuit-breaker/metrics` - Métriques du Circuit Breaker

#### Gym Service
- `GET /gyms` - Endpoint normal
- `GET /gyms/slow` - Simulation de latence (15s)
- `GET /gyms/error` - Simulation d'erreur

## Scénarios de Test

### 1. Test de Fonctionnement Normal

#### Objectif
Vérifier que les deux approches fonctionnent correctement quand le service Gym est disponible.

#### Étapes
```bash
# 1. Démarrer les deux services
cd backend/gym-service && mvn spring-boot:run
cd backend/course-service && mvn spring-boot:run

# 2. Créer quelques salles de sport
curl -X POST http://localhost:8081/gyms \
  -H "Content-Type: application/json" \
  -d '{"name":"Gym Central","location":"Centre-ville","capacity":100}'

# 3. Tester sans Circuit Breaker
curl http://localhost:8082/api/courses/test/gyms-without-cb

# 4. Tester avec Circuit Breaker
curl http://localhost:8082/api/courses/test/gyms-with-cb

# 5. Vérifier l'état du Circuit Breaker
curl http://localhost:8082/api/courses/circuit-breaker/state
```

#### Résultat Attendu
- Les deux endpoints retournent la même liste de salles
- Circuit Breaker en état `CLOSED`
- Logs montrant les appels réussis

### 2. Test de Gestion des Timeouts

#### Objectif
Comparer le comportement quand le service Gym répond lentement.

#### Étapes
```bash
# 1. Tester sans Circuit Breaker (timeout après 10s)
curl http://localhost:8082/api/courses/test/gyms-without-cb

# 2. Pendant que l'appel précédent est en cours, tester l'endpoint lent
curl http://localhost:8081/gyms/slow

# 3. Tester avec Circuit Breaker
curl http://localhost:8082/api/courses/test/gyms-with-cb

# 4. Vérifier l'état du Circuit Breaker
curl http://localhost:8082/api/courses/circuit-breaker/state
```

#### Résultat Attendu
- Sans CB : Exception après timeout (10s)
- Avec CB : Fallback activé, réponse immédiate
- Circuit Breaker peut passer en état `OPEN`

### 3. Test de Gestion des Erreurs

#### Objectif
Vérifier le comportement quand le service Gym retourne des erreurs.

#### Étapes
```bash
# 1. Générer plusieurs erreurs pour déclencher le Circuit Breaker
for i in {1..6}; do
  echo "Appel $i:"
  curl http://localhost:8081/gyms/error
  echo ""
done

# 2. Tester sans Circuit Breaker
curl http://localhost:8082/api/courses/test/gyms-without-cb

# 3. Tester avec Circuit Breaker
curl http://localhost:8082/api/courses/test/gyms-with-cb

# 4. Vérifier les métriques
curl http://localhost:8082/api/courses/circuit-breaker/metrics
```

#### Résultat Attendu
- Sans CB : Propagation de l'erreur
- Avec CB : Fallback activé, liste vide retournée
- Circuit Breaker en état `OPEN`

### 4. Test de Récupération (Half-Open)

#### Objectif
Vérifier la récupération automatique du Circuit Breaker.

#### Étapes
```bash
# 1. Déclencher l'ouverture du Circuit Breaker (voir test 3)

# 2. Attendre 30 secondes (wait-duration-in-open-state)

# 3. Faire un appel avec Circuit Breaker
curl http://localhost:8082/api/courses/test/gyms-with-cb

# 4. Vérifier l'état (devrait être HALF_OPEN)
curl http://localhost:8082/api/courses/circuit-breaker/state

# 5. Faire plusieurs appels réussis
for i in {1..3}; do
  curl http://localhost:8082/api/courses/test/gyms-with-cb
done

# 6. Vérifier l'état final (devrait être CLOSED)
curl http://localhost:8082/api/courses/circuit-breaker/state
```

#### Résultat Attendu
- Transition `OPEN` → `HALF_OPEN` → `CLOSED`
- Récupération automatique du service

### 5. Test de Service Indisponible

#### Objectif
Tester le comportement quand le service Gym est complètement arrêté.

#### Étapes
```bash
# 1. Arrêter le service Gym
# Ctrl+C dans le terminal du gym-service

# 2. Tester sans Circuit Breaker
curl http://localhost:8082/api/courses/test/gyms-without-cb

# 3. Tester avec Circuit Breaker
curl http://localhost:8082/api/courses/test/gyms-with-cb

# 4. Redémarrer le service Gym
cd backend/gym-service && mvn spring-boot:run

# 5. Tester la récupération
curl http://localhost:8082/api/courses/test/gyms-with-cb
```

#### Résultat Attendu
- Sans CB : Connection refused exception
- Avec CB : Fallback activé, données par défaut
- Récupération automatique après redémarrage

### 6. Test d'Intégration Complète

#### Objectif
Tester les fonctionnalités métier utilisant OpenFeign.

#### Étapes
```bash
# 1. Créer une salle
GYM_ID=$(curl -X POST http://localhost:8081/gyms \
  -H "Content-Type: application/json" \
  -d '{"name":"Gym Test","location":"Test Location","capacity":50}' \
  | jq -r '.id')

# 2. Créer un cours associé à cette salle
curl -X POST http://localhost:8082/api/courses \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Yoga\",\"description\":\"Cours de yoga\",\"instructor\":\"Marie\",\"duration\":60,\"maxParticipants\":20,\"price\":25.00,\"schedule\":\"Lundi 18h\",\"level\":\"Débutant\",\"gymId\":\"$GYM_ID\"}"

# 3. Récupérer les cours enrichis avec infos des salles
curl http://localhost:8082/api/courses/with-gym-info

# 4. Récupérer les salles disponibles
curl http://localhost:8082/api/courses/available-gyms
```

#### Résultat Attendu
- Validation de la salle lors de la création du cours
- Enrichissement des données cours avec infos salle
- Gestion gracieuse des erreurs

## Configuration du Circuit Breaker

### Paramètres Actuels
```properties
failure-rate-threshold=50%          # Seuil d'ouverture
wait-duration-in-open-state=30s     # Durée avant half-open
sliding-window-size=10              # Fenêtre d'analyse
minimum-number-of-calls=5           # Appels minimum avant calcul
permitted-calls-in-half-open=3      # Appels test en half-open
```

### Monitoring
- **Actuator**: http://localhost:8082/actuator/circuitbreakers
- **Health**: http://localhost:8082/actuator/health
- **Métriques**: http://localhost:8082/api/courses/circuit-breaker/metrics

## Logs à Surveiller

### Course Service
```
INFO  - Récupération de toutes les salles SANS Circuit Breaker
INFO  - Récupération de toutes les salles AVEC Circuit Breaker
INFO  - État du Circuit Breaker: CLOSED/OPEN/HALF_OPEN
WARN  - Fallback activé pour getAllGyms() - Service Gym indisponible
ERROR - Erreur lors de la récupération des salles sans CB
```

### Gym Service
```
INFO  - Traitement de la requête GET /gyms
WARN  - Simulation de latence activée
ERROR - Erreur simulée pour test Circuit Breaker
```

## Avantages Observés

### Sans Circuit Breaker
- ✅ Simple à implémenter
- ✅ Propagation directe des erreurs
- ❌ Pas de protection contre les pannes en cascade
- ❌ Timeout fixe, pas d'adaptation

### Avec Circuit Breaker
- ✅ Protection contre les pannes en cascade
- ✅ Récupération automatique
- ✅ Fallback configurables
- ✅ Métriques et monitoring
- ✅ Adaptation dynamique aux pannes
- ❌ Complexité supplémentaire
- ❌ Configuration à ajuster selon le contexte

## Conclusion

Le Circuit Breaker avec Resilience4j apporte une résilience significative à l'architecture microservices, permettant une dégradation gracieuse des services et une récupération automatique. Il est particulièrement recommandé pour les environnements de production où la disponibilité est critique.