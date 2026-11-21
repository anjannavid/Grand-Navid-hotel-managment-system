package com.hotelms.ui.panels;

import com.hotelms.model.Room;
import com.hotelms.service.RoomService;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.ModernTable;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.ui.dialogs.RoomDialog;
import com.hotelms.utils.FormatUtils;
import com.hotelms.utils.SessionManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class RoomManagementPanel extends JPanel {

    private final MainFrame mainFrame;
    private final RoomService roomService;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private static final String[] COLUMNS = {"ID", "Room #", "Type", "Floor", "Price/Night", "Bed Type", "Max Occ.", "Status"};

    public RoomManagementPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.roomService = new RoomService();

        setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        buildUI();
    }

    private void buildUI() {
        // Header — using standardized factory
        JButton addBtn = null;
        if (SessionManager.getInstance().isAdmin()) {
            addBtn = ThemeManager.primaryButton("+ Add Room");
            addBtn.addActionListener(e -> openAddRoom());
        }
        JPanel header = ThemeManager.createHeaderPanel("Room Management", "Manage all hotel rooms", addBtn);

        // Toolbar: search + status filter
        // Toolbar: search + status filter
        searchField = ThemeManager.textField("Search rooms...");
        searchField.setPreferredSize(new Dimension(250, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        statusFilter = new JComboBox<>(new String[]{"All", "Available", "Occupied", "Reserved", "Maintenance"});
        statusFilter.setPreferredSize(new Dimension(150, 34));
        statusFilter.addActionListener(e -> applyFilter());

        JPanel toolbar = ThemeManager.createToolbar(searchField, statusFilter);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(toolbar, BorderLayout.SOUTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new ModernTable(tableModel);
        table.setColumnWidth(0, 40);    // ID
        table.setColumnWidth(1, 80);    // Room #
        table.setCenterColumn(3);       // Floor
        table.setCurrencyColumn(4);     // Price
        table.setCenterColumn(6);       // Max Occ
        table.setStatusColumn(7);       // Status

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Bottom buttons
        JButton editBtn = ThemeManager.outlineButton("✏  Edit");
        editBtn.addActionListener(e -> openEditRoom());

        JButton deleteBtn = ThemeManager.dangerButton("🗑  Delete");
        deleteBtn.addActionListener(e -> deleteRoom());
        deleteBtn.setVisible(SessionManager.getInstance().isAdmin());

        JPanel bottomBar = ThemeManager.createBottomBar(editBtn, deleteBtn);

        add(topSection, BorderLayout.NORTH);
        add(table.inScrollPane(), BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String text = searchField.getText().trim().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();

        RowFilter<DefaultTableModel, Object> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                // Status filter
                if (!"All".equals(status)) {
                    String rowStatus = entry.getStringValue(7);
                    if (!rowStatus.equalsIgnoreCase(status.replace(" ", "_"))) return false;
                }
                // Text search
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

    private void openAddRoom() {
        RoomDialog dialog = new RoomDialog(mainFrame, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            mainFrame.showSuccess("Room added successfully");
            refresh();
            mainFrame.refreshDashboard();
        }
    }

    private void openEditRoom() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            mainFrame.showWarning("Please select a room to edit");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int roomId = (int) tableModel.getValueAt(modelRow, 0);

        roomService.getRoomById(roomId).ifPresent(room -> {
            RoomDialog dialog = new RoomDialog(mainFrame, room);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                mainFrame.showSuccess("Room updated successfully");
                refresh();
            }
        });
    }

    private void deleteRoom() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            mainFrame.showWarning("Please select a room to delete");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int roomId = (int) tableModel.getValueAt(modelRow, 0);
        String roomNumber = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(mainFrame,
            "Are you sure you want to delete Room " + roomNumber + "?\n" +
            "This cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override protected String doInBackground() {
                    return roomService.deleteRoom(roomId);
                }
                @Override protected void done() {
                    try {
                        String err = get();
                        if (err == null) {
                            mainFrame.showSuccess("Room " + roomNumber + " deleted");
                            refresh();
                            mainFrame.refreshDashboard();
                        } else {
                            mainFrame.showError(err);
                        }
                    } catch (Exception e) {
                        mainFrame.showError("Failed to delete room");
                    }
                }
            };
            worker.execute();
        }
    }

    public void refresh() {
        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override protected List<Room> doInBackground() {
                return roomService.getAllRooms();
            }
            @Override protected void done() {
                try {
                    List<Room> rooms = get();
                    tableModel.setRowCount(0);
                    for (Room r : rooms) {
                        tableModel.addRow(new Object[]{
                            r.getRoomId(),
                            r.getRoomNumber(),
                            r.getRoomType() != null ? r.getRoomType().getDisplayName() : "—",
                            r.getFloorNumber(),
                            FormatUtils.formatCurrency(r.getPricePerNight()),
                            r.getBedType() != null ? r.getBedType().name() : "—",
                            r.getMaxOccupancy(),
                            r.getStatus() != null ? r.getStatus().name() : "AVAILABLE"
                        });
                    }
                } catch (Exception e) {
                    mainFrame.showError("Failed to load rooms");
                }
            }
        };
        worker.execute();
    }
}
