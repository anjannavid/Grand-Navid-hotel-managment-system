package com.hotelms.dao;

import com.hotelms.database.DatabaseConnection;
import com.hotelms.model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentDAO {

    private static final Logger LOGGER = Logger.getLogger(PaymentDAO.class.getName());

    public List<Payment> findByInvoiceId(int invoiceId) {
        List<Payment> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT p.*, i.invoice_number, u.full_name AS processed_by_name " +
                "FROM payments p " +
                "JOIN invoices i ON p.invoice_id = i.invoice_id " +
                "LEFT JOIN users u ON p.processed_by = u.user_id " +
                "WHERE p.invoice_id = ? ORDER BY p.paid_at");
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByInvoiceId payments failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public double getTotalPaidForInvoice(int invoiceId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount_paid), 0) FROM payments WHERE invoice_id = ?");
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getTotalPaid failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return 0.0;
    }

    public int insert(Payment p) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO payments (invoice_id, amount_paid, payment_method, transaction_ref, " +
                "paid_by, processed_by, remarks) VALUES (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getInvoiceId());
            ps.setDouble(2, p.getAmountPaid());
            ps.setString(3, p.getPaymentMethod().name());
            ps.setString(4, p.getTransactionRef());
            ps.setString(5, p.getPaidBy());
            if (p.getProcessedBy() > 0) ps.setInt(6, p.getProcessedBy());
            else ps.setNull(6, Types.INTEGER);
            ps.setString(7, p.getRemarks());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            keys.close(); ps.close();
            return id;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert payment failed", e);
            throw new RuntimeException("Failed to record payment: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setInvoiceId(rs.getInt("invoice_id"));
        p.setAmountPaid(rs.getDouble("amount_paid"));
        p.setPaymentMethod(Payment.PaymentMethod.fromString(rs.getString("payment_method")));
        p.setTransactionRef(rs.getString("transaction_ref"));
        p.setPaidBy(rs.getString("paid_by"));
        p.setRemarks(rs.getString("remarks"));
        Timestamp paid = rs.getTimestamp("paid_at");
        if (paid != null) p.setPaidAt(paid.toLocalDateTime());
        try { p.setInvoiceNumber(rs.getString("invoice_number")); } catch (SQLException ignored) {}
        try { p.setProcessedByName(rs.getString("processed_by_name")); } catch (SQLException ignored) {}
        return p;
    }
}
