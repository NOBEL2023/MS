# Communication Synchrone avec OpenFeign - Guide Complet

## Vue d'ensemble

Cette implémentation démontre la communication synchrone entre microservices avec OpenFeign, en proposant deux approches :

1. **Sans Circuit Breaker** - Communication directe avec gestion basique des erreurs
2. **Avec Circuit Breaker** - Communication résiliente avec Resilience4j

## Architecture

```
┌─────────────────┐    OpenFeign     ┌─────────────────┐
│  Course Service │ ──────────────► │   Gym Service   │
│   (Port 8082)   │                 │   (Port 8081)   │
│                 │                 │                 │
│ ┌─────────────┐ │                 │ ┌─────────────┐ │
│ │ Sans CB     │ │                 │ │   MongoDB   │ │
│ │ Avec CB     │ │                 │ │             │ │
│ │ Fallback    │ │                 │ └─────────────┘ │
│ └─────────────┘ │                 │                 │
│                 │                 │                 │
│ ┌─────────────┐ │                 │                 │
│ │   MySQL     │ │                 │                 │
│ └─────────────┘ │                 │                 │
└─────────────────┘                 └─────────────────┘
```

## Composants Implémentés

### 1. Clients Feign

#### GymClient (Sans Circuit Breaker)
```java
@FeignClient(
    name = "gym-service",
    url = "${gym-service.url:http://localhost:8081}",
    configuration = GymClientConfiguration.class
)
public interface GymClient {
    @GetMapping("/gyms")
    List<GymDTO> getAllGyms();
    
    @GetMapping("/gyms/{id}")
    GymDTO getGymById(@PathVariable("id") String id);
}
```

#### GymClientWithCircuitBreaker (Avec Circuit Breaker)
```java
@FeignClient(
    name = "gym-service-with-cb",
    url = "${gym-service.url:http://localhost:8081}",
    configuration = GymClientConfiguration.class,
    fallback = GymClientFallback.class
)
public interface GymClientWithCircuitBreaker {
    // Mêmes méthodes avec fallback automatique
}
```

### 2. Configuration Feign

#### Timeouts et Retry
```java
@Configuration
public class GymClientConfiguration {
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(5000, 10000); // 5s connect, 10s read
    }
    
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 3000, 3); // 3 tentatives
    }
}
```

### 3. Circuit Breaker avec Resilience4j

#### Configuration
```properties
resilience4j.circuitbreaker.instances.gym-service.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.gym-service.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.gym-service.sliding-window-size=10
resilience4j.circuitbreaker.instances.gym-service.minimum-number-of-calls=5
```

#### États du Circuit Breaker
- **CLOSED** : Fonctionnement normal
- **OPEN** : Service indisponible, fallback activé
- **HALF_OPEN** : Test de récupération en cours

### 4. Fallback Strategy

```java
@Component
public class GymClientFallback implements GymClientWithCircuitBreaker {
    @Override
    public List<GymDTO> getAllGyms() {
        logger.warn("Fallback activé - Service Gym indisponible");
        return new ArrayList<>(); // Liste vide
    }
    
    @Override
    public GymDTO getGymById(String id) {
        logger.warn("Fallback activé pour getGymById({})", id);
        return createDefaultGym(id); // Salle par défaut
    }
}
```

## Service d'Intégration

### GymIntegrationService

Ce service centralise la communication avec le microservice Gym et propose les deux approches :

```java
@Service
public class GymIntegrationService {
    
    // Communication SANS Circuit Breaker
    public List<GymDTO> getAllGymsWithoutCircuitBreaker() {
        try {
            return gymClient.getAllGyms();
        } catch (Exception e) {
            throw new RuntimeException("Service Gym indisponible", e);
        }
    }
    
    // Communication AVEC Circuit Breaker
    public List<GymDTO> getAllGymsWithCircuitBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("gym-service");
        Supplier<List<GymDTO>> decoratedSupplier = 
            CircuitBreaker.decorateSupplier(cb, gymClientWithCircuitBreaker::getAllGyms);
        return decoratedSupplier.get();
    }
}
```

## Fonctionnalités Métier

### 1. Validation des Salles
Lors de la création/modification d'un cours, validation que la salle existe :

```java
public CourseDTO create(CourseDTO courseDTO) {
    if (courseDTO.getGymId() != null) {
        if (!gymIntegrationService.validateGymExists(courseDTO.getGymId())) {
            throw new RuntimeException("La salle spécifiée n'existe pas");
        }
    }
    // ... création du cours
}
```

### 2. Enrichissement des Données
Récupération des cours avec informations complètes des salles :

```java
public List<CourseWithGymDTO> getAllCoursesWithGymInfo() {
    return repository.findAll().stream()
        .map(this::enrichCourseWithGymInfo)
        .collect(Collectors.toList());
}
```

### 3. Gestion des Pannes
En cas d'indisponibilité du service Gym :
- **Sans CB** : Exception propagée, service indisponible
- **Avec CB** : Fallback activé, service dégradé mais fonctionnel

## Endpoints de Test

### Course Service (8082)

