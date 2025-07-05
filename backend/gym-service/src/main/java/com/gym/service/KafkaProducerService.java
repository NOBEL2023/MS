package com.gym.service;

import com.gym.event.GymEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service pour publier des messages Kafka
 * Support des messages String et Objet
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    // Topics Kafka
    public static final String GYM_EVENTS_TOPIC = "gym-events";
    public static final String GYM_NOTIFICATIONS_TOPIC = "gym-notifications";

    @Autowired
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, GymEvent> gymEventKafkaTemplate;

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
     * Publier un événement GYM (OBJET JSON)
     */
    public void sendGymEvent(GymEvent event) {
        logger.info("Envoi événement GYM: {} pour salle {}", event.getEventType(), event.getGymId());
        
        String key = event.getGymId(); // Utiliser l'ID de la salle comme clé pour le partitioning
        
        CompletableFuture<SendResult<String, GymEvent>> future = 
            gymEventKafkaTemplate.send(GYM_EVENTS_TOPIC, key, event);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("Événement GYM envoyé avec succès: offset={}, partition={}", 
                    result.getRecordMetadata().offset(), 
                    result.getRecordMetadata().partition());
            } else {
                logger.error("Erreur lors de l'envoi de l'événement GYM: {}", exception.getMessage());
            }
        });
    }

    /**
     * Publier une notification simple (STRING)
     */
    public void sendNotification(String gymId, String message) {
        logger.info("Envoi notification pour salle {}: {}", gymId, message);
        sendStringMessage(GYM_NOTIFICATIONS_TOPIC, gymId, message);
    }

    /**
     * Publier un événement de création de salle
     */
    public void publishGymCreated(String gymId, String gymName, String gymEmail, Integer capacity) {
        GymEvent event = new GymEvent(gymId, gymName, GymEvent.EventType.GYM_CREATED);
        event.setGymEmail(gymEmail);
        event.setCapacity(capacity);
        event.setDescription("Nouvelle salle de sport créée");
        
        sendGymEvent(event);
        
        // Notification simple en parallèle
        sendNotification(gymId, "Salle créée: " + gymName);
    }

    /**
     * Publier un événement de mise à jour de salle
     */
    public void publishGymUpdated(String gymId, String gymName, String gymEmail, Integer capacity) {
        GymEvent event = new GymEvent(gymId, gymName, GymEvent.EventType.GYM_UPDATED);
        event.setGymEmail(gymEmail);
        event.setCapacity(capacity);
        event.setDescription("Informations de la salle mises à jour");
        
        sendGymEvent(event);
        sendNotification(gymId, "Salle mise à jour: " + gymName);
    }

    /**
     * Publier un événement de suppression de salle
     */
    public void publishGymDeleted(String gymId, String gymName) {
        GymEvent event = new GymEvent(gymId, gymName, GymEvent.EventType.GYM_DELETED);
        event.setDescription("Salle de sport supprimée");
        
        sendGymEvent(event);
        sendNotification(gymId, "Salle supprimée: " + gymName);
    }

    /**
     * Publier un événement de changement de capacité
     */
    public void publishGymCapacityChanged(String gymId, String gymName, Integer oldCapacity, Integer newCapacity) {
        GymEvent event = new GymEvent(gymId, gymName, GymEvent.EventType.GYM_CAPACITY_CHANGED);
        event.setCapacity(newCapacity);
        event.setDescription(String.format("Capacité changée de %d à %d", oldCapacity, newCapacity));
        
        sendGymEvent(event);
        sendNotification(gymId, String.format("Capacité de %s changée: %d → %d", gymName, oldCapacity, newCapacity));
    }
}