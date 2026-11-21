package com.hotelms.ui.panels;

import com.hotelms.database.DatabaseConnection;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.ModernTable;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.utils.ActivityLogger;
import com.hotelms.utils.FormatUtils;
import com.hotelms.utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityLogPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(ActivityLogPanel.class.getName());

    private final MainFrame mainFrame;
    private ModernTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> moduleFilter;

    private static final String[] COLUMNS = {"ID", "User", "Action", "Module", "Entity ID", "Time"};

    public ActivityLogPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        buildUI();
    }

    private void buildUI() {
        // Header — standardized
        JButton refreshBtn = ThemeManager.outlineButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel header = ThemeManager.createHeaderPanel("Activity Log", "Audit trail of all system actions (Admin only)", refreshBtn);

        // Toolbar: module filter
        JLabel filterLabel = new JLabel("Filter by module:");
        filterLabel.setFont(ThemeManager.FONT_BODY);
        filterLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        moduleFilter = new JComboBox<>(new String[]{
            "All",
            ActivityLogger.MODULE_AUTH,
            ActivityLogger.MODULE_ROOM,
            ActivityLogger.MODULE_GUEST,
            ActivityLogger.MODULE_BOOKING,
            ActivityLogger.MODULE_BILLING,
            ActivityLogger.MODULE_PAYMENT,
            ActivityLogger.MODULE_STAFF,
            ActivityLogger.MODULE_SERVICE
        });
        moduleFilter.setPreferredSize(new Dimension(140, 34));
        moduleFilter.addActionListener(e -> applyFilter());

        JPanel toolbar = ThemeManager.createToolbar(filterLabel, moduleFilter);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(toolbar, BorderLayout.SOUTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new ModernTable(tableModel);
        table.setColumnWidth(0, 50);
        table.setColumnWidth(4, 70);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(topSection, BorderLayout.NORTH);
        add(table.inScrollPane(), BorderLayout.CENTER);
    }

    private void applyFilter() {
        String module = (String) moduleFilter.getSelectedItem();
        if ("All".equals(module)) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(module) + "$", 3));
        }
    }

    public void refresh() {
        if (!SessionManager.getInstance().isAdmin()) return;

        SwingWorker<List<LogEntry>, Void> worker = new SwingWorker<>() {
            @Override protected List<LogEntry> doInBackground() {
                return fetchLogs();
            }
            @Override protected void done() {
                try {
                    List<LogEntry> logs = get();
                    tableModel.setRowCount(0);
                    for (LogEntry entry : logs) {
                        tableModel.addRow(new Object[]{
                            entry.logId,
                            entry.username,
                            entry.action,
                            entry.module,
                            entry.entityId > 0 ? entry.entityId : "—",
                            FormatUtils.formatDateTime(entry.loggedAt)
                        });
                    }
                } catch (Exception e) {
                    mainFrame.showError("Failed to load activity logs");
                }
            }
        };
        worker.execute();
    }

    private List<LogEntry> fetchLogs() {
        List<LogEntry> logs = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT al.log_id, u.username, al.action, al.module, al.entity_id, al.logged_at " +
                "FROM activity_logs al " +
                "JOIN users u ON al.user_id = u.user_id " +
                "ORDER BY al.logged_at DESC LIMIT 500");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LogEntry entry = new LogEntry();
                entry.logId = rs.getInt("log_id");
                entry.username = rs.getString("username");
                entry.action = rs.getString("action");
                entry.module = rs.getString("module");
                entry.entityId = rs.getInt("entity_id");
                Timestamp ts = rs.getTimestamp("logged_at");
                if (ts != null) entry.loggedAt = ts.toLocalDateTime();
                logs.add(entry);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch activity logs", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return logs;
    }

    private static class LogEntry {
        int logId;
        String username;
        String action;
        String module;
        int entityId;
        LocalDateTime loggedAt;
    }
}
