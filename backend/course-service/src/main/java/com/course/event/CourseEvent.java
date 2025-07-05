package com.course.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CourseEvent {
    
    public enum EventType {
        COURSE_CREATED,
        COURSE_UPDATED,
        COURSE_DELETED,
        COURSE_CAPACITY_CHANGED,
        COURSE_ASSIGNED_TO_GYM,
        COURSE_UNASSIGNED_FROM_GYM
    }
    
    private Long courseId;
    private String courseName;
    private String instructor;
    private String gymId;
    private String gymName;
    private Integer maxParticipants;
    private BigDecimal price;
    private String level;
    private EventType eventType;
    private LocalDateTime timestamp;
    private String description;

    // Constructeurs
    public CourseEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public CourseEvent(Long courseId, String courseName, EventType eventType) {
        this();
        this.courseId = courseId;
        this.courseName = courseName;
        this.eventType = eventType;
    }

    // Getters et Setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getGymId() { return gymId; }
    public void setGymId(String gymId) { this.gymId = gymId; }

    public String getGymName() { return gymName; }
    public void setGymName(String gymName) { this.gymName = gymName; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "CourseEvent{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", eventType=" + eventType +
                ", timestamp=" + timestamp +
                '}';
    }
}