package com.hotelms.config;

/**
 * AppConfig — Central configuration constants for the entire application.
 * All configurable values live here. No magic strings scattered in code.
 *
 * Place at: src/main/java/com/hotelms/config/AppConfig.java
 */
public final class AppConfig {

    // Prevent instantiation
    private AppConfig() {}

    // ──────────────────────────────────────────────────────
    // Application Info
    // ──────────────────────────────────────────────────────
    public static final String APP_NAME        = "HotelOS";
    public static final String APP_VERSION     = "1.0.0";
    public static final String APP_SUBTITLE    = "Hotel Management System";
    public static final String HOTEL_NAME      = "Grand NAVID Hotel";
    public static final String HOTEL_ADDRESS   = "Banani, Dhaka, Bangladesh - 400";
    public static final String HOTEL_PHONE     = "01902931445";
    public static final String HOTEL_EMAIL     = "info@grandazure.com";

    // ──────────────────────────────────────────────────────
    // Database Configuration
    // Change these to match YOUR local MySQL setup
    // ──────────────────────────────────────────────────────
    public static final String DB_HOST         = "localhost";
    public static final int    DB_PORT         = 3306;
    public static final String DB_NAME         = "hotel_db";
    public static final String DB_USER         = "root";
    public static final String DB_PASSWORD     = "";
    public static final String DB_URL          =
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
        + "?useSSL=false&serverTimezone=Asia/Dhaka&allowPublicKeyRetrieval=true"
        + "&characterEncoding=UTF-8&autoReconnect=true";

    // Connection pool settings
    public static final int    DB_POOL_MIN     = 2;
    public static final int    DB_POOL_MAX     = 10;
    public static final int    DB_TIMEOUT_SEC  = 30;

    // ──────────────────────────────────────────────────────
    // Security
    // ──────────────────────────────────────────────────────
    public static final int    BCRYPT_ROUNDS   = 12;
    public static final int    SESSION_TIMEOUT = 30; // minutes
    public static final int    MAX_LOGIN_TRIES = 5;

    // ──────────────────────────────────────────────────────
    // Business Rules
    // ──────────────────────────────────────────────────────
    public static final double DEFAULT_DISCOUNT_PCT = 0.0;
    public static final int    INVOICE_DUE_DAYS     = 1;
    public static final String INVOICE_PREFIX       = "INV";
    public static final String CURRENCY_SYMBOL      = "৳";

    // ──────────────────────────────────────────────────────
    // UI Constants
    // ──────────────────────────────────────────────────────
    public static final int    WINDOW_WIDTH    = 1280;
    public static final int    WINDOW_HEIGHT   = 800;
    public static final int    MIN_WIDTH       = 1024;
    public static final int    MIN_HEIGHT      = 680;
    public static final int    SIDEBAR_WIDTH   = 220;
    public static final String DEFAULT_THEME   = "dark"; // "dark" or "light"
    public static final int    TABLE_ROW_HEIGHT = 36;
    public static final int    TOAST_DURATION  = 3000; // ms

    // ──────────────────────────────────────────────────────
    // File Paths
    // ──────────────────────────────────────────────────────
    public static final String PDF_OUTPUT_DIR   = System.getProperty("user.home") + "/HotelMS_Invoices/";
    public static final String LOG_DIR          = System.getProperty("user.home") + "/HotelMS_Logs/";
    public static final String BACKUP_DIR       = System.getProperty("user.home") + "/HotelMS_Backups/";
}
