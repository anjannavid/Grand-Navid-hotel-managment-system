package com.hotelms.ui;

import com.hotelms.config.AppConfig;
import com.hotelms.service.AuthService;
import com.hotelms.ui.components.SidebarPanel;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.ui.components.ToastNotification;
import com.hotelms.ui.panels.*;
import com.hotelms.utils.SessionManager;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final AuthService authService;
    private final SidebarPanel sidebar;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    // Panel references for refresh
    private DashboardPanel dashboardPanel;
    private RoomManagementPanel roomPanel;
    private GuestPanel guestPanel;
    private BookingPanel bookingPanel;
    private BillingPanel billingPanel;
    private CheckInOutPanel checkInOutPanel;
    private AnalyticsPanel analyticsPanel;
    private StaffPanel staffPanel;
    private ActivityLogPanel activityLogPanel;

    public MainFrame(AuthService authService) {
        this.authService = authService;
        this.sidebar = new SidebarPanel();
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);

        initFrame();
        buildPanels();
        wireNavigation();
        showPanel(SidebarPanel.PAGE_DASHBOARD);
    }

    // Frame Setup
    private void initFrame() {
        setTitle(AppConfig.APP_NAME + " — " + AppConfig.HOTEL_NAME);
        setSize(AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(AppConfig.MIN_WIDTH, AppConfig.MIN_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeManager.BG_PRIMARY);

        // Main layout: sidebar + content
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(ThemeManager.BG_PRIMARY);

        // Wrap sidebar in a panel to prevent layout drift
        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.add(sidebar, BorderLayout.CENTER);

        // Responsive behavior
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = getWidth();
                boolean shouldCollapse = width < 1100;
                sidebar.setCollapsed(shouldCollapse);
                
                int targetWidth = shouldCollapse ? 80 : com.formdev.flatlaf.util.UIScale.scale(260);
                sidebarWrapper.setPreferredSize(new Dimension(targetWidth, 0));
                sidebarWrapper.setMinimumSize(new Dimension(targetWidth, 0));
                sidebarWrapper.setMaximumSize(new Dimension(targetWidth, Integer.MAX_VALUE));
                
                sidebarWrapper.revalidate();
                sidebarWrapper.repaint();
            }
        });

        root.add(sidebarWrapper, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);

        contentPanel.setBackground(ThemeManager.BG_PRIMARY);

        setContentPane(root);
    }

    // Panel Construction
    private void buildPanels() {
        dashboardPanel   = new DashboardPanel(this);
        roomPanel        = new RoomManagementPanel(this);
        guestPanel       = new GuestPanel(this);
        bookingPanel     = new BookingPanel(this);
        billingPanel     = new BillingPanel(this);
        checkInOutPanel  = new CheckInOutPanel(this);
        analyticsPanel   = new AnalyticsPanel(this);
        staffPanel       = new StaffPanel(this);
        activityLogPanel = new ActivityLogPanel(this);

        contentPanel.add(dashboardPanel,   SidebarPanel.PAGE_DASHBOARD);
        contentPanel.add(roomPanel,        SidebarPanel.PAGE_ROOMS);
        contentPanel.add(bookingPanel,     SidebarPanel.PAGE_BOOKINGS);
        contentPanel.add(guestPanel,       SidebarPanel.PAGE_GUESTS);
        contentPanel.add(checkInOutPanel,  SidebarPanel.PAGE_CHECKINOUT);
        contentPanel.add(billingPanel,     SidebarPanel.PAGE_BILLING);
        contentPanel.add(analyticsPanel,   SidebarPanel.PAGE_ANALYTICS);
        contentPanel.add(staffPanel,       SidebarPanel.PAGE_STAFF);
        contentPanel.add(activityLogPanel, SidebarPanel.PAGE_ACTIVITYLOG);

        // Services page — placeholder for now (no separate panel required)
        JPanel servicesPlaceholder = new JPanel(new GridBagLayout());
        servicesPlaceholder.setBackground(ThemeManager.BG_PRIMARY);
        JLabel comingSoon = new JLabel("Room Services — Coming Soon");
        comingSoon.setFont(ThemeManager.FONT_H2);
        comingSoon.setForeground(ThemeManager.TEXT_SECONDARY);
        servicesPlaceholder.add(comingSoon);
        contentPanel.add(servicesPlaceholder, SidebarPanel.PAGE_SERVICES);
    }

    // Navigation
    private void wireNavigation() {
        sidebar.setOnNavigate(page -> showPanel(page));

        sidebar.setOnLogout(v -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                authService.logout();
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginFrame loginFrame = new LoginFrame(new AuthService());
                    loginFrame.setVisible(true);
                });
            }
        });
    }

    /**
     * Show the specified panel and refresh its data.
     */
    public void showPanel(String page) {
        cardLayout.show(contentPanel, page);
        sidebar.setActivePage(page);

        // Refresh the panel being shown
        SwingUtilities.invokeLater(() -> {
            switch (page) {
                case SidebarPanel.PAGE_DASHBOARD   -> dashboardPanel.refresh();
                case SidebarPanel.PAGE_ROOMS       -> roomPanel.refresh();
                case SidebarPanel.PAGE_GUESTS      -> guestPanel.refresh();
                case SidebarPanel.PAGE_BOOKINGS    -> bookingPanel.refresh();
                case SidebarPanel.PAGE_BILLING     -> billingPanel.refresh();
                case SidebarPanel.PAGE_CHECKINOUT  -> checkInOutPanel.refresh();
                case SidebarPanel.PAGE_ANALYTICS   -> analyticsPanel.refresh();
                case SidebarPanel.PAGE_STAFF       -> staffPanel.refresh();
                case SidebarPanel.PAGE_ACTIVITYLOG -> activityLogPanel.refresh();
            }
        });
    }

    /**
     * Refreshes the dashboard panel. Called after significant data changes.
     */
    public void refreshDashboard() {
        SwingUtilities.invokeLater(() -> dashboardPanel.refresh());
    }

    // Toast Helpers — show notifications from anywhere
    public void showToast(String message, ToastNotification.Type type) {
        ToastNotification.show(this, message, type);
    }

    public void showSuccess(String message) {
        ToastNotification.success(this, message);
    }

    public void showError(String message) {
        ToastNotification.error(this, message);
    }

    public void showWarning(String message) {
        ToastNotification.warning(this, message);
    }

    public void showInfo(String message) {
        ToastNotification.info(this, message);
    }
}
