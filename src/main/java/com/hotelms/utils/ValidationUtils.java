package com.hotelms.utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class ValidationUtils {

    private ValidationUtils() {}

    private static final Pattern EMAIL   = Pattern.compile("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE   = Pattern.compile("^[+]?[0-9\\-\\s]{7,15}$");
    private static final Pattern AADHAR  = Pattern.compile("^\\d{4}[- ]?\\d{4}[- ]?\\d{4}$");

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) return fieldName + " is required";
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.isBlank()) return null; // email optional in guest
        if (!EMAIL.matcher(email.trim()).matches()) return "Enter a valid email address";
        return null;
    }

    public static String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return "Phone number is required";
        if (!PHONE.matcher(phone.trim()).matches()) return "Enter a valid phone number";
        return null;
    }

    public static String validatePositiveAmount(String text, String fieldName) {
        try {
            double val = Double.parseDouble(text.trim().replace(",", ""));
            if (val < 0) return fieldName + " cannot be negative";
            return null;
        } catch (NumberFormatException e) {
            return fieldName + " must be a valid number";
        }
    }

    public static String validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null)  return "Check-in date is required";
        if (checkOut == null) return "Check-out date is required";
        if (!checkIn.isBefore(checkOut)) return "Check-out must be after check-in";
        if (checkIn.isBefore(LocalDate.now())) return "Check-in date cannot be in the past";
        return null;
    }

    public static String validateUsername(String username) {
        if (username == null || username.isBlank()) return "Username is required";
        if (username.length() < 4) return "Username must be at least 4 characters";
        if (!username.matches("^[a-zA-Z0-9_]+$")) return "Username can only contain letters, digits, and underscore";
        return null;
    }

    /** Returns true if the string represents a positive number */
    public static boolean isPositiveNumber(String text) {
        try {
            return Double.parseDouble(text.trim()) > 0;
        } catch (Exception e) { return false; }
    }
}
