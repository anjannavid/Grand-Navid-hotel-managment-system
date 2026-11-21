package com.hotelms.model;

import java.time.LocalDateTime;

public class User {

    // Role Enum — defines permissions throughout the system
    public enum Role {
        ADMIN {
            @Override public String getDisplayName() { return "Administrator"; }
            @Override public boolean canManageStaff() { return true; }
            @Override public boolean canViewReports() { return true; }
            @Override public boolean canManageBilling() { return true; }
        },
        RECEPTIONIST {
            @Override public String getDisplayName() { return "Receptionist"; }
            @Override public boolean canManageStaff() { return false; }
            @Override public boolean canViewReports() { return true; }
            @Override public boolean canManageBilling() { return true; }
        },
        STAFF {
            @Override public String getDisplayName() { return "Staff"; }
            @Override public boolean canManageStaff() { return false; }
            @Override public boolean canViewReports() { return false; }
            @Override public boolean canManageBilling() { return false; }
        };

        public abstract String getDisplayName();
        public abstract boolean canManageStaff();
        public abstract boolean canViewReports();
        public abstract boolean canManageBilling();

        public static Role fromString(String value) {
            return switch (value.toUpperCase()) {
                case "ADMIN"        -> ADMIN;
                case "RECEPTIONIST" -> RECEPTIONIST;
                default             -> STAFF;
            };
        }
    }

    // Fields
    private int userId;
    private String username;
    private String passwordHash; // BCrypt hash — never the raw password
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean active;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    // Constructors
    public User() {}

    public User(String username, String fullName, String email, Role role) {
        this.username = username;
        this.fullName = fullName;
        this.email    = email;
        this.role     = role;
        this.active   = true;
    }

    // Getters & Setters
    public int getUserId()                    { return userId; }
    public void setUserId(int userId)         { this.userId = userId; }

    public String getUsername()               { return username; }
    public void setUsername(String username)  { this.username = username; }

    public String getPasswordHash()           { return passwordHash; }
    public void setPasswordHash(String hash)  { this.passwordHash = hash; }

    public String getFullName()               { return fullName; }
    public void setFullName(String fullName)  { this.fullName = fullName; }

    public String getEmail()                  { return email; }
    public void setEmail(String email)        { this.email = email; }

    public String getPhone()                  { return phone; }
    public void setPhone(String phone)        { this.phone = phone; }

    public Role getRole()                     { return role; }
    public void setRole(Role role)            { this.role = role; }

    public boolean isActive()                 { return active; }
    public void setActive(boolean active)     { this.active = active; }

    public LocalDateTime getLastLogin()                  { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin)    { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)    { this.createdAt = createdAt; }

    // Derived / Convenience
    public boolean isAdmin()        { return role == Role.ADMIN; }
    public boolean isReceptionist() { return role == Role.RECEPTIONIST; }

    
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length >= 2
            ? String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)
            : String.valueOf(parts[0].charAt(0));
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "', role=" + role + "}";
    }
}
