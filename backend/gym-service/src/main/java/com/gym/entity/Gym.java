
package com.gym.entity;

        import org.springframework.data.annotation.Id;
        import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gyms")
public class Gym {
    @Id
    private String id;
    private String name;
    private String location;

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
