package com.hotelms.model;

import java.time.LocalDateTime;

public class Payment {

    public enum PaymentMethod {
        CASH("Cash"),
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        
        NET_BANKING("Net Banking"),
        CHEQUE("Cheque"),
        WALLET("Wallet");

        private final String display;
        PaymentMethod(String d) { this.display = d; }
        public String getDisplay() { return display; }

        public static PaymentMethod fromString(String v) {
            return switch (v.toUpperCase()) {
                case "CREDIT_CARD" -> CREDIT_CARD;
                case "DEBIT_CARD"  -> DEBIT_CARD;
               
                case "NET_BANKING" -> NET_BANKING;
                case "CHEQUE"      -> CHEQUE;
                case "WALLET"      -> WALLET;
                default            -> CASH;
            };
        }
    }

    private int paymentId;
    private int invoiceId;
    private double amountPaid;
    private PaymentMethod paymentMethod;
    private String transactionRef;
    private String paidBy;
    private int processedBy;
    private String remarks;
    private LocalDateTime paidAt;

    // Denormalized
    private String invoiceNumber;
    private String processedByName;

    public Payment() {}

    // Getters & Setters
    public int getPaymentId()                        { return paymentId; }
    public void setPaymentId(int id)                 { this.paymentId = id; }
    public int getInvoiceId()                        { return invoiceId; }
    public void setInvoiceId(int id)                 { this.invoiceId = id; }
    public double getAmountPaid()                    { return amountPaid; }
    public void setAmountPaid(double a)              { this.amountPaid = a; }
    public PaymentMethod getPaymentMethod()          { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod m)    { this.paymentMethod = m; }
    public String getTransactionRef()                { return transactionRef; }
    public void setTransactionRef(String r)          { this.transactionRef = r; }
    public String getPaidBy()                        { return paidBy; }
    public void setPaidBy(String p)                  { this.paidBy = p; }
    public int getProcessedBy()                      { return processedBy; }
    public void setProcessedBy(int id)               { this.processedBy = id; }
    public String getRemarks()                       { return remarks; }
    public void setRemarks(String r)                 { this.remarks = r; }
    public LocalDateTime getPaidAt()                 { return paidAt; }
    public void setPaidAt(LocalDateTime t)           { this.paidAt = t; }
    public String getInvoiceNumber()                 { return invoiceNumber; }
    public void setInvoiceNumber(String n)           { this.invoiceNumber = n; }
    public String getProcessedByName()               { return processedByName; }
    public void setProcessedByName(String n)         { this.processedByName = n; }
}
