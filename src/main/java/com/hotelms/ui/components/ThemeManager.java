package com.hotelms.ui.components;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import java.awt.*;

public final class ThemeManager {

    // Design Tokens — Dark Theme (Default)
    public static final Color BG_PRIMARY       = new Color(0x1E1F22);
    public static final Color BG_SECONDARY     = new Color(0x2B2D30);
    public static final Color BG_TERTIARY      = new Color(0x3C3F41);
    public static final Color BG_CARD          = new Color(0x2B2D30);
    public static final Color BG_HOVER         = new Color(0x383B3D);
    public static final Color BG_SELECTED      = new Color(0x2D5A9E);

    public static final Color ACCENT_PRIMARY   = new Color(0x4F7BE8); // Blue
    public static final Color ACCENT_SUCCESS   = new Color(0x5EAF80); // Green
    public static final Color ACCENT_WARNING   = new Color(0xE0A44C); // Amber
    public static final Color ACCENT_DANGER    = new Color(0xE06C75); // Red
    public static final Color ACCENT_INFO      = new Color(0x56B6C2); // Cyan

    public static final Color TEXT_PRIMARY     = new Color(0xDFE1E5);
    public static final Color TEXT_SECONDARY   = new Color(0x9199A1);
    public static final Color TEXT_MUTED       = new Color(0x6B737B);
    public static final Color TEXT_INVERSE     = new Color(0xFFFFFF);

    public static final Color BORDER           = new Color(0x43454A);
    public static final Color BORDER_LIGHT     = new Color(0x393B3F);

    public static final Color SIDEBAR_BG       = new Color(0x1C1D20);
    public static final Color SIDEBAR_ITEM_ACTIVE = new Color(0x2D5A9E);

    // Chip/badge backgrounds
    public static final Color CHIP_SUCCESS_BG  = new Color(0x1A3D2B);
    public static final Color CHIP_DANGER_BG   = new Color(0x3D1A1A);
    public static final Color CHIP_WARNING_BG  = new Color(0x3D2E0A);
    public static final Color CHIP_INFO_BG     = new Color(0x0A2D3D);
    public static final Color CHIP_NEUTRAL_BG  = new Color(0x2D2F33);

    // Chart colors
    public static final Color[] CHART_COLORS = {
        ACCENT_PRIMARY, ACCENT_SUCCESS, ACCENT_WARNING,
        ACCENT_DANGER, ACCENT_INFO, new Color(0xC678DD)
    };

    // Typography
    public static final Font FONT_H1       = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_H2       = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_H3       = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MONO     = new Font("JetBrains Mono", Font.PLAIN, 12);

    // Spacing & Sizing
    public static final int PADDING_SM     = 8;
    public static final int PADDING_MD     = 14;
    public static final int PADDING_LG     = 20;
    public static final int PADDING_XL     = 28;
    public static final int RADIUS         = 8;
    public static final int RADIUS_SM      = 5;
    public static final int BUTTON_HEIGHT  = 36;

    private ThemeManager() {}

    // Initialization

    /** Call this BEFORE creating any Swing component */
    public static void applyDarkTheme() {
        try {
            FlatMacDarkLaf.setup();
        } catch (Exception e) {
            // Fallback to standard dark
            try { FlatDarkLaf.setup(); } catch (Exception ex) { ex.printStackTrace(); }
        }
        applyUIDefaults(true);
    }

    public static void applyLightTheme() {
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        applyUIDefaults(false);
    }

