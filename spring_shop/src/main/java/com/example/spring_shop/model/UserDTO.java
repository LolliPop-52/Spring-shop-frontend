package com.example.spring_shop.model;

import com.google.gson.annotations.SerializedName;

public class UserDTO {
    private Long id;
    private String password;
    private String confirmPassword;
    private String name;
    private String email;
    private Long bucketId;
    private String role;
    private boolean enabled = false;

    public UserDTO() {
    }

    public UserDTO(Long id, String password, String confirmPassword, String name, String email, Long bucketId, String role, boolean enabled) {
        this.id = id;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.name = name;
        this.email = email;
        this.bucketId = bucketId;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getBucketId() { return bucketId; }
    public void setBucketId(Long bucketId) { this.bucketId = bucketId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
