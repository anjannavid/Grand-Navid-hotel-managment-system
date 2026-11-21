package com.hotelms.ui.dialogs;

import com.hotelms.model.Room;
import com.hotelms.service.RoomService;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.utils.ActivityLogger;
import com.hotelms.utils.SessionManager;

import javax.swing.*;
import java.awt.*;

public class RoomDialog extends JDialog {

    private final RoomService roomService;
    private final Room existingRoom; // null = add mode
    private boolean saved = false;

    // Form fields
    private JTextField roomNumberField;
    private JComboBox<Room.RoomType> roomTypeCombo;
    private JSpinner floorSpinner;
    private JTextField priceField;
    private JSpinner maxOccupancySpinner;
    private JComboBox<Room.BedType> bedTypeCombo;
    private JTextArea amenitiesArea;
    private JTextArea descriptionArea;
    private JComboBox<Room.RoomStatus> statusCombo;

    public RoomDialog(JFrame parent, Room room) {
        super(parent, room == null ? "Add New Room" : "Edit Room " + room.getRoomNumber(), true);
        this.roomService = new RoomService();
        this.existingRoom = room;

        setSize(480, 620);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(ThemeManager.BG_SECONDARY);

        buildForm();
        if (existingRoom != null) populateForm();
    }

    private void buildForm() {
        JPanel content = new JPanel();
        content.setBackground(ThemeManager.BG_SECONDARY);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Room Number
        content.add(fieldLabel("Room Number *"));
        roomNumberField = ThemeManager.textField("e.g., 101");
        roomNumberField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(roomNumberField);
        content.add(Box.createVerticalStrut(12));

        // Room Type
        content.add(fieldLabel("Room Type *"));
        roomTypeCombo = new JComboBox<>(Room.RoomType.values());
        roomTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(roomTypeCombo);
        content.add(Box.createVerticalStrut(12));

        // Floor + Price row
        JPanel row1 = new JPanel(new GridLayout(1, 2, 12, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel floorPanel = new JPanel(new BorderLayout(0, 4));
        floorPanel.setOpaque(false);
        floorPanel.add(fieldLabel("Floor *"), BorderLayout.NORTH);
        floorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        floorPanel.add(floorSpinner, BorderLayout.CENTER);

        JPanel pricePanel = new JPanel(new BorderLayout(0, 4));
        pricePanel.setOpaque(false);
        pricePanel.add(fieldLabel("Price/Night (৳) *"), BorderLayout.NORTH);
        priceField = ThemeManager.textField("e.g., 4500");
        pricePanel.add(priceField, BorderLayout.CENTER);

        row1.add(floorPanel);
        row1.add(pricePanel);
        content.add(row1);
        content.add(Box.createVerticalStrut(12));

        // Max Occupancy + Bed Type row
        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel occPanel = new JPanel(new BorderLayout(0, 4));
        occPanel.setOpaque(false);
        occPanel.add(fieldLabel("Max Occupancy *"), BorderLayout.NORTH);
        maxOccupancySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        occPanel.add(maxOccupancySpinner, BorderLayout.CENTER);

        JPanel bedPanel = new JPanel(new BorderLayout(0, 4));
        bedPanel.setOpaque(false);
        bedPanel.add(fieldLabel("Bed Type"), BorderLayout.NORTH);
        bedTypeCombo = new JComboBox<>(Room.BedType.values());
        bedPanel.add(bedTypeCombo, BorderLayout.CENTER);

        row2.add(occPanel);
        row2.add(bedPanel);
        content.add(row2);
        content.add(Box.createVerticalStrut(12));

        // Amenities
        content.add(fieldLabel("Amenities"));
        amenitiesArea = new JTextArea(2, 20);
        amenitiesArea.setFont(ThemeManager.FONT_BODY);
        amenitiesArea.setLineWrap(true);
        amenitiesArea.setWrapStyleWord(true);
        JScrollPane amenScroll = new JScrollPane(amenitiesArea);
        amenScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        content.add(amenScroll);
        content.add(Box.createVerticalStrut(12));

        // Description
        content.add(fieldLabel("Description"));
        descriptionArea = new JTextArea(2, 20);
        descriptionArea.setFont(ThemeManager.FONT_BODY);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        content.add(descScroll);
        content.add(Box.createVerticalStrut(12));

        // Status (edit only)
        if (existingRoom != null) {
            content.add(fieldLabel("Status"));
            statusCombo = new JComboBox<>(Room.RoomStatus.values());
            statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            content.add(statusCombo);
            content.add(Box.createVerticalStrut(12));
        }

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);

        JButton cancelBtn = ThemeManager.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = ThemeManager.primaryButton(existingRoom == null ? "Add Room" : "Save Changes");
        saveBtn.addActionListener(e -> save());

        buttonRow.add(cancelBtn);
        buttonRow.add(saveBtn);

        content.add(Box.createVerticalGlue());
        content.add(buttonRow);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        setContentPane(scrollPane);
    }

    private void populateForm() {
        roomNumberField.setText(existingRoom.getRoomNumber());
        roomTypeCombo.setSelectedItem(existingRoom.getRoomType());
        floorSpinner.setValue(existingRoom.getFloorNumber());
        priceField.setText(String.valueOf(existingRoom.getPricePerNight()));
        maxOccupancySpinner.setValue(existingRoom.getMaxOccupancy());
        if (existingRoom.getBedType() != null) bedTypeCombo.setSelectedItem(existingRoom.getBedType());
        if (existingRoom.getAmenities() != null) amenitiesArea.setText(existingRoom.getAmenities());
        if (existingRoom.getDescription() != null) descriptionArea.setText(existingRoom.getDescription());
        if (statusCombo != null && existingRoom.getStatus() != null) statusCombo.setSelectedItem(existingRoom.getStatus());

        // Disable room number for editing
        roomNumberField.setEditable(false);
        roomNumberField.setForeground(ThemeManager.TEXT_MUTED);
    }

    private void save() {
        // Validate
        String roomNum = roomNumberField.getText().trim();
        if (roomNum.isEmpty()) {
            showFieldError("Room number is required"); return;
        }

        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) { showFieldError("Price must be positive"); return; }
        } catch (NumberFormatException e) {
            showFieldError("Invalid price format"); return;
        }

        Room room = existingRoom != null ? existingRoom : new Room();
        room.setRoomNumber(roomNum);
        room.setRoomType((Room.RoomType) roomTypeCombo.getSelectedItem());
        room.setFloorNumber((int) floorSpinner.getValue());
        room.setPricePerNight(price);
        room.setMaxOccupancy((int) maxOccupancySpinner.getValue());
        room.setBedType((Room.BedType) bedTypeCombo.getSelectedItem());
        room.setAmenities(amenitiesArea.getText().trim());
        room.setDescription(descriptionArea.getText().trim());
        if (statusCombo != null) room.setStatus((Room.RoomStatus) statusCombo.getSelectedItem());

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                return existingRoom == null ? roomService.addRoom(room) : roomService.updateRoom(room);
            }
            @Override protected void done() {
                try {
                    String err = get();
                    if (err == null) {
                        saved = true;
                        dispose();
                    } else {
                        showFieldError(err);
                    }
                } catch (Exception e) {
                    showFieldError("Operation failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showFieldError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeManager.FONT_LABEL);
        lbl.setForeground(ThemeManager.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    public boolean isSaved() { return saved; }
}
