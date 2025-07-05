package com.course.controller;

import com.course.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller pour tester la communication Kafka
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
            @RequestParam String courseId,
            @RequestParam String message) {
        
        kafkaProducerService.sendNotification(courseId, message);
        return ResponseEntity.ok("Notification envoyée pour le cours: " + courseId);
    }

    /**
     * Test d'envoi d'événement de création de cours
     */
    @PostMapping("/send-course-created")
    public ResponseEntity<String> sendCourseCreatedEvent(@RequestBody Map<String, Object> courseData) {
        kafkaProducerService.publishCourseCreated(
            Long.valueOf(courseData.get("courseId").toString()),
            courseData.get("courseName").toString(),
            courseData.get("instructor").toString(),
            courseData.get("gymId") != null ? courseData.get("gymId").toString() : null,
            courseData.get("gymName") != null ? courseData.get("gymName").toString() : null,
            courseData.get("maxParticipants") != null ? 
                Integer.valueOf(courseData.get("maxParticipants").toString()) : null,
            courseData.get("price") != null ? 
                new BigDecimal(courseData.get("price").toString()) : null,
            courseData.get("level") != null ? courseData.get("level").toString() : null
        );
        
        return ResponseEntity.ok("Événement COURSE_CREATED envoyé");
    }

    /**
     * Test d'envoi d'événement d'assignation de cours à une salle
     */
    @PostMapping("/send-course-assigned")
    public ResponseEntity<String> sendCourseAssignedEvent(
            @RequestParam Long courseId,
            @RequestParam String courseName,
            @RequestParam String gymId,
            @RequestParam String gymName) {
        
        kafkaProducerService.publishCourseAssignedToGym(courseId, courseName, gymId, gymName);
        return ResponseEntity.ok("Événement COURSE_ASSIGNED_TO_GYM envoyé");
    }

    /**
     * Informations sur les topics utilisés
     */
    @GetMapping("/topics-info")
    public ResponseEntity<Map<String, String>> getTopicsInfo() {
        return ResponseEntity.ok(Map.of(
            "course-events", "Événements des cours (OBJET JSON)",
            "course-notifications", "Notifications des cours (STRING)",
            "gym-events", "Événements des salles (OBJET JSON) - Consommé",
            "gym-notifications", "Notifications des salles (STRING) - Consommé"
        ));
    }
}