package com.cirt.osint_dashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User Model for Authentication
 * Collection: users (in MongoDB)
 */
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Field("username")
    private String username;

    @Field("password")
    @JsonIgnore  // Never send password in JSON responses!
    private String password;

    @Field("full_name")
    private String fullName;

    @Field("role")
    private String role;  // "ADMIN", "ANALYST", "VIEWER"

    @Field("active")
    private boolean active;

    @Field("created_at")
    private String createdAt;

    public User() {}

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.active = true;
        this.createdAt = new java.util.Date().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}