package com.course.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Événement reçu du Gym Service
 * Copie locale pour la désérialisation Kafka
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GymEvent {
    
    public enum EventType {
        GYM_CREATED,
        GYM_UPDATED,
        GYM_DELETED,
        GYM_CAPACITY_CHANGED
    }
    
    private String gymId;
    private String gymName;
    private String gymEmail;
    private Integer capacity;
    private EventType eventType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private String description;

    // Constructeurs
    public GymEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public GymEvent(String gymId, String gymName, EventType eventType) {
        this();
        this.gymId = gymId;
        this.gymName = gymName;
        this.eventType = eventType;
    }

    // Getters et Setters
    public String getGymId() { return gymId; }
    public void setGymId(String gymId) { this.gymId = gymId; }

    public String getGymName() { return gymName; }
    public void setGymName(String gymName) { this.gymName = gymName; }

    public String getGymEmail() { return gymEmail; }
    public void setGymEmail(String gymEmail) { this.gymEmail = gymEmail; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "GymEvent{" +
                "gymId='" + gymId + '\'' +
                ", gymName='" + gymName + '\'' +
                ", eventType=" + eventType +
                ", timestamp=" + timestamp +
                '}';
    }
}