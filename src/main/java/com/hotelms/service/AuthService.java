package com.hotelms.service;

import com.hotelms.dao.UserDAO;
import com.hotelms.model.User;
import com.hotelms.utils.ActivityLogger;
import com.hotelms.utils.PasswordUtils;
import com.hotelms.utils.SessionManager;
import com.hotelms.utils.ValidationUtils;
import com.hotelms.config.AppConfig;

import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;
    private final SessionManager session;

    public AuthService() {
        this.userDAO  = new UserDAO();
        this.session  = SessionManager.getInstance();
    }

    /**
     * Authenticates a user. Returns null on success (session is set),
     * or an error message string on failure.
     */
    public String login(String username, String password) {
        // Lockout check
        if (session.getFailedLoginAttempts() >= AppConfig.MAX_LOGIN_TRIES) {
            return "Account temporarily locked after " + AppConfig.MAX_LOGIN_TRIES + " failed attempts. Restart the app.";
        }

        if (username == null || username.isBlank()) return "Username is required";
        if (password == null || password.isBlank()) return "Password is required";

        Optional<User> userOpt = userDAO.findByUsername(username.trim().toLowerCase());

        if (userOpt.isEmpty()) {
            session.incrementFailedAttempts();
            return "Invalid username or password";
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            return "Your account has been deactivated. Contact administrator.";
        }

        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            session.incrementFailedAttempts();
            int remaining = AppConfig.MAX_LOGIN_TRIES - session.getFailedLoginAttempts();
            return "Invalid username or password." + (remaining > 0 ? " " + remaining + " attempts remaining." : "");
        }

        // Success
        session.login(user);
        session.resetFailedAttempts();
        userDAO.updateLastLogin(user.getUserId());
        ActivityLogger.log(user.getUserId(), "User logged in", ActivityLogger.MODULE_AUTH);
        return null; // null = success
    }

    public void logout() {
        if (session.isLoggedIn()) {
            ActivityLogger.log(session.getUserId(), "User logged out", ActivityLogger.MODULE_AUTH);
            session.logout();
        }
    }

    /**
     * Creates a new staff account.
     * @return null on success, error message on failure.
     */
    public String createUser(String username, String password, String fullName,
                              String email, String phone, User.Role role) {
        // Only admins can create users
        if (!session.isAdmin()) return "Insufficient permissions";

        String usernameErr = ValidationUtils.validateUsername(username);
        if (usernameErr != null) return usernameErr;

        String passwordErr = PasswordUtils.validatePasswordStrength(password);
        if (passwordErr != null) return passwordErr;

        if (fullName == null || fullName.isBlank()) return "Full name is required";

        String emailErr = ValidationUtils.validateEmail(email);
        if (emailErr != null) return emailErr;

        if (userDAO.existsByUsername(username.toLowerCase())) return "Username already taken";
        if (email != null && !email.isBlank() && userDAO.existsByEmail(email)) return "Email already registered";

        User user = new User();
        user.setUsername(username.toLowerCase().trim());
        user.setPasswordHash(PasswordUtils.hashPassword(password));
        user.setFullName(fullName.trim());
        user.setEmail(email != null ? email.trim() : null);
        user.setPhone(phone);
        user.setRole(role);
        user.setActive(true);

        int newId = userDAO.insert(user);
        if (newId > 0) {
            ActivityLogger.log(session.getUserId(), "Created user: " + username, ActivityLogger.MODULE_STAFF, newId, null);
            return null;
        }
        return "Failed to create user. Please try again.";
    }

    public String changePassword(int userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) return "User not found";

        User user = userOpt.get();

        // Only the user themselves or an admin can change password
        if (userId != session.getUserId() && !session.isAdmin()) {
            return "Insufficient permissions";
        }

        // Verify old password if the user is changing their own
        if (userId == session.getUserId()) {
            if (!PasswordUtils.verifyPassword(oldPassword, user.getPasswordHash())) {
                return "Current password is incorrect";
            }
        }

        String strengthErr = PasswordUtils.validatePasswordStrength(newPassword);
        if (strengthErr != null) return strengthErr;

        boolean ok = userDAO.updatePassword(userId, PasswordUtils.hashPassword(newPassword));
        if (ok) {
            ActivityLogger.log(session.getUserId(), "Password changed for user ID: " + userId, ActivityLogger.MODULE_STAFF);
            return null;
        }
        return "Failed to update password";
    }
}
