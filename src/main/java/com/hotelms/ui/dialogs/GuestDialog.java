package com.hotelms.ui.dialogs;

import com.hotelms.model.Guest;
import com.hotelms.service.GuestService;
import com.hotelms.ui.components.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class GuestDialog extends JDialog {

    private final GuestService guestService;
    private final Guest existingGuest;
    private boolean saved = false;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<Guest.IdProofType> idProofTypeCombo;
    private JTextField idProofNumberField;
    private JTextField addressField;
    private JTextField cityField;
    private JTextField stateField;
    private JTextField countryField;
    private JComboBox<Guest.Gender> genderCombo;
    private JTextField dobField;

    public GuestDialog(JFrame parent, Guest guest) {
        super(parent, guest == null ? "Register New Guest" : "Edit Guest", true);
        this.guestService = new GuestService();
        this.existingGuest = guest;

        setSize(500, 640);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(ThemeManager.BG_SECONDARY);

        buildForm();
        if (existingGuest != null) populateForm();
    }

    private void buildForm() {
        JPanel content = new JPanel();
        content.setBackground(ThemeManager.BG_SECONDARY);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // First + Last name
        JPanel nameRow = new JPanel(new GridLayout(1, 2, 12, 0));
        nameRow.setOpaque(false);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel fnPanel = fieldPanel("First Name *");
        firstNameField = ThemeManager.textField("First name");
        fnPanel.add(firstNameField, BorderLayout.CENTER);

        JPanel lnPanel = fieldPanel("Last Name *");
        lastNameField = ThemeManager.textField("Last name");
        lnPanel.add(lastNameField, BorderLayout.CENTER);

        nameRow.add(fnPanel);
        nameRow.add(lnPanel);
        content.add(nameRow);
        content.add(Box.createVerticalStrut(12));

        // Phone + Email
        JPanel contactRow = new JPanel(new GridLayout(1, 2, 12, 0));
        contactRow.setOpaque(false);
        contactRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel phPanel = fieldPanel("Phone *");
        phoneField = ThemeManager.textField("+088-XXXXXXXXXX");
        phPanel.add(phoneField, BorderLayout.CENTER);

        JPanel emPanel = fieldPanel("Email");
        emailField = ThemeManager.textField("guest@email.com");
        emPanel.add(emailField, BorderLayout.CENTER);

        contactRow.add(phPanel);
        contactRow.add(emPanel);
        content.add(contactRow);
        content.add(Box.createVerticalStrut(12));

        // ID Proof Type + Number
        JPanel idRow = new JPanel(new GridLayout(1, 2, 12, 0));
        idRow.setOpaque(false);
        idRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel idTypePanel = fieldPanel("ID Proof Type *");
        idProofTypeCombo = new JComboBox<>(new Guest.IdProofType[] {
    Guest.IdProofType.PASSPORT,
    Guest.IdProofType.DRIVING_LICENSE,
    Guest.IdProofType.VOTER_ID
});
        idTypePanel.add(idProofTypeCombo, BorderLayout.CENTER);

        JPanel idNumPanel = fieldPanel("ID Proof Number *");
        idProofNumberField = ThemeManager.textField("ID number");
        idNumPanel.add(idProofNumberField, BorderLayout.CENTER);

        idRow.add(idTypePanel);
        idRow.add(idNumPanel);
        content.add(idRow);
        content.add(Box.createVerticalStrut(12));

        // Gender + DOB
        JPanel row3 = new JPanel(new GridLayout(1, 2, 12, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel genderPanel = fieldPanel("Gender");
        genderCombo = new JComboBox<>(Guest.Gender.values());
        genderPanel.add(genderCombo, BorderLayout.CENTER);

        JPanel dobPanel = fieldPanel("Date of Birth");
        dobField = ThemeManager.textField("YYYY-MM-DD");
        dobPanel.add(dobField, BorderLayout.CENTER);

        row3.add(genderPanel);
        row3.add(dobPanel);
        content.add(row3);
        content.add(Box.createVerticalStrut(12));

        // Address
        content.add(fieldLabel("Address"));
        addressField = ThemeManager.textField("Street address");
        addressField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(addressField);
        content.add(Box.createVerticalStrut(12));

        // City + State + Country
        JPanel locRow = new JPanel(new GridLayout(1, 3, 8, 0));
        locRow.setOpaque(false);
        locRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel cityPanel = fieldPanel("City");
        cityField = ThemeManager.textField("City");
        cityPanel.add(cityField, BorderLayout.CENTER);

        //JPanel statePanel = fieldPanel("State");
       // stateField = ThemeManager.textField("State");
        //statePanel.add(stateField, BorderLayout.CENTER);

        JPanel countryPanel = fieldPanel("Country");
        countryField = ThemeManager.textField("Bangladesh");
        countryField.setText("Bangladesh");
        countryPanel.add(countryField, BorderLayout.CENTER);

        locRow.add(cityPanel);
       // locRow.add(statePanel);
        locRow.add(countryPanel);
        content.add(locRow);
        content.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);

        JButton cancelBtn = ThemeManager.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = ThemeManager.primaryButton(existingGuest == null ? "Register Guest" : "Save Changes");
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
        firstNameField.setText(existingGuest.getFirstName());
        lastNameField.setText(existingGuest.getLastName());
        phoneField.setText(existingGuest.getPhone());
        emailField.setText(existingGuest.getEmail() != null ? existingGuest.getEmail() : "");
        if (existingGuest.getIdProofType() != null) idProofTypeCombo.setSelectedItem(existingGuest.getIdProofType());
        idProofNumberField.setText(existingGuest.getIdProofNumber());
        addressField.setText(existingGuest.getAddress() != null ? existingGuest.getAddress() : "");
        cityField.setText(existingGuest.getCity() != null ? existingGuest.getCity() : "");
        // stateField.setText(existingGuest.getState() != null ? existingGuest.getState() : "");
        countryField.setText(existingGuest.getCountry() != null ? existingGuest.getCountry() : "Bangladesh");
        if (existingGuest.getGender() != null) genderCombo.setSelectedItem(existingGuest.getGender());
        if (existingGuest.getDateOfBirth() != null) dobField.setText(existingGuest.getDateOfBirth().toString());
    }

    private void save() {
        Guest guest = existingGuest != null ? existingGuest : new Guest();
        guest.setFirstName(firstNameField.getText().trim());
        guest.setLastName(lastNameField.getText().trim());
        guest.setPhone(phoneField.getText().trim());
        guest.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
        guest.setIdProofType((Guest.IdProofType) idProofTypeCombo.getSelectedItem());
        guest.setIdProofNumber(idProofNumberField.getText().trim());
        guest.setAddress(addressField.getText().trim().isEmpty() ? null : addressField.getText().trim());
        guest.setCity(cityField.getText().trim().isEmpty() ? null : cityField.getText().trim());
        guest.setState(null);
        guest.setCountry(countryField.getText().trim().isEmpty() ? "Bangladeh" : countryField.getText().trim());
        guest.setGender((Guest.Gender) genderCombo.getSelectedItem());

        // Parse DOB
        String dobText = dobField.getText().trim();
        if (!dobText.isEmpty()) {
            try {
                guest.setDateOfBirth(LocalDate.parse(dobText));
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                return existingGuest == null ? guestService.addGuest(guest) : guestService.updateGuest(guest);
            }
            @Override protected void done() {
                try {
                    String err = get();
                    if (err == null) {
                        saved = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(GuestDialog.this, err, "Error", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
    e.printStackTrace();

    Throwable cause = e.getCause();

    String errorMessage = cause != null
            ? cause.getMessage()
            : e.getMessage();

    JOptionPane.showMessageDialog(
        GuestDialog.this,
        "Guest Registration Failed!\n\nError: " + errorMessage,
        "Database Error",
        JOptionPane.ERROR_MESSAGE
    );
}
            }
        };
        worker.execute();
    }

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

    public boolean isSaved() { return saved; }
}
