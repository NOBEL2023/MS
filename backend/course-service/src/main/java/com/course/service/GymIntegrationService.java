package com.course.service;

import com.course.client.GymClient;
import com.course.client.GymClientWithCircuitBreaker;
import com.course.dto.GymDTO;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Service d'intégration avec le microservice Gym
 * Propose deux modes : avec et sans Circuit Breaker
 */
@Service
public class GymIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(GymIntegrationService.class);

    @Autowired
    private GymClient gymClient;

    @Autowired
    private GymClientWithCircuitBreaker gymClientWithCircuitBreaker;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Récupère toutes les salles SANS Circuit Breaker
     * En cas d'erreur, l'exception est propagée
     */
    public List<GymDTO> getAllGymsWithoutCircuitBreaker() {
        logger.info("Récupération de toutes les salles SANS Circuit Breaker");
        try {
            List<GymDTO> gyms = gymClient.getAllGyms();
            logger.info("Récupération réussie de {} salles", gyms.size());
            return gyms;
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des salles sans CB: {}", e.getMessage());
            throw new RuntimeException("Service Gym indisponible", e);
        }
    }

    /**
     * Récupère une salle par ID SANS Circuit Breaker
     */
    public Optional<GymDTO> getGymByIdWithoutCircuitBreaker(String gymId) {
        logger.info("Récupération de la salle {} SANS Circuit Breaker", gymId);
        try {
            GymDTO gym = gymClient.getGymById(gymId);
            logger.info("Salle récupérée avec succès: {}", gym.getName());
            return Optional.of(gym);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la salle {} sans CB: {}", gymId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Récupère toutes les salles AVEC Circuit Breaker
     * Utilise le fallback en cas de panne
     */
    public List<GymDTO> getAllGymsWithCircuitBreaker() {
        logger.info("Récupération de toutes les salles AVEC Circuit Breaker");
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("gym-service");
        logger.info("État du Circuit Breaker: {}", circuitBreaker.getState());

        Supplier<List<GymDTO>> decoratedSupplier = CircuitBreaker
            .decorateSupplier(circuitBreaker, gymClientWithCircuitBreaker::getAllGyms);

        try {
            List<GymDTO> gyms = decoratedSupplier.get();
            logger.info("Récupération réussie de {} salles avec CB", gyms.size());
            return gyms;
        } catch (Exception e) {
            logger.error("Erreur avec Circuit Breaker pour getAllGyms: {}", e.getMessage());
            // Le fallback est automatiquement appelé par Feign
            return gymClientWithCircuitBreaker.getAllGyms();
        }
    }

    /**
     * Récupère une salle par ID AVEC Circuit Breaker
     */
    public Optional<GymDTO> getGymByIdWithCircuitBreaker(String gymId) {
        logger.info("Récupération de la salle {} AVEC Circuit Breaker", gymId);
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("gym-service");
        logger.info("État du Circuit Breaker: {}", circuitBreaker.getState());

        Supplier<GymDTO> decoratedSupplier = CircuitBreaker
            .decorateSupplier(circuitBreaker, () -> gymClientWithCircuitBreaker.getGymById(gymId));

        try {
            GymDTO gym = decoratedSupplier.get();
            logger.info("Salle récupérée avec succès avec CB: {}", gym.getName());
            return Optional.of(gym);
        } catch (Exception e) {
            logger.error("Erreur avec Circuit Breaker pour getGymById({}): {}", gymId, e.getMessage());
            // Le fallback est automatiquement appelé par Feign
            GymDTO fallbackGym = gymClientWithCircuitBreaker.getGymById(gymId);
            return Optional.of(fallbackGym);
        }
    }

    /**
     * Valide qu'une salle existe (utilisé lors de la création/modification de cours)
     */
    public boolean validateGymExists(String gymId) {
        logger.info("Validation de l'existence de la salle: {}", gymId);
        
        try {
            Optional<GymDTO> gym = getGymByIdWithCircuitBreaker(gymId);
            boolean exists = gym.isPresent() && !gym.get().getName().contains("indisponible");
            logger.info("Salle {} existe: {}", gymId, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Erreur lors de la validation de la salle {}: {}", gymId, e.getMessage());
            return false;
        }
    }

    /**
     * Récupère les informations d'une salle pour enrichir les données d'un cours
     */
    public GymDTO getGymInfoForCourse(String gymId) {
        logger.info("Récupération des infos salle pour enrichissement: {}", gymId);
        
        Optional<GymDTO> gym = getGymByIdWithCircuitBreaker(gymId);
        return gym.orElse(createDefaultGym(gymId));
    }

    /**
     * Crée une salle par défaut en cas d'indisponibilité du service
     */
    private GymDTO createDefaultGym(String gymId) {
        GymDTO defaultGym = new GymDTO();
        defaultGym.setId(gymId);
        defaultGym.setName("Salle non disponible");
        defaultGym.setLocation("Information temporairement indisponible");
        defaultGym.setCapacity(0);
        return defaultGym;
    }

    /**
     * Méthode utilitaire pour obtenir l'état du Circuit Breaker
     */
    public String getCircuitBreakerState() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("gym-service");
        return circuitBreaker.getState().toString();
    }

    /**
     * Méthode utilitaire pour obtenir les métriques du Circuit Breaker
     */
    public String getCircuitBreakerMetrics() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("gym-service");
        var metrics = circuitBreaker.getMetrics();
        
        return String.format(
            "État: %s, Taux d'échec: %.2f%%, Nombre d'appels: %d, Appels échoués: %d",
            circuitBreaker.getState(),
            metrics.getFailureRate(),
            metrics.getNumberOfCalls(),
            metrics.getNumberOfFailedCalls()
        );
    }
}