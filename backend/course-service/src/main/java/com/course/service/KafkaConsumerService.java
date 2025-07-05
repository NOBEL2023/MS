package com.course.service;

import com.course.event.GymEvent;
import com.course.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Service pour consommer des messages Kafka
 * Écoute les événements du Gym Service
 */
@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * Consommer les messages STRING des notifications Gym
     */
    @KafkaListener(
        topics = "gym-notifications",
        groupId = "course-service-group",
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    public void consumeGymNotification(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        logger.info("Notification GYM reçue - Topic: {}, Partition: {}, Offset: {}, Key: {}, Message: {}", 
            topic, partition, offset, key, message);
        
        try {
            // Traitement de la notification
            processGymNotification(key, message);
            
            // Acquittement manuel
            acknowledgment.acknowledge();
            logger.info("Notification GYM traitée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la notification GYM: {}", e.getMessage(), e);
            // En cas d'erreur, on peut choisir de ne pas acquitter pour retry
        }
    }

    /**
     * Consommer les événements OBJET du Gym Service
     */
    @KafkaListener(
        topics = "gym-events",
        groupId = "course-service-group",
        containerFactory = "gymEventKafkaListenerContainerFactory"
    )
    public void consumeGymEvent(
            @Payload GymEvent gymEvent,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        logger.info("Événement GYM reçu - Topic: {}, Partition: {}, Offset: {}, Key: {}, Event: {}", 
            topic, partition, offset, key, gymEvent);
        
        try {
            // Traitement de l'événement selon son type
            processGymEvent(gymEvent);
            
            // Acquittement manuel
            acknowledgment.acknowledge();
            logger.info("Événement GYM traité avec succès: {}", gymEvent.getEventType());
            
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de l'événement GYM: {}", e.getMessage(), e);
            // En cas d'erreur critique, on peut choisir de ne pas acquitter
        }
    }

    /**
     * Traiter une notification simple de salle
     */
    private void processGymNotification(String gymId, String message) {
        logger.info("Traitement notification pour salle {}: {}", gymId, message);
        
        // Exemple: logger les notifications pour audit
        // Ici on pourrait sauvegarder en base, envoyer des emails, etc.
        
        if (message.contains("supprimée")) {
            logger.warn("Salle {} supprimée - Vérification des cours associés nécessaire", gymId);
        }
    }

    /**
     * Traiter un événement de salle
     */
    private void processGymEvent(GymEvent gymEvent) {
        logger.info("Traitement événement {} pour salle {}", gymEvent.getEventType(), gymEvent.getGymId());
        
        switch (gymEvent.getEventType()) {
            case GYM_CREATED:
                handleGymCreated(gymEvent);
                break;
                
            case GYM_UPDATED:
                handleGymUpdated(gymEvent);
                break;
                
            case GYM_DELETED:
                handleGymDeleted(gymEvent);
                break;
                
            case GYM_CAPACITY_CHANGED:
                handleGymCapacityChanged(gymEvent);
                break;
                
            default:
                logger.warn("Type d'événement GYM non géré: {}", gymEvent.getEventType());
        }
    }

    /**
     * Gérer la création d'une salle
     */
    private void handleGymCreated(GymEvent gymEvent) {
        logger.info("Nouvelle salle créée: {} ({})", gymEvent.getGymName(), gymEvent.getGymId());
        
        // Ici on pourrait:
        // - Mettre à jour un cache local des salles
        // - Envoyer des notifications aux utilisateurs
        // - Déclencher des processus métier
        
        // Exemple: publier une notification pour les cours
        kafkaProducerService.sendNotification("system", 
            "Nouvelle salle disponible: " + gymEvent.getGymName());
    }

    /**
     * Gérer la mise à jour d'une salle
     */
    private void handleGymUpdated(GymEvent gymEvent) {
        logger.info("Salle mise à jour: {} ({})", gymEvent.getGymName(), gymEvent.getGymId());
        
        // Mettre à jour les informations locales si nécessaire
        // Invalider les caches
        // Notifier les cours concernés
    }

    /**
     * Gérer la suppression d'une salle
     */
    private void handleGymDeleted(GymEvent gymEvent) {
        logger.warn("Salle supprimée: {} ({})", gymEvent.getGymName(), gymEvent.getGymId());
        
        // IMPORTANT: Désassocier tous les cours de cette salle
        var coursesToUpdate = courseRepository.findByGymId(gymEvent.getGymId());
        
        if (!coursesToUpdate.isEmpty()) {
            logger.info("Désassociation de {} cours de la salle supprimée", coursesToUpdate.size());
            
            coursesToUpdate.forEach(course -> {
                String oldGymName = gymEvent.getGymName();
                
                // Publier un événement de désassociation
                kafkaProducerService.publishCourseUnassignedFromGym(
                    course.getId(), 
                    course.getTitle(), 
                    gymEvent.getGymId(), 
                    oldGymName
                );
                
                // Retirer l'association
                course.setGymId(null);
                courseRepository.save(course);
            });
            
            logger.info("Tous les cours ont été désassociés de la salle supprimée");
        }
    }

    /**
     * Gérer le changement de capacité d'une salle
     */
    private void handleGymCapacityChanged(GymEvent gymEvent) {
        logger.info("Capacité de la salle {} changée: {}", 
            gymEvent.getGymName(), gymEvent.getCapacity());
        
        // Vérifier si les cours associés respectent toujours la nouvelle capacité
        var associatedCourses = courseRepository.findByGymId(gymEvent.getGymId());
        
        associatedCourses.forEach(course -> {
            if (course.getMaxParticipants() != null && 
                course.getMaxParticipants() > gymEvent.getCapacity()) {
                
                logger.warn("Cours {} dépasse la nouvelle capacité de la salle {} ({} > {})", 
                    course.getTitle(), gymEvent.getGymName(), 
                    course.getMaxParticipants(), gymEvent.getCapacity());
                
                // Publier une notification d'alerte
                kafkaProducerService.sendNotification(course.getId().toString(),
                    String.format("ALERTE: Le cours %s dépasse la capacité de la salle %s", 
                        course.getTitle(), gymEvent.getGymName()));
            }
        });
    }
}