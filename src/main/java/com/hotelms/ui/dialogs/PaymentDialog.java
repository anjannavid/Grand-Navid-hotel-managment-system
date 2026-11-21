package com.hotelms.ui.dialogs;

import com.hotelms.model.Invoice;
import com.hotelms.model.Payment;
import com.hotelms.service.BillingService;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.utils.FormatUtils;

import javax.swing.*;
import java.awt.*;

public class PaymentDialog extends JDialog {

    private final BillingService billingService;
    private final Invoice invoice;
    private boolean saved = false;

    private JTextField amountField;
    private JComboBox<Payment.PaymentMethod> methodCombo;
    private JTextField transactionRefField;
    private JTextField paidByField;
    private JTextArea remarksArea;

    public PaymentDialog(JFrame parent, Invoice invoice) {
        super(parent, "Record Payment — " + invoice.getInvoiceNumber(), true);
        this.billingService = new BillingService();
        this.invoice = invoice;

        setSize(440, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(ThemeManager.BG_SECONDARY);

        buildForm();
    }

    private void buildForm() {
        JPanel content = new JPanel();
        content.setBackground(ThemeManager.BG_SECONDARY);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Invoice summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setOpaque(false);
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Calculate outstanding
        double totalPaid = billingService.getPaymentsForInvoice(invoice.getInvoiceId())
            .stream().mapToDouble(Payment::getAmountPaid).sum();
        double outstanding = invoice.getNetAmount() - totalPaid;

        addSummaryLine(summaryPanel, "Net Amount:", FormatUtils.formatCurrency(invoice.getNetAmount()));
        addSummaryLine(summaryPanel, "Already Paid:", FormatUtils.formatCurrency(totalPaid));
        addSummaryLine(summaryPanel, "Outstanding:", FormatUtils.formatCurrency(outstanding));

        content.add(summaryPanel);
        content.add(Box.createVerticalStrut(16));

        // Amount
        content.add(fieldLabel("Payment Amount (৳) *"));
        amountField = ThemeManager.textField(String.format("%.2f", outstanding));
        amountField.setText(String.format("%.2f", outstanding));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(amountField);
        content.add(Box.createVerticalStrut(12));

        // Payment Method
        content.add(fieldLabel("Payment Method *"));
        methodCombo = new JComboBox<>(Payment.PaymentMethod.values());
        methodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(methodCombo);
        content.add(Box.createVerticalStrut(12));

        // Transaction Reference
        content.add(fieldLabel("Transaction Reference"));
        transactionRefField = ThemeManager.textField("e.g., TXN12345 or cheque number");
        transactionRefField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(transactionRefField);
        content.add(Box.createVerticalStrut(12));

        // Paid By
        content.add(fieldLabel("Paid By"));
        paidByField = ThemeManager.textField("Name of the person paying");
        paidByField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(paidByField);
        content.add(Box.createVerticalStrut(12));

        // Remarks
        content.add(fieldLabel("Remarks"));
        remarksArea = new JTextArea(3, 20);
        remarksArea.setFont(ThemeManager.FONT_BODY);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane remarkScroll = new JScrollPane(remarksArea);
        remarkScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        content.add(remarkScroll);
        content.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);

        JButton cancelBtn = ThemeManager.outlineButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton payBtn = ThemeManager.successButton("✓  Record Payment");
        payBtn.addActionListener(e -> recordPayment());

        buttonRow.add(cancelBtn);
        buttonRow.add(payBtn);

        content.add(Box.createVerticalGlue());
        content.add(buttonRow);

        setContentPane(content);
    }

    private void addSummaryLine(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(ThemeManager.FONT_BODY);
        lblLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(ThemeManager.FONT_BOLD);
        valLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        row.add(lblLabel, BorderLayout.WEST);
        row.add(valLabel, BorderLayout.EAST);
        parent.add(row);
    }

    private void recordPayment() {
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Payment.PaymentMethod method = (Payment.PaymentMethod) methodCombo.getSelectedItem();
        String txnRef = transactionRefField.getText().trim();
        String paidBy = paidByField.getText().trim();
        String remarks = remarksArea.getText().trim();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                return billingService.recordPayment(
                    invoice.getInvoiceId(), amount, method,
                    txnRef.isEmpty() ? null : txnRef,
                    paidBy.isEmpty() ? null : paidBy,
                    remarks.isEmpty() ? null : remarks
                );
            }
            @Override protected void done() {
                try {
                    String err = get();
                    if (err == null) {
                        saved = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(PaymentDialog.this, err, "Payment Error", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PaymentDialog.this, "Failed to record payment", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
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
