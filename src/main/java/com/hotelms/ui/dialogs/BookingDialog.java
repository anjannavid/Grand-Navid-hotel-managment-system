package com.hotelms.ui.dialogs;

import com.hotelms.model.Guest;
import com.hotelms.model.Room;
import com.hotelms.service.BookingService;
import com.hotelms.service.GuestService;
import com.hotelms.service.RoomService;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.utils.FormatUtils;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookingDialog extends JDialog {

    private final BookingService bookingService;
    private final GuestService guestService;
    private final RoomService roomService;
    private boolean saved = false;

    private JComboBox<GuestItem> guestCombo;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JComboBox<RoomItem> roomCombo;
    private JSpinner adultsSpinner;
    private JSpinner childrenSpinner;
    private JTextArea specialRequestsArea;
    private JTextField advanceField;
    private JLabel totalLabel;
    private JLabel nightsLabel;
    private JButton checkAvailabilityBtn;

    public BookingDialog(JFrame parent) {
        super(parent, "Create New Booking", true);
        this.bookingService = new BookingService();
        this.guestService = new GuestService();
        this.roomService = new RoomService();

        setSize(520, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(ThemeManager.BG_SECONDARY);

        buildForm();
        loadGuests();
    }

    private void buildForm() {
        JPanel content = new JPanel();
        content.setBackground(ThemeManager.BG_SECONDARY);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Guest selection
        content.add(fieldLabel("Select Guest *"));
        guestCombo = new JComboBox<>();
        guestCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(guestCombo);
        content.add(Box.createVerticalStrut(12));

        // Check-in / Check-out dates
        JPanel dateRow = new JPanel(new GridLayout(1, 2, 12, 0));
        dateRow.setOpaque(false);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel ciPanel = fieldPanel("Check-In Date * (YYYY-MM-DD)");
        checkInField = ThemeManager.textField(LocalDate.now().toString());
        checkInField.setText(LocalDate.now().toString());
        ciPanel.add(checkInField, BorderLayout.CENTER);

        JPanel coPanel = fieldPanel("Check-Out Date * (YYYY-MM-DD)");
        checkOutField = ThemeManager.textField(LocalDate.now().plusDays(1).toString());
        checkOutField.setText(LocalDate.now().plusDays(1).toString());
        coPanel.add(checkOutField, BorderLayout.CENTER);

        dateRow.add(ciPanel);
        dateRow.add(coPanel);
        content.add(dateRow);
        content.add(Box.createVerticalStrut(8));

        // Check Availability button + nights label
        JPanel availRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        availRow.setOpaque(false);
        availRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        checkAvailabilityBtn = ThemeManager.outlineButton("🔍  Check Availability");
        checkAvailabilityBtn.addActionListener(e -> checkAvailability());

        nightsLabel = new JLabel("");
        nightsLabel.setFont(ThemeManager.FONT_SMALL);
        nightsLabel.setForeground(ThemeManager.ACCENT_INFO);

        availRow.add(checkAvailabilityBtn);
        availRow.add(nightsLabel);
        content.add(availRow);
        content.add(Box.createVerticalStrut(12));

        // Room selection
        content.add(fieldLabel("Select Room *"));
        roomCombo = new JComboBox<>();
        roomCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        roomCombo.addActionListener(e -> updateTotal());
        content.add(roomCombo);
        content.add(Box.createVerticalStrut(12));

        // Adults + Children
        JPanel occRow = new JPanel(new GridLayout(1, 2, 12, 0));
        occRow.setOpaque(false);
        occRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel adultPanel = fieldPanel("Adults *");
        adultsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        adultPanel.add(adultsSpinner, BorderLayout.CENTER);

        JPanel childPanel = fieldPanel("Children");
        childrenSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        childPanel.add(childrenSpinner, BorderLayout.CENTER);

        occRow.add(adultPanel);
        occRow.add(childPanel);
        content.add(occRow);
        content.add(Box.createVerticalStrut(12));

        // Special Requests
        content.add(fieldLabel("Special Requests"));
        specialRequestsArea = new JTextArea(3, 20);
        specialRequestsArea.setFont(ThemeManager.FONT_BODY);
        specialRequestsArea.setLineWrap(true);
        specialRequestsArea.setWrapStyleWord(true);
        JScrollPane spReqScroll = new JScrollPane(specialRequestsArea);
        spReqScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        content.add(spReqScroll);
        content.add(Box.createVerticalStrut(12));

        // Advance Payment + Total
        JPanel payRow = new JPanel(new GridLayout(1, 2, 12, 0));
        payRow.setOpaque(false);
        payRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel advPanel = fieldPanel("Advance Payment (৳)");
        advanceField = ThemeManager.textField("0");
        advanceField.setText("0");
        advPanel.add(advanceField, BorderLayout.CENTER);

        JPanel totalPanel = new JPanel(new BorderLayout(0, 4));
        totalPanel.setOpaque(false);
        totalPanel.add(fieldLabel("Estimated Total"), BorderLayout.NORTH);
        totalLabel = new JLabel("৳0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(ThemeManager.ACCENT_SUCCESS);
        totalPanel.add(totalLabel, BorderLayout.CENTER);

        payRow.add(advPanel);
        payRow.add(totalPanel);
        content.add(payRow);
        content.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);

        JButton cancelBtn = ThemeManager.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton createBtn = ThemeManager.successButton("✓  Create Booking");
        createBtn.addActionListener(e -> createBooking());

        buttonRow.add(cancelBtn);
        buttonRow.add(createBtn);

        content.add(Box.createVerticalGlue());
        content.add(buttonRow);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        setContentPane(scrollPane);
    }

    private void loadGuests() {
        SwingWorker<List<Guest>, Void> worker = new SwingWorker<>() {
            @Override protected List<Guest> doInBackground() {
                return guestService.getAllGuests();
            }
            @Override protected void done() {
                try {
                    List<Guest> guests = get();
                    guestCombo.removeAllItems();
                    for (Guest g : guests) {
                        guestCombo.addItem(new GuestItem(g.getGuestId(), g.getFullName(), g.getPhone()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void checkAvailability() {
        LocalDate checkIn, checkOut;
        try {
            checkIn = LocalDate.parse(checkInField.getText().trim());
            checkOut = LocalDate.parse(checkOutField.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out must be after check-in", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        nightsLabel.setText(FormatUtils.formatNights(nights));

        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override protected List<Room> doInBackground() {
                return roomService.getAvailableRooms(checkIn, checkOut);
            }
            @Override protected void done() {
                try {
                    List<Room> rooms = get();
                    roomCombo.removeAllItems();
                    for (Room r : rooms) {
                        roomCombo.addItem(new RoomItem(r.getRoomId(),
                            r.getRoomNumber() + " — " + r.getRoomType().getDisplayName() +
                            " (" + r.getBedType() + ") — " + FormatUtils.formatCurrency(r.getPricePerNight()) + "/night",
                            r.getPricePerNight()));
                    }
                    if (rooms.isEmpty()) {
                        JOptionPane.showMessageDialog(BookingDialog.this,
                            "No rooms available for the selected dates",
                            "No Availability",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                    updateTotal();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateTotal() {
        try {
            LocalDate checkIn = LocalDate.parse(checkInField.getText().trim());
            LocalDate checkOut = LocalDate.parse(checkOutField.getText().trim());
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

            RoomItem selectedRoom = (RoomItem) roomCombo.getSelectedItem();
            if (selectedRoom != null && nights > 0) {
                double total = nights * selectedRoom.pricePerNight;
                totalLabel.setText(FormatUtils.formatCurrency(total));
            }
        } catch (Exception ignored) {}
    }

    private void createBooking() {
        // Validate guest
        GuestItem guest = (GuestItem) guestCombo.getSelectedItem();
        if (guest == null) {
            JOptionPane.showMessageDialog(this, "Please select a guest", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate room
        RoomItem room = (RoomItem) roomCombo.getSelectedItem();
        if (room == null) {
            JOptionPane.showMessageDialog(this, "Please check availability and select a room", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Parse dates
        LocalDate checkIn, checkOut;
        try {
            checkIn = LocalDate.parse(checkInField.getText().trim());
            checkOut = LocalDate.parse(checkOutField.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double advance;
        try {
            advance = Double.parseDouble(advanceField.getText().trim());
        } catch (NumberFormatException e) {
            advance = 0;
        }

        int numAdults = (int) adultsSpinner.getValue();
        int numChildren = (int) childrenSpinner.getValue();
        String specialReqs = specialRequestsArea.getText().trim();
        double finalAdvance = advance;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                return bookingService.createBooking(
                    room.roomId, guest.guestId,
                    checkIn, checkOut,
                    numAdults, numChildren,
                    specialReqs.isEmpty() ? null : specialReqs,
                    finalAdvance
                );
            }
            @Override protected void done() {
                try {
                    String err = get();
                    if (err == null) {
                        saved = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(BookingDialog.this, err, "Booking Error", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BookingDialog.this, "Failed to create booking", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public boolean isSaved() { return saved; }

    private JPanel fieldPanel(String label) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(fieldLabel(label), BorderLayout.NORTH);
        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeManager.FONT_LABEL);
        lbl.setForeground(ThemeManager.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private record GuestItem(int guestId, String name, String phone) {
        @Override public String toString() { return name + " (" + phone + ")"; }
    }

    private record RoomItem(int roomId, String display, double pricePerNight) {
        @Override public String toString() { return display; }
    }
}
