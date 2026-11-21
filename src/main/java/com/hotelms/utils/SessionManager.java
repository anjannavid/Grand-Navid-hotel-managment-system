package com.hotelms.utils;

import com.hotelms.model.User;
import java.time.LocalDateTime;

public final class SessionManager {

    private static volatile SessionManager instance;
    private User currentUser;
    private LocalDateTime loginTime;
    private int failedLoginAttempts = 0;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) instance = new SessionManager();
            }
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    public void logout() {
        this.currentUser = null;
        this.loginTime = null;
    }

    public boolean isLoggedIn() { return currentUser != null; }
    public User getCurrentUser() { return currentUser; }
    public LocalDateTime getLoginTime() { return loginTime; }

    public int getUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isReceptionist() {
        return currentUser != null && (currentUser.isAdmin() || currentUser.isReceptionist());
    }

    public void incrementFailedAttempts() { failedLoginAttempts++; }
    public void resetFailedAttempts()     { failedLoginAttempts = 0; }
    public int getFailedLoginAttempts()   { return failedLoginAttempts; }
}
