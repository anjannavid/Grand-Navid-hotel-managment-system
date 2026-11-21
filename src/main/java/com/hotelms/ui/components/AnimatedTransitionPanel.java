package com.hotelms.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class AnimatedTransitionPanel extends JPanel {

    private final Map<String, Component> components = new HashMap<>();
    private Component currentComponent;
    
    private BufferedImage imageOld;
    private BufferedImage imageNew;
    private int slideOffset = 0;
    private boolean isAnimating = false;
    private Timer timer;

    public AnimatedTransitionPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    public void addPanel(Component comp, String name) {
        components.put(name, comp);
        if (currentComponent == null) {
            currentComponent = comp;
            add(comp, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    public void showPanel(String name) {
        if (isAnimating || !components.containsKey(name)) return;
        
        Component nextComponent = components.get(name);
        if (nextComponent == currentComponent) return;

        // Capture current state
        imageOld = captureComponent(this);
        
        // Prepare new component off-screen to capture its state
        nextComponent.setSize(getSize());
        nextComponent.doLayout();
        imageNew = captureComponent(nextComponent);

        // Remove actual components to draw the animation
        removeAll();
        isAnimating = true;
        slideOffset = 0;

        timer = new Timer(15, e -> {
            slideOffset += getWidth() / 15; // Slide speed
            if (slideOffset >= getWidth()) {
                finishTransition(nextComponent);
            }
            repaint();
        });
        timer.start();
    }

    private void finishTransition(Component nextComponent) {
        timer.stop();
        isAnimating = false;
        imageOld = null;
        imageNew = null;
        
        currentComponent = nextComponent;
        removeAll();
        add(currentComponent, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private BufferedImage captureComponent(Component comp) {
        if (comp.getWidth() <= 0 || comp.getHeight() <= 0) return null;
        BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        comp.paint(g2);
        g2.dispose();
        return img;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isAnimating && imageOld != null && imageNew != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Old image slides to the left
            g2.drawImage(imageOld, -slideOffset, 0, null);
            // New image slides in from the right
            g2.drawImage(imageNew, getWidth() - slideOffset, 0, null);
            g2.dispose();
        }
    }
}
