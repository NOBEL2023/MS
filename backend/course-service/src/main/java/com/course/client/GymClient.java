package com.course.client;

import com.course.dto.GymDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Client Feign SANS Circuit Breaker pour communiquer avec le Gym Service
 * Configuration basique avec timeout et retry
 */
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