    private static void applyUIDefaults(boolean dark) {
        Color bg    = dark ? BG_SECONDARY   : new Color(0xF5F5F5);
        Color panel = dark ? BG_PRIMARY     : new Color(0xFFFFFF);
        Color text  = dark ? TEXT_PRIMARY   : new Color(0x2B2D30);
        Color border= dark ? BORDER         : new Color(0xD0D0D0);

        UIManager.put("Panel.background",            bg);
        UIManager.put("ScrollPane.background",       bg);
        UIManager.put("Table.background",            bg);
        UIManager.put("Table.alternateRowColor",     dark ? BG_TERTIARY : new Color(0xF9F9F9));
        UIManager.put("Table.gridColor",             border);
        UIManager.put("Table.foreground",            text);
        UIManager.put("Table.selectionBackground",   ACCENT_PRIMARY);
        UIManager.put("Table.selectionForeground",   TEXT_INVERSE);
        UIManager.put("TableHeader.background",      dark ? BG_TERTIARY : new Color(0xECECEC));
        UIManager.put("TableHeader.foreground",      text);
        UIManager.put("TableHeader.font",            FONT_BOLD);
        UIManager.put("Table.rowHeight",             36);

        UIManager.put("Button.arc",                  8);
        UIManager.put("Component.arc",               8);
        UIManager.put("TextComponent.arc",           6);
        UIManager.put("ScrollBar.width",             8);
        UIManager.put("ScrollBar.thumbArc",          999);

        UIManager.put("defaultFont",                 FONT_BODY);

        // Don't override Button.background globally — it forces ALL buttons blue
        // Instead, style buttons via factory methods (primaryButton, etc.)
        UIManager.put("Button.font",                 FONT_BOLD);
        UIManager.put("Button.arc",                  8);

        UIManager.put("ComboBox.background",         dark ? BG_TERTIARY : Color.WHITE);
        UIManager.put("ComboBox.foreground",         dark ? TEXT_PRIMARY : new Color(0x2B2D30));
        UIManager.put("TextField.background",        dark ? BG_TERTIARY : Color.WHITE);
        UIManager.put("PasswordField.background",    dark ? BG_TERTIARY : Color.WHITE);
        UIManager.put("TextArea.background",         dark ? BG_TERTIARY : Color.WHITE);

        // Checkbox / RadioButton
        UIManager.put("CheckBox.foreground",         text);
        UIManager.put("RadioButton.foreground",      text);

        // Dialog backgrounds
        UIManager.put("OptionPane.background",       dark ? BG_SECONDARY : Color.WHITE);
        UIManager.put("OptionPane.messageForeground", text);
        UIManager.put("Panel.background",            bg);

        UIManager.put("ToolTip.background",          dark ? BG_TERTIARY : new Color(0x424242));
        UIManager.put("ToolTip.foreground",          TEXT_INVERSE);
        UIManager.put("ToolTip.border",              BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    // ══════════════════════════════════════════════════════
    // BUTTON FACTORIES — Consistent sizing & style
    // ══════════════════════════════════════════════════════

    /** A primary action button (blue filled) */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT_PRIMARY);
        btn.setForeground(TEXT_INVERSE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, BUTTON_HEIGHT));
        return btn;
    }

    /** A danger/destructive button (red) */
    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(ACCENT_DANGER);
        return btn;
    }

    /** A success button (green) */
    public static JButton successButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(ACCENT_SUCCESS);
        return btn;
    }

    /** A warning button (amber) */
    public static JButton warningButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(ACCENT_WARNING);
        btn.setForeground(new Color(0x1E1F22));
        return btn;
    }

    /** An outlined/ghost button */
    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, BUTTON_HEIGHT));
        return btn;
    }

    // ══════════════════════════════════════════════════════
    // INPUT FACTORIES
    // ══════════════════════════════════════════════════════

    /** Styled text field with placeholder */
    public static JTextField textField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, BUTTON_HEIGHT));
        return tf;
    }

    /** Styled password field with placeholder */
    public static JPasswordField passwordField(String placeholder) {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.putClientProperty("JTextField.placeholderText", placeholder);
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, BUTTON_HEIGHT));
        return pf;
    }

    /** Styled combo box */
    public static <T> JComboBox<T> styledComboBox(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        box.setFont(FONT_BODY);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, BUTTON_HEIGHT));
        return box;
    }

    /** Section title label (small uppercase muted) */
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    /** Form field label */
    public static JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    // ══════════════════════════════════════════════════════
    // LAYOUT FACTORIES — Consistent page structure
    // ══════════════════════════════════════════════════════

    /**
     * Creates a standard page header with title, subtitle, and optional action button.
     *
     *   Title                          [Action Button]
     *   Subtitle text
     */
    public static JPanel createHeaderPanel(String title, String subtitle, JButton actionButton) {
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, PADDING_MD, 0));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_H1);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(titleLabel);

        titleBox.add(Box.createVerticalStrut(4));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FONT_BODY);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBox.add(subtitleLabel);

        header.add(titleBox, BorderLayout.CENTER);

        if (actionButton != null) {
            JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
            btnWrapper.setOpaque(false);
            btnWrapper.add(actionButton);
            header.add(btnWrapper, BorderLayout.EAST);
        }

        return header;
    }

    /** Creates a header without an action button */
    public static JPanel createHeaderPanel(String title, String subtitle) {
        return createHeaderPanel(title, subtitle, null);
    }

    /**
     * Creates a styled card panel with rounded border and proper background.
     */
    public static JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS * 2, RADIUS * 2);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS * 2, RADIUS * 2);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() { return false; }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING_MD, PADDING_MD, PADDING_MD, PADDING_MD));
        return panel;
    }

    /**
     * Creates a horizontal toolbar with search field and optional filter components.
     */
    public static JPanel createToolbar(JComponent... components) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SM, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(PADDING_SM, 0, PADDING_SM, 0));
        for (JComponent comp : components) {
            toolbar.add(comp);
        }
        return toolbar;
    }

    /**
     * Standard content panel padding for all pages.
     */
    public static JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(
            PADDING_LG, PADDING_LG, PADDING_LG, PADDING_LG));
        return panel;
    }

    /**
     * Creates a bottom action bar with right-aligned buttons.
     */
    public static JPanel createBottomBar(JButton... buttons) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SM, PADDING_SM));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        for (JButton btn : buttons) {
            bar.add(btn);
        }
        return bar;
    }

    /**
     * Creates a styled JDialog.
     */
    public static JDialog createStyledDialog(JFrame parent, String title, boolean modal) {
        JDialog dialog = new JDialog(parent, title, modal);
        dialog.getContentPane().setBackground(BG_SECONDARY);
        return dialog;
    }

    /**
     * Creates a wrapper panel for a form field (label + input).
     */
    public static JPanel formFieldPanel(String labelText, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(formLabel(labelText), BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }
}
