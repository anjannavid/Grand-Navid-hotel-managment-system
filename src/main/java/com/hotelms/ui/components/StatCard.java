package com.hotelms.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class StatCard extends JPanel {

    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final Color accentColor;
    private boolean hovered = false;

    public StatCard(String title, String value, String subtitle, Color accentColor, String icon) {
        this.accentColor = accentColor;

        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Content panel with proper padding
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_MD, ThemeManager.PADDING_MD + 6,
            ThemeManager.PADDING_MD, ThemeManager.PADDING_MD));

        // Top row: title + icon
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_SMALL);
        titleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        topRow.add(titleLabel, BorderLayout.WEST);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconLabel.setForeground(accentColor);
        topRow.add(iconLabel, BorderLayout.EAST);

        // Value
        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));

        // Subtitle / delta
        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(ThemeManager.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(topRow);
        content.add(valueLabel);
        content.add(subtitleLabel);

        add(content, BorderLayout.CENTER);

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    /** Update the displayed value (call from EDT) */
    public void setValue(String value) {
        valueLabel.setText(value);
        revalidate();
        repaint();
    }

    /** Update the subtitle (e.g., percentage or trend) */
    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int r = ThemeManager.RADIUS * 2;

        // Card background with rounded corners — slightly lighter on hover
        g2.setColor(hovered ? ThemeManager.BG_HOVER : ThemeManager.BG_CARD);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));

        // Border — accent tint on hover
        g2.setColor(hovered
            ? new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60)
            : ThemeManager.BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, r, r));

        // Left accent stripe — clipped to card shape so it doesn't overflow
        Shape clip = g2.getClip();
        g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
        g2.setColor(accentColor);
        g2.fillRect(0, 0, 4, getHeight());
        g2.setClip(clip);

        g2.dispose();
    }

    @Override
    public boolean isOpaque() { return false; }
}
