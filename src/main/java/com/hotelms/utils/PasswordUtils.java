package com.hotelms.utils;

import org.mindrot.jbcrypt.BCrypt;
import com.hotelms.config.AppConfig;

public final class PasswordUtils {

    private PasswordUtils() {}

    /**
     * Hashes a plain-text password with BCrypt.
     * The salt is embedded in the resulting hash string.
     *
     * @param plainPassword the raw password entered by the user
     * @return a 60-character BCrypt hash, safe to store in the DB
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(AppConfig.BCRYPT_ROUNDS));
    }

    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Malformed hash — treat as mismatch
            return false;
        }
    }

    /**
     * Validates password strength.
     * Rules: 8+ chars, at least one uppercase, one lowercase, one digit, one special char.
     *
     * @return null if valid; error message string if invalid
     */
    public static String validatePasswordStrength(String password) {
        if (password == null || password.length() < 8)
            return "Password must be at least 8 characters long";
        if (!password.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter";
        if (!password.matches(".*[a-z].*"))
            return "Password must contain at least one lowercase letter";
        if (!password.matches(".*\\d.*"))
            return "Password must contain at least one digit";
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
            return "Password must contain at least one special character";
        return null; // valid
    }
}
