package com.course.service;

import com.course.event.CourseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour publier des messages Kafka depuis le Course Service
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    // Topics Kafka
    public static final String COURSE_EVENTS_TOPIC = "course-events";
    public static final String COURSE_NOTIFICATIONS_TOPIC = "course-notifications";

    @Autowired
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, CourseEvent> courseEventKafkaTemplate;

    /**
     * Publier un message STRING
     */
    public void sendStringMessage(String topic, String key, String message) {
        logger.info("Envoi message STRING vers topic '{}' avec clé '{}': {}", topic, key, message);
        
        CompletableFuture<SendResult<String, String>> future = 
            stringKafkaTemplate.send(topic, key, message);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("Message STRING envoyé avec succès: offset={}, partition={}", 
                    result.getRecordMetadata().offset(), 
                    result.getRecordMetadata().partition());
            } else {
                logger.error("Erreur lors de l'envoi du message STRING: {}", exception.getMessage());
            }
        });
    }

    /**
     * Publier un événement COURSE (OBJET JSON)
     */
    public void sendCourseEvent(CourseEvent event) {
        logger.info("Envoi événement COURSE: {} pour cours {}", event.getEventType(), event.getCourseId());
        
        String key = event.getCourseId().toString(); // Utiliser l'ID du cours comme clé
        
        CompletableFuture<SendResult<String, CourseEvent>> future = 
            courseEventKafkaTemplate.send(COURSE_EVENTS_TOPIC, key, event);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("Événement COURSE envoyé avec succès: offset={}, partition={}", 
                    result.getRecordMetadata().offset(), 
                    result.getRecordMetadata().partition());
            } else {
                logger.error("Erreur lors de l'envoi de l'événement COURSE: {}", exception.getMessage());
            }
        });
    }

    /**
     * Publier une notification simple (STRING)
     */
    public void sendNotification(String courseId, String message) {
        logger.info("Envoi notification pour cours {}: {}", courseId, message);
        sendStringMessage(COURSE_NOTIFICATIONS_TOPIC, courseId, message);
    }

    /**
     * Publier un événement de création de cours
     */
    public void publishCourseCreated(Long courseId, String courseName, String instructor, 
                                   String gymId, String gymName, Integer maxParticipants, 
                                   BigDecimal price, String level) {
        CourseEvent event = new CourseEvent(courseId, courseName, CourseEvent.EventType.COURSE_CREATED);
        event.setInstructor(instructor);
        event.setGymId(gymId);
        event.setGymName(gymName);
        event.setMaxParticipants(maxParticipants);
        event.setPrice(price);
        event.setLevel(level);
        event.setDescription("Nouveau cours créé");
        
        sendCourseEvent(event);
        sendNotification(courseId.toString(), "Cours créé: " + courseName);
    }

    /**
     * Publier un événement de mise à jour de cours
     */
    public void publishCourseUpdated(Long courseId, String courseName, String instructor,
                                   String gymId, String gymName, Integer maxParticipants,
                                   BigDecimal price, String level) {
        CourseEvent event = new CourseEvent(courseId, courseName, CourseEvent.EventType.COURSE_UPDATED);
        event.setInstructor(instructor);
        event.setGymId(gymId);
        event.setGymName(gymName);
        event.setMaxParticipants(maxParticipants);
        event.setPrice(price);
        event.setLevel(level);
        event.setDescription("Cours mis à jour");
        
        sendCourseEvent(event);
        sendNotification(courseId.toString(), "Cours mis à jour: " + courseName);
    }

    /**
     * Publier un événement de suppression de cours
     */
    public void publishCourseDeleted(Long courseId, String courseName) {
        CourseEvent event = new CourseEvent(courseId, courseName, CourseEvent.EventType.COURSE_DELETED);
        event.setDescription("Cours supprimé");
        
        sendCourseEvent(event);
        sendNotification(courseId.toString(), "Cours supprimé: " + courseName);
    }

    /**
     * Publier un événement d'assignation de cours à une salle
     */
    public void publishCourseAssignedToGym(Long courseId, String courseName, String gymId, String gymName) {
        CourseEvent event = new CourseEvent(courseId, courseName, CourseEvent.EventType.COURSE_ASSIGNED_TO_GYM);
        event.setGymId(gymId);
        event.setGymName(gymName);
        event.setDescription("Cours assigné à la salle: " + gymName);
        
        sendCourseEvent(event);
        sendNotification(courseId.toString(), 
            String.format("Cours %s assigné à la salle %s", courseName, gymName));
    }

    /**
     * Publier un événement de désassignation de cours d'une salle
     */
    public void publishCourseUnassignedFromGym(Long courseId, String courseName, String gymId, String gymName) {
        CourseEvent event = new CourseEvent(courseId, courseName, CourseEvent.EventType.COURSE_UNASSIGNED_FROM_GYM);
        event.setGymId(gymId);
        event.setGymName(gymName);
        event.setDescription("Cours désassigné de la salle: " + gymName);
        
        sendCourseEvent(event);
        sendNotification(courseId.toString(), 
            String.format("Cours %s désassigné de la salle %s", courseName, gymName));
    }
}