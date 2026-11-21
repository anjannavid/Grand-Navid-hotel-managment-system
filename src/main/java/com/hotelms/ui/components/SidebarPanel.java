package com.hotelms.ui.components;

import com.hotelms.utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {

    public static final String PAGE_DASHBOARD   = "DASHBOARD";
    public static final String PAGE_ROOMS       = "ROOMS";
    public static final String PAGE_BOOKINGS    = "BOOKINGS";
    public static final String PAGE_GUESTS      = "GUESTS";
    public static final String PAGE_CHECKINOUT  = "CHECKINOUT";
    public static final String PAGE_BILLING     = "BILLING";
    public static final String PAGE_SERVICES    = "SERVICES";
    public static final String PAGE_ANALYTICS   = "ANALYTICS";
    public static final String PAGE_STAFF       = "STAFF";
    public static final String PAGE_ACTIVITYLOG = "ACTIVITYLOG";

    private String activePage = PAGE_DASHBOARD;
    private final List<NavItem> navItems = new ArrayList<>();
    private Consumer<String> onNavigate;
    private Consumer<Void> onLogout;
    private JLabel userNameLabel;
    private JLabel userRoleLabel;
    private JLabel logoutBtn;
    private JPanel logoTextBox;
    private final List<JLabel> sectionHeaders = new ArrayList<>();
    private boolean isCollapsed = false;

    public SidebarPanel() {
        setBackground(ThemeManager.SIDEBAR_BG);
        setLayout(new BorderLayout());
        buildSidebar();
    }

    public void setOnNavigate(Consumer<String> callback) { this.onNavigate = callback; }
    public void setOnLogout(Consumer<Void> callback)     { this.onLogout = callback; }

    private void buildSidebar() {
        ScrollablePanel inner = new ScrollablePanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        inner.add(buildLogo());
        inner.add(createSeparator());
        inner.add(Box.createVerticalStrut(6));

        inner.add(sectionHeader("Main"));
        addNavItem(inner, PAGE_DASHBOARD,  "🏠", "Dashboard");
        addNavItem(inner, PAGE_ROOMS,      "🛏", "Room Management");
        addNavItem(inner, PAGE_BOOKINGS,   "📅", "Bookings");
        addNavItem(inner, PAGE_GUESTS,     "👥", "Guests");
        inner.add(Box.createVerticalStrut(6));

        inner.add(createSeparator());
        inner.add(Box.createVerticalStrut(6));
        inner.add(sectionHeader("Operations"));
        addNavItem(inner, PAGE_CHECKINOUT, "🚪", "Check-in / Check-out");
        addNavItem(inner, PAGE_BILLING,    "🧾", "Billing & Invoice");
        addNavItem(inner, PAGE_SERVICES,   "🍽",  "Room Services");
        inner.add(Box.createVerticalStrut(6));

        if (SessionManager.getInstance().isReceptionist()) {
            inner.add(createSeparator());
            inner.add(Box.createVerticalStrut(6));
            inner.add(sectionHeader("Reports & Admin"));
            addNavItem(inner, PAGE_ANALYTICS,   "📊", "Analytics");
            if (SessionManager.getInstance().isAdmin()) {
                addNavItem(inner, PAGE_STAFF,       "👤", "Staff Management");
                addNavItem(inner, PAGE_ACTIVITYLOG, "📋", "Activity Log");
            }
        }

        inner.add(Box.createVerticalGlue());

        inner.add(createSeparator());
        inner.add(buildUserFooter());

        // Wrap in a scroll pane in case screen is short
        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(ThemeManager.SIDEBAR_BG);
        scroll.setBackground(ThemeManager.SIDEBAR_BG);

        add(scroll, BorderLayout.CENTER);

        // Right border separator
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER));
    }

    private JPanel buildLogo() {
        JPanel logo = new JPanel(new BorderLayout(12, 0));
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 18, 16, 18));
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Icon box
        JPanel iconBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.ACCENT_PRIMARY);
                g2.fillRoundRect(0, 0, 36, 36, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "🏨";
                g2.drawString(icon, (36 - fm.stringWidth(icon)) / 2, 26);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
            @Override public Dimension getPreferredSize() { return new Dimension(36, 36); }
            @Override public Dimension getMinimumSize() { return getPreferredSize(); }
            @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        };

        logoTextBox = new JPanel();
        logoTextBox.setLayout(new BoxLayout(logoTextBox, BoxLayout.Y_AXIS));
        logoTextBox.setOpaque(false);

        JLabel appName = new JLabel("HotelOS");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appName.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel version = new JLabel("v1.0  •  Management Suite");
        version.setFont(ThemeManager.FONT_SMALL);
        version.setForeground(ThemeManager.TEXT_MUTED);

        logoTextBox.add(appName);
        logoTextBox.add(Box.createVerticalStrut(2));
        logoTextBox.add(version);

        logo.add(iconBox, BorderLayout.WEST);
        logo.add(logoTextBox, BorderLayout.CENTER);
        return logo;
    }

    private void addNavItem(JPanel container, String page, String emoji, String label) {
        NavItem item = new NavItem(page, emoji, label);
        navItems.add(item);
        container.add(item);
    }

    private JPanel buildUserFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.ACCENT_PRIMARY.darker());
                g2.fillOval(0, 0, 34, 34);
                g2.setColor(ThemeManager.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String initials = SessionManager.getInstance().isLoggedIn()
                    ? SessionManager.getInstance().getCurrentUser().getInitials() : "?";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials,
                    (34 - fm.stringWidth(initials)) / 2,
                    (34 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
            @Override public Dimension getPreferredSize() { return new Dimension(34, 34); }
            @Override public Dimension getMinimumSize() { return getPreferredSize(); }
            @Override public Dimension getMaximumSize() { return getPreferredSize(); }
        };

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        String name = SessionManager.getInstance().isLoggedIn()
            ? SessionManager.getInstance().getCurrentUser().getFullName() : "Unknown";
        String role = SessionManager.getInstance().isLoggedIn()
            ? SessionManager.getInstance().getCurrentUser().getRole().getDisplayName() : "";

        userNameLabel = new JLabel(name.length() > 16 ? name.substring(0, 16) + "…" : name);
        userNameLabel.setFont(ThemeManager.FONT_BOLD);
        userNameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        userRoleLabel = new JLabel(role);
        userRoleLabel.setFont(ThemeManager.FONT_SMALL);
        userRoleLabel.setForeground(ThemeManager.ACCENT_SUCCESS);

        info.add(userNameLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(userRoleLabel);

        // Logout button
        logoutBtn = new JLabel("↩");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoutBtn.setForeground(ThemeManager.TEXT_MUTED);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setToolTipText("Logout");
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (onLogout != null) onLogout.accept(null);
            }
            @Override public void mouseEntered(MouseEvent e) { logoutBtn.setForeground(ThemeManager.ACCENT_DANGER); }
            @Override public void mouseExited(MouseEvent e)  { logoutBtn.setForeground(ThemeManager.TEXT_MUTED); }
        });

        footer.add(avatar, BorderLayout.WEST);
        footer.add(info,   BorderLayout.CENTER);
        footer.add(logoutBtn, BorderLayout.EAST);
        return footer;
    }

    private JPanel createSeparator() {
        JPanel sepPanel = new JPanel(new BorderLayout());
        sepPanel.setOpaque(false);
        sepPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        sepPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sepPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel line = new JPanel();
        line.setBackground(ThemeManager.BORDER);
        line.setPreferredSize(new Dimension(0, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sepPanel.add(line, BorderLayout.CENTER);

        return sepPanel;
    }

    private JLabel sectionHeader(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(ThemeManager.TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 20, 6, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        sectionHeaders.add(lbl);
        return lbl;
    }

    /** Programmatically activate a page */
    public void setActivePage(String page) {
        this.activePage = page;
        navItems.forEach(item -> item.setActive(item.page.equals(page)));
        repaint();
    }

    public void setCollapsed(boolean collapsed) {
        if (this.isCollapsed == collapsed) return;
        this.isCollapsed = collapsed;
        
        logoTextBox.setVisible(!collapsed);
        userNameLabel.setVisible(!collapsed);
        userRoleLabel.setVisible(!collapsed);
        logoutBtn.setVisible(!collapsed);
        sectionHeaders.forEach(h -> h.setVisible(!collapsed));
        
        navItems.forEach(item -> item.setCollapsed(collapsed));
        
        revalidate();
        repaint();
    }

    // NavItem — Clean left-aligned nav button
    private class NavItem extends JPanel {
        final String page;
        private boolean active = false;
        private boolean hovered = false;
        private final JLabel emojiLbl;
        private final JLabel textLbl;
        private final JPanel content;

        NavItem(String page, String emoji, String label) {
            this.page = page;

            setOpaque(false);
            // Let layout manager decide size based on container and contents
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(LEFT_ALIGNMENT);
            setLayout(new BorderLayout());

            // Single content panel using BorderLayout for tight alignment
            content = new JPanel(new BorderLayout(10, 0));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 14));

            emojiLbl = new JLabel(emoji);
            emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            emojiLbl.setPreferredSize(new Dimension(20, 40));
            emojiLbl.setHorizontalAlignment(SwingConstants.CENTER);

            textLbl = new JLabel(label);
            textLbl.setFont(ThemeManager.FONT_BODY);
            textLbl.setForeground(ThemeManager.TEXT_SECONDARY);

            content.add(emojiLbl, BorderLayout.WEST);
            content.add(textLbl, BorderLayout.CENTER);
            add(content, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    setActivePage(page);
                    if (onNavigate != null) onNavigate.accept(page);
                }
                @Override public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    if (!active) textLbl.setForeground(ThemeManager.TEXT_PRIMARY);
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    hovered = false;
                    if (!active) textLbl.setForeground(ThemeManager.TEXT_SECONDARY);
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            textLbl.setForeground(active ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_SECONDARY);
            textLbl.setFont(active
                ? new Font("Segoe UI", Font.BOLD, 13)
                : ThemeManager.FONT_BODY);
            repaint();
        }

        void setCollapsed(boolean collapsed) {
            textLbl.setVisible(!collapsed);
            content.remove(emojiLbl);
            if (collapsed) {
                content.setBorder(BorderFactory.createEmptyBorder());
                content.add(emojiLbl, BorderLayout.CENTER);
            } else {
                content.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 14));
                content.add(emojiLbl, BorderLayout.WEST);
            }
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(new Color(ThemeManager.ACCENT_PRIMARY.getRed(),
                    ThemeManager.ACCENT_PRIMARY.getGreen(),
                    ThemeManager.ACCENT_PRIMARY.getBlue(), 25));
                g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 8, 8);
                // Left accent bar
                g2.setColor(ThemeManager.ACCENT_PRIMARY);
                g2.fillRoundRect(4, 8, 3, getHeight() - 16, 3, 3);
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 40; }
        @Override
        public boolean getScrollableTracksViewportWidth() { return true; }
        @Override
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
