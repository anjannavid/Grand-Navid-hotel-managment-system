package com.hotelms.ui.components;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class StatusBadge extends JPanel implements TableCellRenderer {

    private final JLabel textLabel;
    private Color badgeBg = ThemeManager.CHIP_NEUTRAL_BG;
    private Color cellBg  = ThemeManager.BG_PRIMARY;

    public StatusBadge() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        textLabel = new JLabel();
        textLabel.setOpaque(false);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        add(textLabel);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value == null) { textLabel.setText("—"); return this; }

        String text = value.toString();

        // Determine colors by string value (works for any status enum)
        badgeBg = switch (text.toUpperCase().replace(" ", "_")) {
            case "AVAILABLE", "PAID", "DELIVERED"  -> ThemeManager.CHIP_SUCCESS_BG;
            case "OCCUPIED",  "CHECKED_IN"          -> ThemeManager.CHIP_DANGER_BG;
            case "CONFIRMED", "PARTIAL", "PENDING"  -> ThemeManager.CHIP_WARNING_BG;
            case "MAINTENANCE", "CANCELLED", "NO_SHOW", "REFUNDED" -> ThemeManager.CHIP_NEUTRAL_BG;
            case "RESERVED",  "IN_PROGRESS"         -> ThemeManager.CHIP_INFO_BG;
            case "CHECKED_OUT"                      -> ThemeManager.CHIP_SUCCESS_BG;
            default -> ThemeManager.CHIP_NEUTRAL_BG;
        };

        Color fg = switch (text.toUpperCase().replace(" ", "_")) {
            case "AVAILABLE", "PAID", "DELIVERED", "CHECKED_OUT" -> ThemeManager.ACCENT_SUCCESS;
            case "OCCUPIED",  "CHECKED_IN", "CANCELLED"          -> ThemeManager.ACCENT_DANGER;
            case "CONFIRMED", "PARTIAL", "PENDING"               -> ThemeManager.ACCENT_WARNING;
            case "RESERVED",  "IN_PROGRESS"                      -> ThemeManager.ACCENT_INFO;
            default -> ThemeManager.TEXT_SECONDARY;
        };

        // Display friendly name
        String display = text.replace("_", " ");
        if (text.equals("CHECKED_IN"))  display = "Checked In";
        if (text.equals("CHECKED_OUT")) display = "Checked Out";
        if (text.equals("NO_SHOW"))     display = "No Show";
        if (text.equals("NET_BANKING")) display = "Net Banking";

        textLabel.setText(display);
        textLabel.setForeground(fg);

        // Track the cell background for painting behind the pill
        if (isSelected) {
            cellBg = ThemeManager.BG_SELECTED;
        } else if (row % 2 == 0) {
            cellBg = ThemeManager.BG_PRIMARY;
        } else {
            cellBg = ThemeManager.BG_SECONDARY;
        }

        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill cell background first
        g2.setColor(cellBg);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw the pill badge centered within the cell
        FontMetrics fm = g2.getFontMetrics(textLabel.getFont());
        int textWidth = fm.stringWidth(textLabel.getText());
        int pillW = textWidth + 20;
        int pillH = 22;
        int x = (getWidth() - pillW) / 2;
        int y = (getHeight() - pillH) / 2;

        g2.setColor(badgeBg);
        g2.fillRoundRect(x, y, pillW, pillH, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
