package com.hotelms.ui.panels;

import com.hotelms.model.Booking;
import com.hotelms.model.Guest;
import com.hotelms.service.BookingService;
import com.hotelms.service.GuestService;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.ModernTable;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.ui.dialogs.GuestDialog;
import com.hotelms.utils.FormatUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class GuestPanel extends JPanel {

    private final MainFrame mainFrame;
    private final GuestService guestService;
    private final BookingService bookingService;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    private static final String[] COLUMNS = {"ID", "Name", "Phone", "Email", "ID Proof", "City", "Registered"};

    public GuestPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.guestService = new GuestService();
        this.bookingService = new BookingService();

        setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        buildUI();
    }

    private void buildUI() {
        // Header — using standardized factory
        JButton addBtn = ThemeManager.primaryButton("+ Add Guest");
        addBtn.addActionListener(e -> openAddGuest());
        JPanel header = ThemeManager.createHeaderPanel("Guest Management", "View and manage guest records", addBtn);

        // Toolbar
        searchField = ThemeManager.textField("Search by name, phone, email, ID...");
        searchField.setPreferredSize(new Dimension(300, 34));
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

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new ModernTable(tableModel);
        table.setColumnWidth(0, 40);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Bottom buttons — using standardized factory
        JButton viewHistoryBtn = ThemeManager.outlineButton("📋  Booking History");
        viewHistoryBtn.addActionListener(e -> viewBookingHistory());

        JButton editBtn = ThemeManager.outlineButton("✏  Edit");
        editBtn.addActionListener(e -> openEditGuest());

        JPanel bottomBar = ThemeManager.createBottomBar(viewHistoryBtn, editBtn);

        add(topSection, BorderLayout.NORTH);
        add(table.inScrollPane(), BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String text = searchField.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    private void openAddGuest() {
        GuestDialog dialog = new GuestDialog(mainFrame, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            mainFrame.showSuccess("Guest registered successfully");
            refresh();
        }
    }

    private void openEditGuest() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            mainFrame.showWarning("Please select a guest to edit");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int guestId = (int) tableModel.getValueAt(modelRow, 0);

        guestService.getGuestById(guestId).ifPresent(guest -> {
            GuestDialog dialog = new GuestDialog(mainFrame, guest);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                mainFrame.showSuccess("Guest updated successfully");
                refresh();
            }
        });
    }

    private void viewBookingHistory() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            mainFrame.showWarning("Please select a guest");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int guestId = (int) tableModel.getValueAt(modelRow, 0);
        String guestName = (String) tableModel.getValueAt(modelRow, 1);

        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override protected List<Booking> doInBackground() {
                return new com.hotelms.dao.BookingDAO().findByGuestId(guestId);
            }
            @Override protected void done() {
                try {
                    List<Booking> bookings = get();
                    showBookingHistoryDialog(guestName, bookings);
                } catch (Exception e) {
                    mainFrame.showError("Failed to load booking history");
                }
            }
        };
        worker.execute();
    }

    private void showBookingHistoryDialog(String guestName, List<Booking> bookings) {
        JDialog dialog = new JDialog(mainFrame, "Booking History — " + guestName, true);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(mainFrame);

        DefaultTableModel histModel = new DefaultTableModel(
            new String[]{"#", "Room", "Check-In", "Check-Out", "Amount", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Booking b : bookings) {
            histModel.addRow(new Object[]{
                b.getBookingId(),
                b.getRoomNumber(),
                FormatUtils.formatDate(b.getCheckInDate()),
                FormatUtils.formatDate(b.getCheckOutDate()),
                FormatUtils.formatCurrency(b.getTotalAmount()),
                b.getStatus().name()
            });
        }

        ModernTable histTable = new ModernTable(histModel);
        histTable.setStatusColumn(5);
        histTable.setCurrencyColumn(4);
        histTable.setColumnWidth(0, 40);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(ThemeManager.BG_SECONDARY);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Bookings for " + guestName);
        titleLabel.setFont(ThemeManager.FONT_H3);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        if (bookings.isEmpty()) {
            JLabel empty = new JLabel("No bookings found for this guest");
            empty.setFont(ThemeManager.FONT_BODY);
            empty.setForeground(ThemeManager.TEXT_MUTED);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            content.add(titleLabel, BorderLayout.NORTH);
            content.add(empty, BorderLayout.CENTER);
        } else {
            content.add(titleLabel, BorderLayout.NORTH);
            content.add(histTable.inScrollPane(), BorderLayout.CENTER);
        }

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    public void refresh() {
        SwingWorker<List<Guest>, Void> worker = new SwingWorker<>() {
            @Override protected List<Guest> doInBackground() {
                return guestService.getAllGuests();
            }
            @Override protected void done() {
                try {
                    List<Guest> guests = get();
                    tableModel.setRowCount(0);
                    for (Guest g : guests) {
                        tableModel.addRow(new Object[]{
                            g.getGuestId(),
                            g.getFullName(),
                            g.getPhone() != null ? g.getPhone() : "—",
                            g.getEmail() != null ? g.getEmail() : "—",
                            g.getIdProofType() != null ? g.getIdProofType().name().replace("_", " ") : "—",
                            g.getCity() != null ? g.getCity() : "—",
                            FormatUtils.formatDateTime(g.getRegisteredAt())
                        });
                    }
                } catch (Exception e) {
                    mainFrame.showError("Failed to load guests");
                }
            }
        };
        worker.execute();
    }
}
