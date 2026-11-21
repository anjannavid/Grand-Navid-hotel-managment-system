package com.hotelms.ui.panels;

import com.hotelms.dao.UserDAO;
import com.hotelms.model.User;
import com.hotelms.service.AuthService;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.ModernTable;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.utils.ActivityLogger;
import com.hotelms.utils.FormatUtils;
import com.hotelms.utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StaffPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;
    private final UserDAO userDAO;

    private ModernTable table;
    private DefaultTableModel tableModel;

    private static final String[] COLUMNS = {"ID", "Username", "Full Name", "Email", "Phone", "Role", "Active", "Last Login"};

    public StaffPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authService = new AuthService();
        this.userDAO = new UserDAO();

        setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        buildUI();
    }

    private void buildUI() {
        // Header — standardized
        JButton addBtn = ThemeManager.primaryButton("+ Add Staff");
        addBtn.addActionListener(e -> openAddStaff());
        JPanel header = ThemeManager.createHeaderPanel("Staff Management", "Manage staff accounts and roles (Admin only)", addBtn);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new ModernTable(tableModel);
        table.setColumnWidth(0, 40);
        table.setCenterColumn(6);

        // Bottom buttons — standardized
        JButton editBtn = ThemeManager.outlineButton("✏  Edit Role");
        editBtn.addActionListener(e -> editStaffRole());

        JButton toggleBtn = ThemeManager.outlineButton("🔒  Toggle Active");
        toggleBtn.addActionListener(e -> toggleActive());

        JButton resetPwdBtn = ThemeManager.outlineButton("🔑  Reset Password");
        resetPwdBtn.addActionListener(e -> resetPassword());

        JPanel bottomBar = ThemeManager.createBottomBar(editBtn, toggleBtn, resetPwdBtn);

        add(header, BorderLayout.NORTH);
        add(table.inScrollPane(), BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void openAddStaff() {
        JDialog dialog = ThemeManager.createStyledDialog(mainFrame, "Add New Staff", true);

        JPanel mainPanel = new JPanel(new BorderLayout(0, ThemeManager.PADDING_LG));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));
        mainPanel.setOpaque(false);

        // Header
        JLabel titleLbl = new JLabel("Add New Staff");
        titleLbl.setFont(ThemeManager.FONT_H2);
        titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);
        mainPanel.add(titleLbl, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(6, 1, 0, ThemeManager.PADDING_SM));
        form.setOpaque(false);

        JTextField usernameField = ThemeManager.textField("Username");
        JPasswordField passwordField = ThemeManager.passwordField("Password");
        JTextField fullNameField = ThemeManager.textField("Full Name");
        JTextField emailField = ThemeManager.textField("Email Address");
        JTextField phoneField = ThemeManager.textField("Phone Number");
        JComboBox<User.Role> roleCombo = ThemeManager.styledComboBox(User.Role.values());

        form.add(ThemeManager.formFieldPanel("Username", usernameField));
        form.add(ThemeManager.formFieldPanel("Password", passwordField));
        form.add(ThemeManager.formFieldPanel("Full Name", fullNameField));
        form.add(ThemeManager.formFieldPanel("Email", emailField));
        form.add(ThemeManager.formFieldPanel("Phone", phoneField));
        form.add(ThemeManager.formFieldPanel("Role", roleCombo));

        mainPanel.add(form, BorderLayout.CENTER);

        // Actions
        JButton saveBtn = ThemeManager.primaryButton("Save Staff");
        JButton cancelBtn = ThemeManager.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override protected String doInBackground() {
                    return authService.createUser(
                        usernameField.getText().trim(),
                        new String(passwordField.getPassword()),
                        fullNameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        (User.Role) roleCombo.getSelectedItem()
                    );
                }
                @Override protected void done() {
                    try {
                        String err = get();
                        if (err == null) {
                            dialog.dispose();
                            mainFrame.showSuccess("Staff account created");
                            refresh();
                        } else {
                            JOptionPane.showMessageDialog(dialog, err, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Failed to create staff account", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, ThemeManager.PADDING_SM, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(cancelBtn);
        actionPanel.add(saveBtn);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setSize(400, dialog.getHeight() + 20);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    private void editStaffRole() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { mainFrame.showWarning("Select a staff member"); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int userId = (int) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);

        // Don't allow editing self
        if (userId == SessionManager.getInstance().getUserId()) {
            mainFrame.showWarning("You cannot change your own role");
            return;
        }

        User.Role newRole = (User.Role) JOptionPane.showInputDialog(mainFrame,
            "Select new role for " + username + ":",
            "Change Role",
            JOptionPane.QUESTION_MESSAGE,
            null,
            User.Role.values(),
            User.Role.STAFF);

        if (newRole != null) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override protected Boolean doInBackground() {
                    var userOpt = userDAO.findById(userId);
                    if (userOpt.isEmpty()) return false;
                    User user = userOpt.get();
                    user.setRole(newRole);
                    boolean ok = userDAO.update(user);
                    if (ok) {
                        ActivityLogger.log(SessionManager.getInstance().getUserId(),
                            "Changed role of " + username + " to " + newRole.getDisplayName(),
                            ActivityLogger.MODULE_STAFF, userId, null);
                    }
                    return ok;
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            mainFrame.showSuccess("Role updated for " + username);
                            refresh();
                        } else {
                            mainFrame.showError("Failed to update role");
                        }
                    } catch (Exception e) {
                        mainFrame.showError("Operation failed");
                    }
                }
            };
            worker.execute();
        }
    }

    private void toggleActive() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { mainFrame.showWarning("Select a staff member"); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int userId = (int) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);
        String activeStr = tableModel.getValueAt(modelRow, 6).toString();
        boolean currentlyActive = "Yes".equals(activeStr);

        if (userId == SessionManager.getInstance().getUserId()) {
            mainFrame.showWarning("You cannot deactivate your own account");
            return;
        }

        String action = currentlyActive ? "deactivate" : "activate";
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
            "Are you sure you want to " + action + " user '" + username + "'?",
            "Confirm " + action, JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override protected Boolean doInBackground() {
                    boolean ok = userDAO.setActive(userId, !currentlyActive);
                    if (ok) {
                        ActivityLogger.log(SessionManager.getInstance().getUserId(),
                            (currentlyActive ? "Deactivated" : "Activated") + " user: " + username,
                            ActivityLogger.MODULE_STAFF, userId, null);
                    }
                    return ok;
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            mainFrame.showSuccess("User " + username + " " + action + "d");
                            refresh();
                        } else {
                            mainFrame.showError("Failed to " + action + " user");
                        }
                    } catch (Exception e) {
                        mainFrame.showError("Operation failed");
                    }
                }
            };
            worker.execute();
        }
    }

    private void resetPassword() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { mainFrame.showWarning("Select a staff member"); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int userId = (int) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);

        JPasswordField newPwdField = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(mainFrame,
            new Object[]{"Enter new password for " + username + ":", newPwdField},
            "Reset Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPwd = new String(newPwdField.getPassword());
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override protected String doInBackground() {
                    return authService.changePassword(userId, null, newPwd);
                }
                @Override protected void done() {
                    try {
                        String err = get();
                        if (err == null) {
                            mainFrame.showSuccess("Password reset for " + username);
                        } else {
                            mainFrame.showError(err);
                        }
                    } catch (Exception e) {
                        mainFrame.showError("Failed to reset password");
                    }
                }
            };
            worker.execute();
        }
    }

    public void refresh() {
        if (!SessionManager.getInstance().isAdmin()) return;

        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override protected List<User> doInBackground() {
                return userDAO.findAll();
            }
            @Override protected void done() {
                try {
                    List<User> users = get();
                    tableModel.setRowCount(0);
                    for (User u : users) {
                        tableModel.addRow(new Object[]{
                            u.getUserId(),
                            u.getUsername(),
                            u.getFullName(),
                            u.getEmail() != null ? u.getEmail() : "—",
                            u.getPhone() != null ? u.getPhone() : "—",
                            u.getRole().getDisplayName(),
                            u.isActive() ? "Yes" : "No",
                            FormatUtils.formatDateTime(u.getLastLogin())
                        });
                    }
                } catch (Exception e) {
                    mainFrame.showError("Failed to load staff");
                }
            }
        };
        worker.execute();
    }
}
