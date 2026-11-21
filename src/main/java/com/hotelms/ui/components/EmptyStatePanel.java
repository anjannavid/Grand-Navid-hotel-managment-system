package com.hotelms.ui.components;

import javax.swing.*;
import java.awt.*;

public class EmptyStatePanel extends JPanel {

    public EmptyStatePanel(String emoji, String title, String subtitle) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        // Emoji Icon
        JLabel iconLbl = new JLabel(emoji);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeManager.FONT_H2);
        titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLbl.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));

        // Subtitle
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(ThemeManager.FONT_BODY);
        subLbl.setForeground(ThemeManager.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        add(Box.createVerticalGlue());
        add(iconLbl);
        add(titleLbl);
        add(subLbl);
        add(Box.createVerticalGlue());
    }
}
