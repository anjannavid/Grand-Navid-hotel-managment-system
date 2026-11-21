package com.hotelms;

import com.hotelms.database.DatabaseInitializer;
import com.hotelms.service.AuthService;
import com.hotelms.ui.LoginFrame;
import com.hotelms.ui.components.ThemeManager;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // 1. Apply theme BEFORE any Swing components are created
        ThemeManager.applyDarkTheme();

        // 2. Initialize database in the background, then show UI
        SwingUtilities.invokeLater(() -> {
            try {
                // Show a splash-like progress while DB initializes
                JFrame splash = createSplashFrame();
                splash.setVisible(true);

                // Initialize DB on a background thread
                SwingWorker<Void, Void> dbWorker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            DatabaseInitializer.initialize();
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
                            throw e;
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        splash.dispose();
                        try {
                            get(); // Check for exceptions
                            // 3. Show login
                            AuthService authService = new AuthService();
                            LoginFrame loginFrame = new LoginFrame(authService);
                            loginFrame.setVisible(true);
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Failed to start application", e);
                            JOptionPane.showMessageDialog(null,
                                "Failed to connect to the database.\n\n" +
                                "Please ensure:\n" +
                                "1. MySQL is running on localhost:3306\n" +
                                "2. Database 'hotel_db' exists (CREATE DATABASE hotel_db;)\n" +
                                "3. Update DB credentials in AppConfig.java\n\n" +
                                "Error: " + e.getMessage(),
                                "Database Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                            System.exit(1);
                        }
                    }
                };
                dbWorker.execute();

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Application startup failed", e);
                JOptionPane.showMessageDialog(null,
                    "Application failed to start: " + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * Creates a minimal splash screen shown while the DB initializes.
     */
    private static JFrame createSplashFrame() {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setSize(360, 160);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        JPanel panel = new JPanel();
        panel.setBackground(ThemeManager.BG_SECONDARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BORDER, 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        JLabel title = new JLabel("🏨  HotelOS");
        title.setFont(ThemeManager.FONT_H2);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel status = new JLabel("Initializing database...");
        status.setFont(ThemeManager.FONT_BODY);
        status.setForeground(ThemeManager.TEXT_SECONDARY);
        status.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setMaximumSize(new java.awt.Dimension(280, 4));
        progress.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        panel.add(status);
        panel.add(Box.createVerticalStrut(14));
        panel.add(progress);

        frame.setContentPane(panel);
        return frame;
    }
}
