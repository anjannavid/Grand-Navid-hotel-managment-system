package com.hotelms.dao;

import com.hotelms.database.DatabaseConnection;
import com.hotelms.model.Invoice;
import com.hotelms.model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvoiceDAO {

    private static final Logger LOGGER = Logger.getLogger(InvoiceDAO.class.getName());

    private static final String SELECT_BASE =
        "SELECT i.invoice_id, i.booking_id, i.invoice_number, i.room_charges, i.service_charges, " +
        "i.tax_percent, i.tax_amount, i.discount_percent, i.discount_amount, i.net_amount, " +
        "i.payment_status, i.notes, i.generated_at, " +
        "CONCAT(g.first_name,' ',g.last_name) AS guest_name, r.room_number " +
        "FROM invoices i " +
        "JOIN bookings b ON i.booking_id = b.booking_id " +
        "JOIN guests g   ON b.guest_id   = g.guest_id " +
        "JOIN rooms r    ON b.room_id    = r.room_id ";

    public Optional<Invoice> findByBookingId(int bookingId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE i.booking_id = ?");
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByBookingId invoice failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public Optional<Invoice> findById(int invoiceId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE i.invoice_id = ?");
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findById invoice failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY i.generated_at DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll invoices failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public List<Invoice> findByPaymentStatus(Invoice.PaymentStatus status) {
        List<Invoice> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE i.payment_status = ?");
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByPaymentStatus failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    /** Gets next sequence number for invoice number generation */
    public int getNextSequence() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) + 1 FROM invoices");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getNextSequence failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return 1;
    }

    public int insert(Invoice inv) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO invoices (booking_id, invoice_number, room_charges, service_charges, " +
                "tax_percent, tax_amount, discount_percent, discount_amount, net_amount, payment_status, notes) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, inv.getBookingId());
            ps.setString(2, inv.getInvoiceNumber());
            ps.setDouble(3, inv.getRoomCharges());
            ps.setDouble(4, inv.getServiceCharges());
            ps.setDouble(5, inv.getTaxPercent());
            ps.setDouble(6, inv.getTaxAmount());
            ps.setDouble(7, inv.getDiscountPercent());
            ps.setDouble(8, inv.getDiscountAmount());
            ps.setDouble(9, inv.getNetAmount());
            ps.setString(10, inv.getPaymentStatus().name());
            ps.setString(11, inv.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            keys.close(); ps.close();
            return id;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert invoice failed", e);
            throw new RuntimeException("Failed to create invoice: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean update(Invoice inv) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE invoices SET room_charges=?, service_charges=?, tax_percent=?, tax_amount=?, " +
                "discount_percent=?, discount_amount=?, net_amount=?, payment_status=?, notes=? " +
                "WHERE invoice_id=?");
            ps.setDouble(1, inv.getRoomCharges());
            ps.setDouble(2, inv.getServiceCharges());
            ps.setDouble(3, inv.getTaxPercent());
            ps.setDouble(4, inv.getTaxAmount());
            ps.setDouble(5, inv.getDiscountPercent());
            ps.setDouble(6, inv.getDiscountAmount());
            ps.setDouble(7, inv.getNetAmount());
            ps.setString(8, inv.getPaymentStatus().name());
            ps.setString(9, inv.getNotes());
            ps.setInt(10, inv.getInvoiceId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "update invoice failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean updatePaymentStatus(int invoiceId, Invoice.PaymentStatus status) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE invoices SET payment_status=? WHERE invoice_id=?");
            ps.setString(1, status.name());
            ps.setInt(2, invoiceId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updatePaymentStatus failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setBookingId(rs.getInt("booking_id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        inv.setRoomCharges(rs.getDouble("room_charges"));
        inv.setServiceCharges(rs.getDouble("service_charges"));
        inv.setTaxPercent(rs.getDouble("tax_percent"));
        inv.setTaxAmount(rs.getDouble("tax_amount"));
        inv.setDiscountPercent(rs.getDouble("discount_percent"));
        inv.setDiscountAmount(rs.getDouble("discount_amount"));
        inv.setNetAmount(rs.getDouble("net_amount"));
        inv.setPaymentStatus(Invoice.PaymentStatus.fromString(rs.getString("payment_status")));
        inv.setNotes(rs.getString("notes"));
        Timestamp gen = rs.getTimestamp("generated_at");
        if (gen != null) inv.setGeneratedAt(gen.toLocalDateTime());
        inv.setGuestName(rs.getString("guest_name"));
        inv.setRoomNumber(rs.getString("room_number"));
        return inv;
    }
}
