package com.hotelms.service;

import com.hotelms.config.AppConfig;
import com.hotelms.dao.InvoiceDAO;
import com.hotelms.dao.PaymentDAO;
import com.hotelms.model.Invoice;
import com.hotelms.model.Payment;
import com.hotelms.utils.ActivityLogger;
import com.hotelms.utils.FormatUtils;
import com.hotelms.utils.SessionManager;

import java.util.List;
import java.util.Optional;

public class BillingService {

    private final InvoiceDAO invoiceDAO;
    private final PaymentDAO paymentDAO;

    public BillingService() {
        this.invoiceDAO = new InvoiceDAO();
        this.paymentDAO = new PaymentDAO();
    }

    public List<Invoice> getAllInvoices() {
        return invoiceDAO.findAll();
    }

    public Optional<Invoice> getInvoiceByBookingId(int bookingId) {
        return invoiceDAO.findByBookingId(bookingId);
    }

    public Optional<Invoice> getInvoiceById(int invoiceId) {
        return invoiceDAO.findById(invoiceId);
    }

    public List<Payment> getPaymentsForInvoice(int invoiceId) {
        return paymentDAO.findByInvoiceId(invoiceId);
    }

    /**
     * Auto-generates an invoice when a booking is created.
     * Room charges = pricePerNight × nights.
     * No GST/tax is applied.
     */
    public int generateInvoice(int bookingId, double pricePerNight, long nights) {
        // Don't create a duplicate invoice
        var existing = invoiceDAO.findByBookingId(bookingId);
        if (existing.isPresent()) return existing.get().getInvoiceId();

        double roomCharges = pricePerNight * nights;
        double taxAmount   = 0.0;
        double netAmount   = roomCharges;

        Invoice inv = new Invoice();
        inv.setBookingId(bookingId);
        inv.setInvoiceNumber(FormatUtils.generateInvoiceNumber(invoiceDAO.getNextSequence()));
        inv.setRoomCharges(roomCharges);
        inv.setServiceCharges(0.0);
        inv.setTaxPercent(0.0);
        inv.setTaxAmount(taxAmount);
        inv.setDiscountPercent(0.0);
        inv.setDiscountAmount(0.0);
        inv.setNetAmount(netAmount);
        inv.setPaymentStatus(Invoice.PaymentStatus.PENDING);

        int id = invoiceDAO.insert(inv);
        ActivityLogger.log(SessionManager.getInstance().getUserId(),
            "Generated invoice " + inv.getInvoiceNumber() + " for booking #" + bookingId,
            ActivityLogger.MODULE_BILLING, id, null);
        return id;
    }

    /**
     * Recalculates and updates an invoice's service charges and totals.
     * Called after adding service orders to a booking.
     */
    public String updateInvoiceCharges(int invoiceId, double serviceCharges, double discountPercent) {
        var invOpt = invoiceDAO.findById(invoiceId);
        if (invOpt.isEmpty()) return "Invoice not found";

        Invoice inv = invOpt.get();
        double subtotal       = inv.getRoomCharges() + serviceCharges;
        double discountAmount = subtotal * (discountPercent / 100.0);
        double taxable        = subtotal - discountAmount;
        double taxAmount      = 0.0;
        double netAmount      = taxable;

        inv.setServiceCharges(serviceCharges);
        inv.setDiscountPercent(discountPercent);
        inv.setDiscountAmount(discountAmount);
        inv.setTaxAmount(taxAmount);
        inv.setNetAmount(netAmount);

        invoiceDAO.update(inv);
        return null;
    }

    /**
     * Records a payment against an invoice.
     * Automatically updates invoice payment_status (PENDING → PARTIAL → PAID).
     * @return null on success, error message on failure
     */
    public String recordPayment(int invoiceId, double amount, Payment.PaymentMethod method,
                                String transactionRef, String paidBy, String remarks) {
        if (amount <= 0) return "Payment amount must be greater than zero";

        var invOpt = invoiceDAO.findById(invoiceId);
        if (invOpt.isEmpty()) return "Invoice not found";

        Invoice inv = invOpt.get();

        if (inv.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
            return "Invoice " + inv.getInvoiceNumber() + " is already fully paid";
        }

        double alreadyPaid = paymentDAO.getTotalPaidForInvoice(invoiceId);
        double outstanding = inv.getNetAmount() - alreadyPaid;

        if (amount > outstanding + 0.01) { // 0.01 tolerance for floating point
            return "Payment amount (" + FormatUtils.formatCurrency(amount) +
                   ") exceeds outstanding balance (" + FormatUtils.formatCurrency(outstanding) + ")";
        }

        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setAmountPaid(amount);
        payment.setPaymentMethod(method);
        payment.setTransactionRef(transactionRef);
        payment.setPaidBy(paidBy);
        payment.setProcessedBy(SessionManager.getInstance().getUserId());
        payment.setRemarks(remarks);

        int payId = paymentDAO.insert(payment);
        if (payId <= 0) return "Failed to record payment";

        // Determine new payment status
        double newTotalPaid = alreadyPaid + amount;
        Invoice.PaymentStatus newStatus;
        if (newTotalPaid >= inv.getNetAmount() - 0.01) {
            newStatus = Invoice.PaymentStatus.PAID;
        } else {
            newStatus = Invoice.PaymentStatus.PARTIAL;
        }
        invoiceDAO.updatePaymentStatus(invoiceId, newStatus);

        ActivityLogger.log(SessionManager.getInstance().getUserId(),
            "Recorded payment " + FormatUtils.formatCurrency(amount) + " for invoice " + inv.getInvoiceNumber(),
            ActivityLogger.MODULE_PAYMENT, payId, method.getDisplay());

        return null;
    }

    /** Apply a discount to an invoice before payment */
    public String applyDiscount(int invoiceId, double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100)
            return "Discount must be between 0% and 100%";

        if (!SessionManager.getInstance().isReceptionist())
            return "Insufficient permissions to apply discount";

        var invOpt = invoiceDAO.findById(invoiceId);
        if (invOpt.isEmpty()) return "Invoice not found";

        Invoice inv = invOpt.get();
        double subtotal       = inv.getRoomCharges() + inv.getServiceCharges();
        double discountAmount = subtotal * (discountPercent / 100.0);
        double taxable        = subtotal - discountAmount;
        double taxAmount      = 0.0;

        inv.setDiscountPercent(discountPercent);
        inv.setDiscountAmount(discountAmount);
        inv.setTaxPercent(0.0);
        inv.setTaxAmount(taxAmount);
        inv.setNetAmount(taxable);

        invoiceDAO.update(inv);
        return null;
    }
}
