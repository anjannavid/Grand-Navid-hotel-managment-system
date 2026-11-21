package com.hotelms.ui.panels;

import com.hotelms.config.AppConfig;
import com.hotelms.model.Invoice;
import com.hotelms.model.Payment;
import com.hotelms.service.BillingService;
import com.hotelms.ui.MainFrame;
import com.hotelms.ui.components.ModernTable;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.ui.dialogs.PaymentDialog;
import com.hotelms.utils.FormatUtils;
import com.hotelms.utils.SessionManager;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class BillingPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BillingService billingService;

    private ModernTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMNS = {
        "ID", "Invoice #", "Booking", "Guest", "Room", "Room Charges",
        "Discount", "Net Amount", "Status"
    };

    public BillingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.billingService = new BillingService();

        setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG,
            ThemeManager.PADDING_LG, ThemeManager.PADDING_LG));

        buildUI();
    }

    private void buildUI() {
        // Header — standardized
        JPanel header = ThemeManager.createHeaderPanel("Billing & Invoices", "Manage invoices and payments");

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new ModernTable(tableModel);
        table.setColumnWidth(0, 40);
        table.setCurrencyColumn(5);
        table.setCurrencyColumn(6);
        table.setCurrencyColumn(7);
        table.setStatusColumn(8);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Bottom buttons — standardized
        JButton viewBtn = ThemeManager.outlineButton("📋  View Invoice");
        viewBtn.addActionListener(e -> viewInvoice());

        JButton payBtn = ThemeManager.successButton("💳  Record Payment");
        payBtn.addActionListener(e -> recordPayment());

        JButton discountBtn = ThemeManager.outlineButton("🏷  Apply Discount");
        discountBtn.addActionListener(e -> applyDiscount());

        JButton pdfBtn = ThemeManager.primaryButton("📄  Export PDF");
        pdfBtn.addActionListener(e -> exportPDF());

        JPanel bottomBar;
        if (SessionManager.getInstance().isReceptionist()) {
            bottomBar = ThemeManager.createBottomBar(viewBtn, payBtn, discountBtn, pdfBtn);
        } else {
            bottomBar = ThemeManager.createBottomBar(viewBtn, payBtn, pdfBtn);
        }

        add(header, BorderLayout.NORTH);
        add(table.inScrollPane(), BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private int getSelectedInvoiceId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return -1;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (int) tableModel.getValueAt(modelRow, 0);
    }

    private void viewInvoice() {
        int invoiceId = getSelectedInvoiceId();
        if (invoiceId < 0) { mainFrame.showWarning("Select an invoice"); return; }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Invoice inv;
            List<Payment> payments;

            @Override protected Void doInBackground() {
                inv = billingService.getInvoiceById(invoiceId).orElse(null);
                payments = billingService.getPaymentsForInvoice(invoiceId);
                return null;
            }
            @Override protected void done() {
                if (inv == null) { mainFrame.showError("Invoice not found"); return; }
                showInvoiceDetail(inv, payments);
            }
        };
        worker.execute();
    }

    private void showInvoiceDetail(Invoice inv, List<Payment> payments) {
        JDialog dialog = new JDialog(mainFrame, "Invoice — " + inv.getInvoiceNumber(), true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel content = new JPanel();
        content.setBackground(ThemeManager.BG_SECONDARY);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Invoice header
        addDetailRow(content, "Invoice #", inv.getInvoiceNumber());
        addDetailRow(content, "Guest", inv.getGuestName() != null ? inv.getGuestName() : "—");
        addDetailRow(content, "Room", inv.getRoomNumber() != null ? inv.getRoomNumber() : "—");
        addDetailRow(content, "Generated", FormatUtils.formatDateTime(inv.getGeneratedAt()));

        content.add(Box.createVerticalStrut(12));
        content.add(new JSeparator());
        content.add(Box.createVerticalStrut(12));

        addDetailRow(content, "Room Charges", FormatUtils.formatCurrency(inv.getRoomCharges()));
        addDetailRow(content, "Service Charges", FormatUtils.formatCurrency(inv.getServiceCharges()));
        addDetailRow(content, "Subtotal", FormatUtils.formatCurrency(inv.getSubtotal()));
        if (inv.getDiscountAmount() > 0) {
            addDetailRow(content, "Discount (" + inv.getDiscountPercent() + "%)", "−" + FormatUtils.formatCurrency(inv.getDiscountAmount()));
        }

        content.add(Box.createVerticalStrut(8));
        JLabel netLabel = new JLabel("Net Amount: " + FormatUtils.formatCurrency(inv.getNetAmount()));
        netLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        netLabel.setForeground(ThemeManager.ACCENT_SUCCESS);
        netLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(netLabel);

        content.add(Box.createVerticalStrut(8));
        addDetailRow(content, "Status", inv.getPaymentStatus().getDisplay());

        // Payments list
        if (!payments.isEmpty()) {
            content.add(Box.createVerticalStrut(12));
            content.add(new JSeparator());
            content.add(Box.createVerticalStrut(8));

            JLabel payTitle = new JLabel("Payments");
            payTitle.setFont(ThemeManager.FONT_H3);
            payTitle.setForeground(ThemeManager.TEXT_PRIMARY);
            payTitle.setAlignmentX(LEFT_ALIGNMENT);
            content.add(payTitle);
            content.add(Box.createVerticalStrut(6));

            for (Payment p : payments) {
                addDetailRow(content,
                    p.getPaymentMethod().getDisplay() + (p.getTransactionRef() != null ? " (" + p.getTransactionRef() + ")" : ""),
                    FormatUtils.formatCurrency(p.getAmountPaid()) + "  •  " + FormatUtils.formatDateTime(p.getPaidAt()));
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dialog.setContentPane(scroll);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(ThemeManager.FONT_BODY);
        lblLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(ThemeManager.FONT_BOLD);
        valLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        row.add(lblLabel, BorderLayout.WEST);
        row.add(valLabel, BorderLayout.EAST);
        parent.add(row);
        parent.add(Box.createVerticalStrut(4));
    }

    private void recordPayment() {
        int invoiceId = getSelectedInvoiceId();
        if (invoiceId < 0) { mainFrame.showWarning("Select an invoice"); return; }

        billingService.getInvoiceById(invoiceId).ifPresent(inv -> {
            PaymentDialog dialog = new PaymentDialog(mainFrame, inv);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                mainFrame.showSuccess("Payment recorded successfully");
                refresh();
                mainFrame.refreshDashboard();
            }
        });
    }

    private void applyDiscount() {
        int invoiceId = getSelectedInvoiceId();
        if (invoiceId < 0) { mainFrame.showWarning("Select an invoice"); return; }

        String input = JOptionPane.showInputDialog(mainFrame,
            "Enter discount percentage (0-100):", "Apply Discount", JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double pct = Double.parseDouble(input.trim());
                SwingWorker<String, Void> worker = new SwingWorker<>() {
                    @Override protected String doInBackground() {
                        return billingService.applyDiscount(invoiceId, pct);
                    }
                    @Override protected void done() {
                        try {
                            String err = get();
                            if (err == null) {
                                mainFrame.showSuccess("Discount applied");
                                refresh();
                            } else {
                                mainFrame.showError(err);
                            }
                        } catch (Exception e) {
                            mainFrame.showError("Failed to apply discount");
                        }
                    }
                };
                worker.execute();
            } catch (NumberFormatException e) {
                mainFrame.showError("Invalid discount value");
            }
        }
    }

    private void exportPDF() {
        int invoiceId = getSelectedInvoiceId();
        if (invoiceId < 0) { mainFrame.showWarning("Select an invoice to export"); return; }

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                try {
                    Invoice inv = billingService.getInvoiceById(invoiceId).orElse(null);
                    if (inv == null) return "Invoice not found";

                    List<Payment> payments = billingService.getPaymentsForInvoice(invoiceId);

                    // Create PDF
                    String dir = AppConfig.PDF_OUTPUT_DIR;
                    new File(dir).mkdirs();
                    String filePath = dir + inv.getInvoiceNumber() + ".pdf";

                    Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
                    PdfWriter.getInstance(doc, new FileOutputStream(filePath));
                    doc.open();

                    // Title
                    Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                    Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
                    Font boldFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
                    Font headerFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);

                    Paragraph hotelName = new Paragraph(AppConfig.HOTEL_NAME, titleFont);
                    hotelName.setAlignment(Element.ALIGN_CENTER);
                    doc.add(hotelName);

                    Paragraph address = new Paragraph(AppConfig.HOTEL_ADDRESS, normalFont);
                    address.setAlignment(Element.ALIGN_CENTER);
                    doc.add(address);

                    doc.add(new Paragraph(" "));
                    doc.add(new Paragraph("INVOICE", headerFont));
                    doc.add(new Paragraph(" "));

                    // Invoice details table
                    PdfPTable detailTable = new PdfPTable(2);
                    detailTable.setWidthPercentage(100);
                    addPdfRow(detailTable, "Invoice #:", inv.getInvoiceNumber(), boldFont, normalFont);
                    addPdfRow(detailTable, "Guest:", inv.getGuestName() != null ? inv.getGuestName() : "—", boldFont, normalFont);
                    addPdfRow(detailTable, "Room:", inv.getRoomNumber() != null ? inv.getRoomNumber() : "—", boldFont, normalFont);
                    addPdfRow(detailTable, "Date:", FormatUtils.formatDateTime(inv.getGeneratedAt()), boldFont, normalFont);
                    doc.add(detailTable);

                    doc.add(new Paragraph(" "));

                    // Charges table
                    PdfPTable chargesTable = new PdfPTable(2);
                    chargesTable.setWidthPercentage(100);
                    addPdfRow(chargesTable, "Room Charges:", FormatUtils.formatCurrency(inv.getRoomCharges()), normalFont, normalFont);
                    addPdfRow(chargesTable, "Service Charges:", FormatUtils.formatCurrency(inv.getServiceCharges()), normalFont, normalFont);
                    addPdfRow(chargesTable, "Subtotal:", FormatUtils.formatCurrency(inv.getSubtotal()), normalFont, normalFont);
                    if (inv.getDiscountAmount() > 0) {
                        addPdfRow(chargesTable, "Discount (" + inv.getDiscountPercent() + "%):", "-" + FormatUtils.formatCurrency(inv.getDiscountAmount()), normalFont, normalFont);
                    }
                    addPdfRow(chargesTable, "NET AMOUNT:", FormatUtils.formatCurrency(inv.getNetAmount()), boldFont, boldFont);
                    addPdfRow(chargesTable, "Status:", inv.getPaymentStatus().getDisplay(), normalFont, normalFont);
                    doc.add(chargesTable);

                    // Payments
                    if (!payments.isEmpty()) {
                        doc.add(new Paragraph(" "));
                        doc.add(new Paragraph("Payments", headerFont));
                        for (Payment p : payments) {
                            doc.add(new Paragraph(
                                p.getPaymentMethod().getDisplay() + ": " +
                                FormatUtils.formatCurrency(p.getAmountPaid()) +
                                " on " + FormatUtils.formatDateTime(p.getPaidAt()), normalFont));
                        }
                    }

                    // Footer
                    doc.add(new Paragraph(" "));
                    doc.add(new Paragraph("Thank you for staying with us!", normalFont));

                    doc.close();
                    return filePath;
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }

            @Override protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Error:")) {
                        mainFrame.showError(result);
                    } else {
                        mainFrame.showSuccess("PDF exported: " + result);
                    }
                } catch (Exception e) {
                    mainFrame.showError("PDF export failed");
                }
            }
        };
        worker.execute();
    }

    private void addPdfRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
        cell1.setBorder(PdfPCell.NO_BORDER);
        cell1.setPadding(4);

        PdfPCell cell2 = new PdfPCell(new Phrase(value, valueFont));
        cell2.setBorder(PdfPCell.NO_BORDER);
        cell2.setPadding(4);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(cell1);
        table.addCell(cell2);
    }

    public void refresh() {
        SwingWorker<List<Invoice>, Void> worker = new SwingWorker<>() {
            @Override protected List<Invoice> doInBackground() {
                return billingService.getAllInvoices();
            }
            @Override protected void done() {
                try {
                    List<Invoice> invoices = get();
                    tableModel.setRowCount(0);
                    for (Invoice inv : invoices) {
                        tableModel.addRow(new Object[]{
                            inv.getInvoiceId(),
                            inv.getInvoiceNumber(),
                            inv.getBookingId(),
                            inv.getGuestName() != null ? inv.getGuestName() : "—",
                            inv.getRoomNumber() != null ? inv.getRoomNumber() : "—",
                            FormatUtils.formatCurrency(inv.getRoomCharges()),
                            inv.getDiscountAmount() > 0 ? FormatUtils.formatCurrency(inv.getDiscountAmount()) : "—",
                            FormatUtils.formatCurrency(inv.getNetAmount()),
                            inv.getPaymentStatus().name()
                        });
                    }
                } catch (Exception e) {
                    mainFrame.showError("Failed to load invoices");
                }
            }
        };
        worker.execute();
    }
}
