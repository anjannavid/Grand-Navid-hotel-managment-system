package com.hotelms.ui.panels;

import com.hotelms.model.Booking;
import com.hotelms.service.BookingService;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.ModernTable;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.utils.FormatUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class CheckInOutPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BookingService bookingService;

    private DefaultTableModel checkInsModel;
    private DefaultTableModel checkOutsModel;
    private ModernTable checkInsTable;
    private ModernTable checkOutsTable;
    private JTextField searchField;

    private static final String[] CI_COLUMNS = {"ID", "Guest", "Room", "Phone", "Amount"};
    private static final String[] CO_COLUMNS = {"ID", "Guest", "Room", "Phone", "Amount"};

    public CheckInOutPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.bookingService = new BookingService();

        setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        buildUI();
    }

    private void buildUI() {
        // Header — standardized
        JPanel header = ThemeManager.createHeaderPanel("Check-in / Check-out", "Today's arrivals and departures");

        // Search bar in toolbar
        searchField = ThemeManager.textField("Search by booking ID or guest name...");
        searchField.setPreferredSize(new Dimension(280, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        JPanel toolbar = ThemeManager.createToolbar(searchField);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(toolbar, BorderLayout.SOUTH);

        // Two-column layout
        JPanel columns = new JPanel(new GridLayout(1, 2, ThemeManager.PADDING_MD, 0));
        columns.setOpaque(false);

        // Check-ins column
        JPanel checkInColumn = buildColumn("🟢  Today's Check-ins", CI_COLUMNS, true);
        checkInsModel = (DefaultTableModel) checkInsTable.getModel();

        // Check-outs column
        JPanel checkOutColumn = buildColumn("🔴  Today's Check-outs", CO_COLUMNS, false);
        checkOutsModel = (DefaultTableModel) checkOutsTable.getModel();

        columns.add(checkInColumn);
        columns.add(checkOutColumn);

        add(topSection, BorderLayout.NORTH);
        add(columns, BorderLayout.CENTER);
    }

    private JPanel buildColumn(String columnTitle, String[] cols, boolean isCheckIn) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(ThemeManager.PADDING_MD, 0, 0, 0));

        JLabel titleLabel = new JLabel(columnTitle);
        titleLabel.setFont(ThemeManager.FONT_H3);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        ModernTable table = new ModernTable(model);
        table.setColumnWidth(0, 45);
        table.setCurrencyColumn(4);

        if (isCheckIn) {
            checkInsTable = table;
        } else {
            checkOutsTable = table;
        }

        // Action button
        JButton actionBtn;
        if (isCheckIn) {
            actionBtn = ThemeManager.successButton("✓  Check In Selected");
            actionBtn.addActionListener(e -> performAction(table, model, true));
        } else {
            actionBtn = ThemeManager.primaryButton("🚪  Check Out Selected");
            actionBtn.addActionListener(e -> performAction(table, model, false));
        }

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        actionPanel.add(actionBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(table.inScrollPane(), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void performAction(ModernTable table, DefaultTableModel model, boolean isCheckIn) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            mainFrame.showWarning("Please select a booking");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int bookingId = (int) model.getValueAt(modelRow, 0);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                return isCheckIn
                    ? bookingService.performCheckIn(bookingId)
                    : bookingService.performCheckOut(bookingId);
            }
            @Override protected void done() {
                try {
                    String err = get();
                    if (err == null) {
                        mainFrame.showSuccess((isCheckIn ? "Check-in" : "Check-out") + " successful for #" + bookingId);
                        refresh();
                        mainFrame.refreshDashboard();
                    } else {
                        mainFrame.showError(err);
                    }
                } catch (Exception e) {
                    mainFrame.showError((isCheckIn ? "Check-in" : "Check-out") + " failed");
                }
            }
        };
        worker.execute();
    }

    private void applyFilter() {
        String text = searchField.getText().trim().toLowerCase();
        // Apply to both tables
        RowFilter<DefaultTableModel, Object> filter = text.isEmpty() ? null
            : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text));

        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> ciSorter =
            checkInsTable.getRowSorter() instanceof TableRowSorter<?> s
                ? (TableRowSorter<DefaultTableModel>) s : null;
        if (ciSorter != null) ciSorter.setRowFilter(filter);

        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> coSorter =
            checkOutsTable.getRowSorter() instanceof TableRowSorter<?> s
                ? (TableRowSorter<DefaultTableModel>) s : null;
        if (coSorter != null) coSorter.setRowFilter(filter);
    }

    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Booking> checkIns;
            List<Booking> checkOuts;

            @Override protected Void doInBackground() {
                checkIns = bookingService.getTodayCheckIns();
                checkOuts = bookingService.getTodayCheckOuts();
                return null;
            }
            @Override protected void done() {
                // Check-ins
                checkInsModel.setRowCount(0);
                for (Booking b : checkIns) {
                    checkInsModel.addRow(new Object[]{
                        b.getBookingId(),
                        b.getGuestName() != null ? b.getGuestName() : "—",
                        b.getRoomNumber() != null ? b.getRoomNumber() : "—",
                        b.getGuestPhone() != null ? b.getGuestPhone() : "—",
                        FormatUtils.formatCurrency(b.getTotalAmount())
                    });
                }

                // Check-outs
                checkOutsModel.setRowCount(0);
                for (Booking b : checkOuts) {
                    checkOutsModel.addRow(new Object[]{
                        b.getBookingId(),
                        b.getGuestName() != null ? b.getGuestName() : "—",
                        b.getRoomNumber() != null ? b.getRoomNumber() : "—",
                        b.getGuestPhone() != null ? b.getGuestPhone() : "—",
                        FormatUtils.formatCurrency(b.getTotalAmount())
                    });
                }

                // Add row sorters for filtering
                if (checkInsTable.getRowSorter() == null) {
                    checkInsTable.setRowSorter(new TableRowSorter<>(checkInsModel));
                }
                if (checkOutsTable.getRowSorter() == null) {
                    checkOutsTable.setRowSorter(new TableRowSorter<>(checkOutsModel));
                }
            }
        };
        worker.execute();
    }
}