#### Tests de Communication
```bash
# Test sans Circuit Breaker
GET /api/courses/test/gyms-without-cb

# Test avec Circuit Breaker  
GET /api/courses/test/gyms-with-cb

# État du Circuit Breaker
GET /api/courses/circuit-breaker/state

# Métriques du Circuit Breaker
GET /api/courses/circuit-breaker/metrics
```

#### Fonctionnalités Métier
```bash
# Cours enrichis avec infos salles
GET /api/courses/with-gym-info

# Cours par salle
GET /api/courses/by-gym/{gymId}

# Salles disponibles
GET /api/courses/available-gyms
```

### Gym Service (8081)

#### Endpoints Normaux
```bash
GET /gyms           # Liste des salles
GET /gyms/{id}      # Salle par ID
POST /gyms          # Créer une salle
PUT /gyms/{id}      # Modifier une salle
DELETE /gyms/{id}   # Supprimer une salle
```

#### Endpoints de Test
```bash
GET /gyms/slow      # Simulation de latence (15s)
GET /gyms/error     # Simulation d'erreur
```

## Scénarios de Test

### 1. Fonctionnement Normal
```bash
# Créer une salle
curl -X POST http://localhost:8081/gyms \
  -H "Content-Type: application/json" \
  -d '{"name":"Gym Test","location":"Centre-ville","capacity":100}'

# Tester les deux approches
curl http://localhost:8082/api/courses/test/gyms-without-cb
curl http://localhost:8082/api/courses/test/gyms-with-cb
```

### 2. Test de Timeout
```bash
# Déclencher un timeout
curl http://localhost:8081/gyms/slow &

# Tester pendant la latence
curl http://localhost:8082/api/courses/test/gyms-without-cb  # Timeout
curl http://localhost:8082/api/courses/test/gyms-with-cb     # Fallback
```

### 3. Test d'Erreur
```bash
# Générer des erreurs pour ouvrir le circuit
for i in {1..6}; do
  curl http://localhost:8081/gyms/error
done

# Tester après ouverture du circuit
curl http://localhost:8082/api/courses/test/gyms-with-cb  # Fallback immédiat
```

### 4. Test de Récupération
```bash
# Attendre 30s après ouverture du circuit
sleep 30

# Le circuit passe en HALF_OPEN
curl http://localhost:8082/api/courses/circuit-breaker/state

# Appels de test pour fermer le circuit
curl http://localhost:8082/api/courses/test/gyms-with-cb
```

## Monitoring et Observabilité

### Actuator Endpoints
```bash
# Santé générale
GET /actuator/health

# État des Circuit Breakers
GET /actuator/circuitbreakers

# Événements des Circuit Breakers
GET /actuator/circuitbreakerevents
```

### Métriques Personnalisées
```bash
# État actuel
GET /api/courses/circuit-breaker/state

# Métriques détaillées
GET /api/courses/circuit-breaker/metrics
```

### Logs Structurés
```
INFO  - Récupération de toutes les salles AVEC Circuit Breaker
INFO  - État du Circuit Breaker: CLOSED
INFO  - Récupération réussie de 3 salles avec CB
WARN  - Fallback activé pour getAllGyms() - Service Gym indisponible
ERROR - Erreur avec Circuit Breaker pour getAllGyms: Connection refused
```

## Avantages et Inconvénients

### Sans Circuit Breaker
**Avantages :**
- ✅ Simplicité d'implémentation
- ✅ Comportement prévisible
- ✅ Propagation directe des erreurs

**Inconvénients :**
- ❌ Pas de protection contre les pannes en cascade
- ❌ Indisponibilité totale en cas de panne
- ❌ Pas d'adaptation aux conditions réseau

### Avec Circuit Breaker
**Avantages :**
- ✅ Résilience aux pannes
- ✅ Dégradation gracieuse
- ✅ Récupération automatique
- ✅ Métriques et monitoring
- ✅ Protection contre les pannes en cascade

**Inconvénients :**
- ❌ Complexité supplémentaire
- ❌ Configuration à ajuster
- ❌ Comportement parfois imprévisible

## Bonnes Pratiques

### 1. Configuration du Circuit Breaker
- Ajuster les seuils selon le contexte métier
- Définir des fallbacks appropriés
- Monitorer les métriques régulièrement

### 2. Gestion des Timeouts
- Timeout de connexion court (5s)
- Timeout de lecture adapté au service (10s)
- Retry avec backoff exponentiel

### 3. Fallback Strategy
- Données par défaut cohérentes
- Logs explicites pour le debugging
- Éviter les fallbacks qui masquent les problèmes

### 4. Monitoring
- Alertes sur l'ouverture des circuits
- Métriques de performance
- Logs structurés pour l'analyse

## Conclusion

Cette implémentation démontre l'importance de la résilience dans une architecture microservices. Le Circuit Breaker avec Resilience4j apporte une protection significative contre les pannes en cascade tout en maintenant la disponibilité du service avec une dégradation gracieuse.

Le choix entre les deux approches dépend du contexte :
- **Sans CB** : Environnements simples, services critiques
- **Avec CB** : Production, haute disponibilité, services distribués