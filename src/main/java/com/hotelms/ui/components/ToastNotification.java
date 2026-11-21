package com.hotelms.ui.components;

import com.hotelms.config.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ToastNotification extends JWindow {

    public enum Type {
        SUCCESS(ThemeManager.ACCENT_SUCCESS, "✓"),
        ERROR  (ThemeManager.ACCENT_DANGER,  "✕"),
        WARNING(ThemeManager.ACCENT_WARNING, "⚠"),
        INFO   (ThemeManager.ACCENT_INFO,    "ℹ");

        final Color color;
        final String icon;
        Type(Color c, String i) { this.color = c; this.icon = i; }
    }

    private float opacity = 0f;
    private final boolean supportsTranslucency;

    private ToastNotification(JFrame owner, String message, Type type) {
        super(owner);
        this.supportsTranslucency = checkTranslucencySupport();

        JPanel content = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Left color stripe
                g2.setColor(type.color);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 20));

        // Icon
        JLabel iconLbl = new JLabel(type.icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        iconLbl.setForeground(type.color);
        content.add(iconLbl, BorderLayout.WEST);

        // Message
        JLabel msgLbl = new JLabel(message);
        msgLbl.setFont(ThemeManager.FONT_BODY);
        msgLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        content.add(msgLbl, BorderLayout.CENTER);

        // Border
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 20)
        ));

        setContentPane(content);
        setBackground(new Color(0, 0, 0, 0));
        pack();
        setSize(Math.max(320, getWidth()), getHeight());

        // Position: bottom-right of owner
        if (owner != null) {
            int x = owner.getX() + owner.getWidth()  - getWidth()  - 24;
            int y = owner.getY() + owner.getHeight() - getHeight() - 60;
            setLocation(x, y);
        }

        if (supportsTranslucency) {
            try { setOpacity(0f); } catch (Exception ignored) {}
        }
        setVisible(true);
        startFadeIn();
    }

    private boolean checkTranslucencySupport() {
        try {
            return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
        } catch (Exception e) {
            return false;
        }
    }

    private void startFadeIn() {
        if (!supportsTranslucency) {
            // No fade, just show and auto-dismiss
            Timer hold = new Timer(AppConfig.TOAST_DURATION, ev -> dispose());
            hold.setRepeats(false);
            hold.start();
            return;
        }
        Timer fadeIn = new Timer(20, null);
        fadeIn.addActionListener(e -> {
            opacity = Math.min(1f, opacity + 0.08f);
            try { setOpacity(opacity); } catch (Exception ignored) {}
            if (opacity >= 1f) {
                fadeIn.stop();
                Timer hold = new Timer(AppConfig.TOAST_DURATION, ev -> startFadeOut());
                hold.setRepeats(false);
                hold.start();
            }
        });
        fadeIn.start();
    }

    private void startFadeOut() {
        Timer fadeOut = new Timer(20, null);
        fadeOut.addActionListener(e -> {
            opacity = Math.max(0f, opacity - 0.06f);
            try { setOpacity(opacity); } catch (Exception ignored) {}
            if (opacity <= 0f) {
                fadeOut.stop();
                dispose();
            }
        });
        fadeOut.start();
    }

    /** Shows a toast. Must be called from the Event Dispatch Thread. */
    public static void show(JFrame owner, String message, Type type) {
        SwingUtilities.invokeLater(() -> new ToastNotification(owner, message, type));
    }

    public static void success(JFrame owner, String message) { show(owner, message, Type.SUCCESS); }
    public static void error(JFrame owner, String message)   { show(owner, message, Type.ERROR); }
    public static void warning(JFrame owner, String message) { show(owner, message, Type.WARNING); }
    public static void info(JFrame owner, String message)    { show(owner, message, Type.INFO); }
}
