package com.course.client;

import com.course.dto.GymDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback pour le client Gym avec Circuit Breaker
 * Fournit des réponses par défaut en cas de panne du service Gym
 */
@Component
public class GymClientFallback implements GymClientWithCircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(GymClientFallback.class);

    @Override
    public List<GymDTO> getAllGyms() {
        logger.warn("Fallback activé pour getAllGyms() - Service Gym indisponible");
        return new ArrayList<>();
    }

    @Override
    public GymDTO getGymById(String id) {
        logger.warn("Fallback activé pour getGymById({}) - Service Gym indisponible", id);
        
        // Retourner une salle par défaut avec des informations minimales
        GymDTO fallbackGym = new GymDTO();
        fallbackGym.setId(id);
        fallbackGym.setName("Salle temporairement indisponible");
        fallbackGym.setLocation("Information non disponible");
        fallbackGym.setCapacity(0);
        
        return fallbackGym;
    }
}