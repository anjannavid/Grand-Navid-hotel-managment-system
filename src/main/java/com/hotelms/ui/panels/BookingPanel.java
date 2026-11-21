package com.hotelms.ui.panels;

import com.hotelms.model.Booking;
import com.hotelms.service.BookingService;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.ModernTable;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.ui.dialogs.BookingDialog;
import com.hotelms.utils.FormatUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class BookingPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BookingService bookingService;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private String activeStatusFilter = "ALL";

    private static final String[] COLUMNS = {
        "ID", "Guest", "Room", "Type", "Check-In", "Check-Out", "Nights", "Amount", "Status"
    };

    public BookingPanel(MainFrame mainFrame) {
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
        JButton newBookingBtn = ThemeManager.primaryButton("+ New Booking");
        newBookingBtn.addActionListener(e -> openNewBooking());
        JPanel header = ThemeManager.createHeaderPanel("Bookings", "Manage reservations and check-ins", newBookingBtn);

        // Status filter tabs
        JPanel filterTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        filterTabs.setOpaque(false);

        String[] tabs = {"All", "Confirmed", "Checked In", "Checked Out", "Cancelled"};
        ButtonGroup tabGroup = new ButtonGroup();

        for (String tab : tabs) {
            JToggleButton btn = new JToggleButton(tab);
            btn.setFont(ThemeManager.FONT_BODY);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.putClientProperty("JButton.buttonType", "roundRect");
            if ("All".equals(tab)) btn.setSelected(true);
            btn.addActionListener(e -> {
                activeStatusFilter = tab.toUpperCase().replace(" ", "_");
                if ("ALL".equals(activeStatusFilter)) activeStatusFilter = "ALL";
                applyFilter();
            });
            tabGroup.add(btn);
            filterTabs.add(btn);
        }

        searchField = ThemeManager.textField("Search bookings...");
        searchField.setPreferredSize(new Dimension(250, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        filterTabs.add(Box.createHorizontalStrut(20));
        filterTabs.add(searchField);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(filterTabs, BorderLayout.SOUTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new ModernTable(tableModel);
        table.setColumnWidth(0, 45);
        table.setCenterColumn(6);
        table.setCurrencyColumn(7);
        table.setStatusColumn(8);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Bottom — standardized
        JButton checkInBtn = ThemeManager.successButton("✓  Check In");
        checkInBtn.addActionListener(e -> performCheckIn());
        JButton checkOutBtn = ThemeManager.primaryButton("🚪  Check Out");
        checkOutBtn.addActionListener(e -> performCheckOut());
        JButton cancelBtn = ThemeManager.dangerButton("✕  Cancel Booking");
        cancelBtn.addActionListener(e -> cancelBooking());
        JPanel bottomBar = ThemeManager.createBottomBar(checkInBtn, checkOutBtn, cancelBtn);

        add(topSection, BorderLayout.NORTH);
        add(table.inScrollPane(), BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String text = searchField.getText().trim().toLowerCase();
        RowFilter<DefaultTableModel, Object> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                if (!"ALL".equals(activeStatusFilter)) {
                    String rowStatus = entry.getStringValue(8);
                    if (!rowStatus.equalsIgnoreCase(activeStatusFilter)) return false;
                }
                if (!text.isEmpty()) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        if (entry.getStringValue(i).toLowerCase().contains(text)) return true;
                    }
                    return false;
                }
                return true;
            }
        };
        sorter.setRowFilter(filter);
    }

    private void openNewBooking() {
        BookingDialog dialog = new BookingDialog(mainFrame);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            mainFrame.showSuccess("Booking created successfully");
            refresh();
            mainFrame.refreshDashboard();
        }
    }

    private void performCheckIn() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { mainFrame.showWarning("Select a booking to check in"); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int bookingId = (int) tableModel.getValueAt(modelRow, 0);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() { return bookingService.performCheckIn(bookingId); }
            @Override protected void done() {
                try {
                    String err = get();
                    if (err == null) { mainFrame.showSuccess("Check-in successful for booking #" + bookingId); refresh(); mainFrame.refreshDashboard(); }
                    else mainFrame.showError(err);
                } catch (Exception e) { mainFrame.showError("Check-in failed"); }
            }
        };
        worker.execute();
    }

    private void performCheckOut() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { mainFrame.showWarning("Select a booking to check out"); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int bookingId = (int) tableModel.getValueAt(modelRow, 0);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() { return bookingService.performCheckOut(bookingId); }
            @Override protected void done() {
                try {
                    String err = get();
                    if (err == null) { mainFrame.showSuccess("Check-out successful for booking #" + bookingId); refresh(); mainFrame.refreshDashboard(); }
                    else mainFrame.showError(err);
                } catch (Exception e) { mainFrame.showError("Check-out failed"); }
            }
        };
        worker.execute();
    }

    private void cancelBooking() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { mainFrame.showWarning("Select a booking to cancel"); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int bookingId = (int) tableModel.getValueAt(modelRow, 0);
        String reason = JOptionPane.showInputDialog(mainFrame, "Enter cancellation reason:", "Cancel Booking #" + bookingId, JOptionPane.QUESTION_MESSAGE);
        if (reason != null && !reason.trim().isEmpty()) {
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override protected String doInBackground() { return bookingService.cancelBooking(bookingId, reason.trim()); }
                @Override protected void done() {
                    try {
                        String err = get();
                        if (err == null) { mainFrame.showSuccess("Booking #" + bookingId + " cancelled"); refresh(); mainFrame.refreshDashboard(); }
                        else mainFrame.showError(err);
                    } catch (Exception e) { mainFrame.showError("Cancellation failed"); }
                }
            };
            worker.execute();
        }
    }

    public void refresh() {
        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override protected List<Booking> doInBackground() { return bookingService.getAllBookings(); }
            @Override protected void done() {
                try {
                    List<Booking> bookings = get();
                    tableModel.setRowCount(0);
                    for (Booking b : bookings) {
                        tableModel.addRow(new Object[]{
                            b.getBookingId(),
                            b.getGuestName() != null ? b.getGuestName() : "—",
                            b.getRoomNumber() != null ? b.getRoomNumber() : "—",
                            b.getRoomType() != null ? b.getRoomType() : "—",
                            FormatUtils.formatDate(b.getCheckInDate()),
                            FormatUtils.formatDate(b.getCheckOutDate()),
                            b.getNights(),
                            FormatUtils.formatCurrency(b.getTotalAmount()),
                            b.getStatus().name()
                        });
                    }
                } catch (Exception e) { mainFrame.showError("Failed to load bookings"); }
            }
        };
        worker.execute();
    }
}
