package com.course.dto;

import java.math.BigDecimal;

public class CourseWithGymDTO {
    private Long id;
    private String title;
    private String description;
    private String instructor;
    private Integer duration;
    private Integer maxParticipants;
    private BigDecimal price;
    private String schedule;
    private String level;
    private String gymId;
    
    // Informations enrichies de la salle
    private String gymName;
    private String gymLocation;
    private Integer gymCapacity;

    // Constructeurs
    public CourseWithGymDTO() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getGymId() { return gymId; }
    public void setGymId(String gymId) { this.gymId = gymId; }

    public String getGymName() { return gymName; }
    public void setGymName(String gymName) { this.gymName = gymName; }

    public String getGymLocation() { return gymLocation; }
    public void setGymLocation(String gymLocation) { this.gymLocation = gymLocation; }

    public Integer getGymCapacity() { return gymCapacity; }
    public void setGymCapacity(Integer gymCapacity) { this.gymCapacity = gymCapacity; }
}