package com.hotelms.model;

import java.time.LocalDateTime;

public class Invoice {

    public enum PaymentStatus {
        PENDING("Pending"),
        PARTIAL("Partial"),
        PAID("Paid"),
        REFUNDED("Refunded");

        private final String display;
        PaymentStatus(String d) { this.display = d; }
        public String getDisplay() { return display; }

        public static PaymentStatus fromString(String v) {
            return switch (v.toUpperCase()) {
                case "PARTIAL"  -> PARTIAL;
                case "PAID"     -> PAID;
                case "REFUNDED" -> REFUNDED;
                default         -> PENDING;
            };
        }
    }

    private int invoiceId;
    private int bookingId;
    private String invoiceNumber;
    private double roomCharges;
    private double serviceCharges;
    private double taxPercent    = 0.0;
    private double taxAmount;
    private double discountPercent;
    private double discountAmount;
    private double netAmount;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String notes;
    private LocalDateTime generatedAt;

    // Denormalized
    private String guestName;
    private String roomNumber;

    public Invoice() {}

    // Getters & Setters
    public int getInvoiceId()                          { return invoiceId; }
    public void setInvoiceId(int id)                   { this.invoiceId = id; }
    public int getBookingId()                          { return bookingId; }
    public void setBookingId(int id)                   { this.bookingId = id; }
    public String getInvoiceNumber()                   { return invoiceNumber; }
    public void setInvoiceNumber(String n)             { this.invoiceNumber = n; }
    public double getRoomCharges()                     { return roomCharges; }
    public void setRoomCharges(double c)               { this.roomCharges = c; }
    public double getServiceCharges()                  { return serviceCharges; }
    public void setServiceCharges(double c)            { this.serviceCharges = c; }
    public double getTaxPercent()                      { return taxPercent; }
    public void setTaxPercent(double p)                { this.taxPercent = p; }
    public double getTaxAmount()                       { return taxAmount; }
    public void setTaxAmount(double a)                 { this.taxAmount = a; }
    public double getDiscountPercent()                 { return discountPercent; }
    public void setDiscountPercent(double p)           { this.discountPercent = p; }
    public double getDiscountAmount()                  { return discountAmount; }
    public void setDiscountAmount(double a)            { this.discountAmount = a; }
    public double getNetAmount()                       { return netAmount; }
    public void setNetAmount(double a)                 { this.netAmount = a; }
    public PaymentStatus getPaymentStatus()            { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus s)      { this.paymentStatus = s; }
    public String getNotes()                           { return notes; }
    public void setNotes(String n)                     { this.notes = n; }
    public LocalDateTime getGeneratedAt()              { return generatedAt; }
    public void setGeneratedAt(LocalDateTime t)        { this.generatedAt = t; }
    public String getGuestName()                       { return guestName; }
    public void setGuestName(String n)                 { this.guestName = n; }
    public String getRoomNumber()                      { return roomNumber; }
    public void setRoomNumber(String n)                { this.roomNumber = n; }

    public double getSubtotal() { return roomCharges + serviceCharges; }

    @Override public String toString() {
        return "Invoice{" + invoiceNumber + ", ৳" + netAmount + ", " + paymentStatus + "}";
    }
}
