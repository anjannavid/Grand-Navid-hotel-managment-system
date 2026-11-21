package com.hotelms.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ToggleSwitch extends JPanel {

    private boolean selected = false;
    private int thumbPosition = 2;
    private final int trackWidth = 40;
    private final int trackHeight = 22;
    private Timer animationTimer;

    public ToggleSwitch() {
        setOpaque(false);
        setPreferredSize(new Dimension(trackWidth, trackHeight));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(!selected);
            }
        });
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (this.selected == selected) return;
        this.selected = selected;
        
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        int targetPos = selected ? trackWidth - trackHeight + 2 : 2;
        int step = selected ? 2 : -2;

        animationTimer = new Timer(10, e -> {
            thumbPosition += step;
            if ((selected && thumbPosition >= targetPos) || (!selected && thumbPosition <= targetPos)) {
                thumbPosition = targetPos;
                animationTimer.stop();
            }
            repaint();
        });
        animationTimer.start();
        
        firePropertyChange("selected", !selected, selected);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Track
        if (selected) {
            g2.setColor(ThemeManager.ACCENT_SUCCESS);
        } else {
            g2.setColor(ThemeManager.BG_TERTIARY);
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, trackWidth, trackHeight, trackHeight, trackHeight));

        // Outline
        g2.setColor(ThemeManager.BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, trackWidth - 1, trackHeight - 1, trackHeight, trackHeight));

        // Thumb
        int thumbSize = trackHeight - 4;
        g2.setColor(ThemeManager.TEXT_INVERSE);
        g2.fillOval(thumbPosition, 2, thumbSize, thumbSize);
        
        // Thumb Shadow
        g2.setColor(new Color(0, 0, 0, 30));
        g2.drawOval(thumbPosition, 2, thumbSize, thumbSize);

        g2.dispose();
    }
}
