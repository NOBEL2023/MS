package com.course.dto;

public class GymDTO {
    private String id;
    private String name;
    private String location;
    private String phone;
    private String email;
    private Integer capacity;

    // Constructeurs
    public GymDTO() {}

    public GymDTO(String id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return "GymDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", capacity=" + capacity +
                '}';
    }
}