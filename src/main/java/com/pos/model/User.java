package com.pos.model;

public class User {

    private int userId;
    private String username;
    private String fullName;
    private String role;
    private String status;

    public User(int userId, String username, String fullName,
                String role, String status) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}
