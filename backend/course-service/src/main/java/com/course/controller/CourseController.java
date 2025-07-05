package com.course.controller;

import com.course.dto.CourseDTO;
import com.course.dto.CourseWithGymDTO;
import com.course.dto.GymDTO;
import com.course.service.CourseService;
import com.course.service.GymIntegrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private GymIntegrationService gymIntegrationService;

    @PostMapping
    public ResponseEntity<CourseDTO> create(@Valid @RequestBody CourseDTO dto) {
        return ResponseEntity.ok(courseService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAll() {
        return ResponseEntity.ok(courseService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> update(@PathVariable Long id, @Valid @RequestBody CourseDTO dto) {
        return ResponseEntity.ok(courseService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère tous les cours enrichis avec les informations des salles
     */
    @GetMapping("/with-gym-info")
    public ResponseEntity<List<CourseWithGymDTO>> getAllCoursesWithGymInfo() {
        return ResponseEntity.ok(courseService.getAllCoursesWithGymInfo());
    }

    /**
     * Récupère les cours d'une salle spécifique par ID
     */
    @GetMapping("/by-gym/{gymId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByGym(@PathVariable String gymId) {
        return ResponseEntity.ok(courseService.getCoursesByGymId(gymId));
    }

    /**
     * Récupère les cours d'une salle spécifique par nom
     */
    @GetMapping("/by-gym-name/{gymName}")
    public ResponseEntity<List<CourseDTO>> getCoursesByGymName(@PathVariable String gymName) {
        return ResponseEntity.ok(courseService.getCoursesByGymName(gymName));
    }

    /**
     * Récupère toutes les salles disponibles (pour les formulaires)
     */
    @GetMapping("/available-gyms")
    public ResponseEntity<List<GymDTO>> getAvailableGyms() {
        return ResponseEntity.ok(courseService.getAvailableGyms());
    }

    /**
     * Test de communication SANS Circuit Breaker
     */
    @GetMapping("/test/gyms-without-cb")
    public ResponseEntity<List<GymDTO>> testGymsWithoutCircuitBreaker() {
        return ResponseEntity.ok(gymIntegrationService.getAllGymsWithoutCircuitBreaker());
    }

    /**
     * Test de communication AVEC Circuit Breaker
     */
    @GetMapping("/test/gyms-with-cb")
    public ResponseEntity<List<GymDTO>> testGymsWithCircuitBreaker() {
        return ResponseEntity.ok(gymIntegrationService.getAllGymsWithCircuitBreaker());
    }

    /**
     * Obtient l'état du Circuit Breaker
     */
    @GetMapping("/circuit-breaker/state")
    public ResponseEntity<String> getCircuitBreakerState() {
        return ResponseEntity.ok(gymIntegrationService.getCircuitBreakerState());
    }

    /**
     * Obtient les métriques du Circuit Breaker
     */
    @GetMapping("/circuit-breaker/metrics")
    public ResponseEntity<String> getCircuitBreakerMetrics() {
        return ResponseEntity.ok(gymIntegrationService.getCircuitBreakerMetrics());
    }
}