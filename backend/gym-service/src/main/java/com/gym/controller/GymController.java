package com.gym.controller;

import com.gym.dto.GymDTO;
import com.gym.service.GymService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/gyms")
@CrossOrigin(origins = "*")
public class GymController {

    @Autowired
    private GymService gymService;

    @GetMapping
    public ResponseEntity<List<GymDTO>> getAllGyms() {
        return ResponseEntity.ok(gymService.getAllGyms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GymDTO> getGymById(@PathVariable String id) {
        Optional<GymDTO> gym = gymService.getGymById(id);
        return gym.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GymDTO> createGym(@RequestBody GymDTO gymDTO) {
        return ResponseEntity.ok(gymService.createGym(gymDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GymDTO> updateGym(@PathVariable String id, @RequestBody GymDTO gymDTO) {
        GymDTO updated = gymService.updateGym(id, gymDTO);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGym(@PathVariable String id) {
        gymService.deleteGym(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint pour tester la latence (simulation de lenteur)
     */
    @GetMapping("/slow")
    public ResponseEntity<List<GymDTO>> getGymsSlowly() throws InterruptedException {
        Thread.sleep(15000); // Simulation de 15 secondes de latence
        return ResponseEntity.ok(gymService.getAllGyms());
    }

    /**
     * Endpoint pour simuler une erreur
     */
    @GetMapping("/error")
    public ResponseEntity<List<GymDTO>> getGymsWithError() {
        throw new RuntimeException("Erreur simulée pour tester le Circuit Breaker");
    }
}