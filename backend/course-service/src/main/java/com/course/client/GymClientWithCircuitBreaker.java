package com.course.client;

import com.course.dto.GymDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Client Feign AVEC Circuit Breaker pour communiquer avec le Gym Service
 * Utilise Resilience4j pour la gestion des pannes
 */
@FeignClient(
    name = "gym-service-with-cb",
    url = "${gym-service.url:http://localhost:8081}",
    configuration = GymClientConfiguration.class,
    fallback = GymClientFallback.class
)
public interface GymClientWithCircuitBreaker {

    @GetMapping("/gyms")
    List<GymDTO> getAllGyms();

    @GetMapping("/gyms/{id}")
    GymDTO getGymById(@PathVariable("id") String id);
}