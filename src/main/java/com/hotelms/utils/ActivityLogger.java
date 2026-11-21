package com.hotelms.utils;

import com.hotelms.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ActivityLogger {

    private static final Logger LOGGER = Logger.getLogger(ActivityLogger.class.getName());

    private ActivityLogger() {}

    // Module constants — keep consistent
    public static final String MODULE_AUTH     = "AUTH";
    public static final String MODULE_ROOM     = "ROOM";
    public static final String MODULE_GUEST    = "GUEST";
    public static final String MODULE_BOOKING  = "BOOKING";
    public static final String MODULE_BILLING  = "BILLING";
    public static final String MODULE_PAYMENT  = "PAYMENT";
    public static final String MODULE_STAFF    = "STAFF";
    public static final String MODULE_SERVICE  = "SERVICE";

    /**
     * Logs an activity asynchronously so it never blocks the UI thread.
     *
     * @param userId      ID of the user performing the action
     * @param action      Short action verb, e.g. "Created booking #42"
     * @param module      Module constant from above
     * @param entityId    ID of the affected record (nullable)
     * @param description Longer description (nullable)
     */
    public static void log(int userId, String action, String module, Integer entityId, String description) {
        // Run in background thread so DB write never freezes the UI
        Thread.ofVirtual().start(() -> {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO activity_logs (user_id, action, module, entity_id, description) VALUES (?,?,?,?,?)");
                ps.setInt(1, userId);
                ps.setString(2, action);
                ps.setString(3, module);
                if (entityId != null) ps.setInt(4, entityId); else ps.setNull(4, java.sql.Types.INTEGER);
                ps.setString(5, description);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to write activity log", e);
            } finally {
                DatabaseConnection.getInstance().releaseConnection(conn);
            }
        });
    }

    
    public static void log(int userId, String action, String module) {
        log(userId, action, module, null, null);
    }
}
