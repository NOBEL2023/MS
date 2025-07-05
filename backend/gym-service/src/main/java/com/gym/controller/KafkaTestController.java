package com.gym.controller;

import com.gym.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller pour tester la communication Kafka du Gym Service
 */
@RestController
@RequestMapping("/api/kafka-test")
@CrossOrigin(origins = "*")
public class KafkaTestController {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * Test d'envoi de message STRING
     */
    @PostMapping("/send-string")
    public ResponseEntity<String> sendStringMessage(
            @RequestParam String topic,
            @RequestParam String key,
            @RequestParam String message) {
        
        kafkaProducerService.sendStringMessage(topic, key, message);
        return ResponseEntity.ok("Message STRING envoyé vers topic: " + topic);
    }

    /**
     * Test d'envoi de notification
     */
    @PostMapping("/send-notification")
    public ResponseEntity<String> sendNotification(
            @RequestParam String gymId,
            @RequestParam String message) {
        
        kafkaProducerService.sendNotification(gymId, message);
        return ResponseEntity.ok("Notification envoyée pour la salle: " + gymId);
    }

    /**
     * Test d'envoi d'événement de création de salle
     */
    @PostMapping("/send-gym-created")
    public ResponseEntity<String> sendGymCreatedEvent(
            @RequestParam String gymId,
            @RequestParam String gymName,
            @RequestParam(required = false) String gymEmail,
            @RequestParam(required = false) Integer capacity) {
        
        kafkaProducerService.publishGymCreated(gymId, gymName, gymEmail, capacity);
        return ResponseEntity.ok("Événement GYM_CREATED envoyé");
    }

    /**
     * Test d'envoi d'événement de changement de capacité
     */
    @PostMapping("/send-capacity-changed")
    public ResponseEntity<String> sendCapacityChangedEvent(
            @RequestParam String gymId,
            @RequestParam String gymName,
            @RequestParam Integer oldCapacity,
            @RequestParam Integer newCapacity) {
        
        kafkaProducerService.publishGymCapacityChanged(gymId, gymName, oldCapacity, newCapacity);
        return ResponseEntity.ok("Événement GYM_CAPACITY_CHANGED envoyé");
    }

    /**
     * Test d'envoi d'événement de suppression de salle
     */
    @PostMapping("/send-gym-deleted")
    public ResponseEntity<String> sendGymDeletedEvent(
            @RequestParam String gymId,
            @RequestParam String gymName) {
        
        kafkaProducerService.publishGymDeleted(gymId, gymName);
        return ResponseEntity.ok("Événement GYM_DELETED envoyé");
    }

    /**
     * Informations sur les topics utilisés
     */
    @GetMapping("/topics-info")
    public ResponseEntity<Map<String, String>> getTopicsInfo() {
        return ResponseEntity.ok(Map.of(
            "gym-events", "Événements des salles (OBJET JSON)",
            "gym-notifications", "Notifications des salles (STRING)"
        ));
    }
}