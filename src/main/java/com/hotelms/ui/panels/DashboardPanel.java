package com.hotelms.ui.panels;

import com.hotelms.model.Booking;
import com.hotelms.model.Room;
import com.hotelms.service.BookingService;
import com.hotelms.service.GuestService;
import com.hotelms.service.RoomService;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.ModernTable;
import com.hotelms.ui.components.StatCard;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.utils.FormatUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final GuestService guestService;

    // Stat cards
    private StatCard totalRoomsCard;
    private StatCard occupiedCard;
    private StatCard todayRevenueCard;
    private StatCard checkoutsCard;

    // Occupancy bar
    private JPanel occupancyBar;
    private JLabel occupancyLabel;

    // Tables
    private DefaultTableModel recentBookingsModel;
    private DefaultTableModel todayCheckInsModel;

    // Auto-refresh
    private Timer autoRefreshTimer;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.roomService = new RoomService();
        this.bookingService = new BookingService();
        this.guestService = new GuestService();

        setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());

        buildUI();
        startAutoRefresh();
    }

    private void buildUI() {
        JPanel mainContent = new JPanel(new BorderLayout(0, ThemeManager.PADDING_LG));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        // Header — using standardized factory
        JButton refreshBtn = ThemeManager.outlineButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel header = ThemeManager.createHeaderPanel("Dashboard", "Overview of hotel operations", refreshBtn);

        mainContent.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, ThemeManager.PADDING_LG));
        body.setOpaque(false);

        // Stat cards row
        body.add(buildStatCards(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, ThemeManager.PADDING_LG));
        centerPanel.setOpaque(false);

        // Occupancy bar
        centerPanel.add(buildOccupancySection(), BorderLayout.NORTH);

        // Bottom half: recent bookings + today check-ins
        centerPanel.add(buildTablesSection(), BorderLayout.CENTER);

        body.add(centerPanel, BorderLayout.CENTER);

        mainContent.add(body, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, ThemeManager.PADDING_MD, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 100));

        totalRoomsCard  = new StatCard("Total Rooms", "0", "All rooms", ThemeManager.ACCENT_PRIMARY, "🏨");
        occupiedCard    = new StatCard("Occupied", "0", "Currently occupied", ThemeManager.ACCENT_DANGER, "🛏");
        todayRevenueCard = new StatCard("Today's Revenue", "৳0", "Payments received today", ThemeManager.ACCENT_SUCCESS, "💰");
        checkoutsCard   = new StatCard("Today Checkouts", "0", "Due today", ThemeManager.ACCENT_WARNING, "🚪");

        row.add(totalRoomsCard);
        row.add(occupiedCard);
        row.add(todayRevenueCard);
        row.add(checkoutsCard);

        return row;
    }

    private JPanel buildOccupancySection() {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel sectionTitle = new JLabel("Room Occupancy");
        sectionTitle.setFont(ThemeManager.FONT_H3);
        sectionTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        occupancyLabel = new JLabel("0%");
        occupancyLabel.setFont(ThemeManager.FONT_BOLD);
        occupancyLabel.setForeground(ThemeManager.ACCENT_PRIMARY);

        headerRow.add(sectionTitle, BorderLayout.WEST);
        headerRow.add(occupancyLabel, BorderLayout.EAST);

        occupancyBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2.setColor(ThemeManager.BG_TERTIARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        occupancyBar.setOpaque(false);
        occupancyBar.setPreferredSize(new Dimension(0, 24));
        occupancyBar.setLayout(null); // for custom painting

        section.add(headerRow, BorderLayout.NORTH);
        section.add(occupancyBar, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        legend.setOpaque(false);
        legend.add(legendDot(ThemeManager.ACCENT_DANGER, "Occupied"));
        legend.add(legendDot(ThemeManager.ACCENT_WARNING, "Reserved"));
        legend.add(legendDot(ThemeManager.TEXT_MUTED, "Maintenance"));
        legend.add(legendDot(ThemeManager.ACCENT_SUCCESS, "Available"));
        section.add(legend, BorderLayout.SOUTH);

        return section;
    }

    private JPanel legendDot(Color color, String label) {
        JPanel dot = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dot.setOpaque(false);
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(2, 2, 8, 8);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        circle.setPreferredSize(new Dimension(12, 12));
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_SMALL);
        lbl.setForeground(ThemeManager.TEXT_MUTED);
        dot.add(circle);
        dot.add(lbl);
        return dot;
    }

    private JPanel buildTablesSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, ThemeManager.PADDING_MD, 0));
        section.setOpaque(false);

        // Recent Bookings
        JPanel recentPanel = buildTablePanel("Recent Bookings",
            new String[]{"#", "Guest", "Room", "Check-In", "Status"});
        recentBookingsModel = (DefaultTableModel) ((ModernTable) getTableFromPanel(recentPanel)).getModel();

        // Today's Check-ins
        JPanel checkInsPanel = buildTablePanel("Today's Check-ins",
            new String[]{"#", "Guest", "Room", "Phone"});
        todayCheckInsModel = (DefaultTableModel) ((ModernTable) getTableFromPanel(checkInsPanel)).getModel();

        section.add(recentPanel);
        section.add(checkInsPanel);

        return section;
    }

    private JPanel buildTablePanel(String title, String[] columns) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_H3);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        ModernTable table = new ModernTable(model);
        table.setColumnWidth(0, 40);

        // Set status column for Recent Bookings table
        if (columns.length == 5) {
            table.setStatusColumn(4);
        }

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(table.inScrollPane(), BorderLayout.CENTER);

        return panel;
    }

    private JTable getTableFromPanel(JPanel panel) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JScrollPane sp) {
                return (JTable) sp.getViewport().getView();
            }
        }
        return null;
    }

    public void refresh() {
        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() {
                DashboardData data = new DashboardData();
                data.totalRooms = roomService.countTotal();
                data.occupied = roomService.countOccupied();
                data.available = roomService.countAvailable();
                data.todayRevenue = bookingService.getTodayRevenue();
                data.todayCheckOuts = bookingService.getTodayCheckOuts();
                data.todayCheckIns = bookingService.getTodayCheckIns();
                data.recentBookings = bookingService.getRecentBookings(5);
                data.statusCounts = roomService.getStatusCounts();
                return data;
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    updateUI(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateUI(DashboardData data) {
        // Stat cards
        totalRoomsCard.setValue(String.valueOf(data.totalRooms));
        totalRoomsCard.setSubtitle(data.available + " available");

        occupiedCard.setValue(String.valueOf(data.occupied));
        double occPct = data.totalRooms > 0 ? (data.occupied * 100.0 / data.totalRooms) : 0;
        occupiedCard.setSubtitle(String.format("%.0f%% occupancy", occPct));

        todayRevenueCard.setValue(FormatUtils.formatCurrency(data.todayRevenue));
        todayRevenueCard.setSubtitle("Payments received today");

        checkoutsCard.setValue(String.valueOf(data.todayCheckOuts.size()));
        checkoutsCard.setSubtitle("Due today");

        // Occupancy bar
        updateOccupancyBar(data);

        // Recent bookings table
        recentBookingsModel.setRowCount(0);
        for (Booking b : data.recentBookings) {
            recentBookingsModel.addRow(new Object[]{
                b.getBookingId(),
                b.getGuestName() != null ? b.getGuestName() : "—",
                b.getRoomNumber() != null ? b.getRoomNumber() : "—",
                FormatUtils.formatDate(b.getCheckInDate()),
                b.getStatus().name()
            });
        }

        // Today check-ins table
        todayCheckInsModel.setRowCount(0);
        for (Booking b : data.todayCheckIns) {
            todayCheckInsModel.addRow(new Object[]{
                b.getBookingId(),
                b.getGuestName() != null ? b.getGuestName() : "—",
                b.getRoomNumber() != null ? b.getRoomNumber() : "—",
                b.getGuestPhone() != null ? b.getGuestPhone() : "—"
            });
        }
    }

    private void updateOccupancyBar(DashboardData data) {
        int total = data.totalRooms;
        if (total <= 0) {
            occupancyLabel.setText("0%");
            return;
        }

        int occupied = data.statusCounts.getOrDefault(Room.RoomStatus.OCCUPIED, 0);
        int reserved = data.statusCounts.getOrDefault(Room.RoomStatus.RESERVED, 0);
        int maintenance = data.statusCounts.getOrDefault(Room.RoomStatus.MAINTENANCE, 0);
        int available = data.statusCounts.getOrDefault(Room.RoomStatus.AVAILABLE, 0);

        double occPct = (double) occupied / total;
        double resPct = (double) reserved / total;
        double maintPct = (double) maintenance / total;

        occupancyLabel.setText(String.format("%.0f%% occupied", occPct * 100));

        // Repaint the occupancy bar with segments
        occupancyBar.removeAll();
        occupancyBar.setLayout(null);

        // We'll paint segments in paintComponent
        JPanel barInner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background
                g2.setColor(ThemeManager.BG_TERTIARY);
                g2.fillRoundRect(0, 0, w, h, 10, 10);

                int x = 0;
                // Occupied (red)
                int occW = (int) (w * occPct);
                if (occW > 0) {
                    g2.setColor(ThemeManager.ACCENT_DANGER);
                    g2.fillRoundRect(x, 0, occW, h, x == 0 ? 10 : 0, x == 0 ? 10 : 0);
                    x += occW;
                }
                // Reserved (amber)
                int resW = (int) (w * resPct);
                if (resW > 0) {
                    g2.setColor(ThemeManager.ACCENT_WARNING);
                    g2.fillRect(x, 0, resW, h);
                    x += resW;
                }
                // Maintenance (gray)
                int maintW = (int) (w * maintPct);
                if (maintW > 0) {
                    g2.setColor(ThemeManager.TEXT_MUTED);
                    g2.fillRect(x, 0, maintW, h);
                }

                g2.dispose();
            }
        };
        barInner.setOpaque(false);
        barInner.setBounds(0, 0, occupancyBar.getWidth(), occupancyBar.getHeight());

        occupancyBar.add(barInner);
        occupancyBar.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                barInner.setBounds(0, 0, occupancyBar.getWidth(), occupancyBar.getHeight());
            }
        });
        occupancyBar.revalidate();
        occupancyBar.repaint();
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(60_000, e -> refresh()); // every 60 seconds
        autoRefreshTimer.setRepeats(true);
        autoRefreshTimer.start();
    }

    private static class DashboardData {
        int totalRooms;
        int occupied;
        int available;
        double todayRevenue;
        List<Booking> todayCheckOuts;
        List<Booking> todayCheckIns;
        List<Booking> recentBookings;
        Map<Room.RoomStatus, Integer> statusCounts;
    }
}
