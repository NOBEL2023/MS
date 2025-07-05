package com.course.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration du Circuit Breaker avec Resilience4j
 */
@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public CircuitBreaker gymServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)                    // 50% de taux d'échec pour ouvrir le circuit
            .waitDurationInOpenState(Duration.ofSeconds(30))  // Attendre 30s avant de passer en half-open
            .slidingWindowSize(10)                       // Fenêtre glissante de 10 appels
            .minimumNumberOfCalls(5)                     // Minimum 5 appels avant calcul du taux d'échec
            .permittedNumberOfCallsInHalfOpenState(3)    // 3 appels autorisés en half-open
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build();

        return registry.circuitBreaker("gym-service", config);
    }
}