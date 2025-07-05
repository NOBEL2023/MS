package com.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CourseDTO {
    private Long id;
    
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    private String description;
    private String instructor;
    
    @Positive(message = "La durée doit être positive")
    private Integer duration;
    
    @Positive(message = "Le nombre maximum de participants doit être positif")
    private Integer maxParticipants;
    
    @Positive(message = "Le prix doit être positif")
    private BigDecimal price;
    
    private String schedule;
    private String level;
    private String gymId;
    private String gymName; // Ajout du nom de la salle

    // Constructeurs
    public CourseDTO() {}

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
}