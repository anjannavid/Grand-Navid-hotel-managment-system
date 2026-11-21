package com.hotelms.dao;

import com.hotelms.database.DatabaseConnection;
import com.hotelms.model.Booking;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingDAO {

    private static final Logger LOGGER = Logger.getLogger(BookingDAO.class.getName());

    // JOIN with guests and rooms for denormalized view
    private static final String SELECT_FULL =
        "SELECT b.booking_id, b.room_id, b.guest_id, b.user_id, b.check_in_date, b.check_out_date, " +
        "b.actual_checkin, b.actual_checkout, b.num_adults, b.num_children, b.special_requests, " +
        "b.status, b.total_amount, b.advance_paid, b.notes, b.booked_at, " +
        "CONCAT(g.first_name,' ',g.last_name) AS guest_name, g.phone AS guest_phone, " +
        "r.room_number, r.room_type " +
        "FROM bookings b " +
        "JOIN guests g ON b.guest_id = g.guest_id " +
        "JOIN rooms  r ON b.room_id  = r.room_id ";

    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL + "ORDER BY b.booked_at DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll bookings failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public Optional<Booking> findById(int bookingId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL + "WHERE b.booking_id = ?");
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findById booking failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public List<Booking> findByGuestId(int guestId) {
        List<Booking> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL + "WHERE b.guest_id = ? ORDER BY b.check_in_date DESC");
            ps.setInt(1, guestId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByGuestId failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public List<Booking> findByStatus(Booking.BookingStatus status) {
        List<Booking> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL + "WHERE b.status = ? ORDER BY b.check_in_date");
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByStatus failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    /** All bookings checking in today */
    public List<Booking> findTodayCheckIns() {
        List<Booking> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL +
                "WHERE b.check_in_date = CURDATE() AND b.status = 'CONFIRMED'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "findTodayCheckIns failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    /** All bookings checking out today */
    public List<Booking> findTodayCheckOuts() {
        List<Booking> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL +
                "WHERE b.check_out_date = CURDATE() AND b.status = 'CHECKED_IN'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "findTodayCheckOuts failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public List<Booking> findRecentBookings(int limit) {
        List<Booking> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL + "ORDER BY b.booked_at DESC LIMIT ?");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "findRecentBookings failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public List<Booking> search(String keyword) {
        List<Booking> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String pattern = "%" + keyword.toLowerCase() + "%";
            PreparedStatement ps = conn.prepareStatement(SELECT_FULL +
                "WHERE LOWER(g.first_name) LIKE ? OR LOWER(g.last_name) LIKE ? " +
                "OR g.phone LIKE ? OR r.room_number LIKE ? OR CAST(b.booking_id AS CHAR) LIKE ? " +
                "ORDER BY b.booked_at DESC");
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "booking search failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    
    public boolean hasConflict(int roomId, LocalDate checkIn, LocalDate checkOut, int excludeBookingId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM bookings " +
                "WHERE room_id = ? AND booking_id != ? " +
                "AND status IN ('CONFIRMED','CHECKED_IN') " +
                "AND check_in_date < ? AND check_out_date > ?");
            ps.setInt(1, roomId);
            ps.setInt(2, excludeBookingId);
            ps.setDate(3, Date.valueOf(checkOut));
            ps.setDate(4, Date.valueOf(checkIn));
            ResultSet rs = ps.executeQuery();
            boolean conflict = rs.next() && rs.getInt(1) > 0;
            rs.close(); ps.close();
            return conflict;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "hasConflict check failed", e);
            return true; // fail-safe: assume conflict on error
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public int countByStatus(Booking.BookingStatus status) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM bookings WHERE status = ?");
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countByStatus failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public int countTotal() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM bookings");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countTotal bookings failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    // Revenue for today
    public double getTodayRevenue() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount_paid), 0) FROM payments WHERE DATE(paid_at) = CURDATE()");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getTodayRevenue failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return 0.0;
    }

    // Monthly revenue for chart
    public List<double[]> getMonthlyRevenue(int year) {
        List<double[]> data = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT MONTH(paid_at) as month, SUM(amount_paid) as revenue " +
                "FROM payments WHERE YEAR(paid_at) = ? GROUP BY MONTH(paid_at) ORDER BY month");
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) data.add(new double[]{rs.getInt("month"), rs.getDouble("revenue")});
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getMonthlyRevenue failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return data;
    }

    // WRITE

    public int insert(Booking b) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO bookings (room_id, guest_id, user_id, check_in_date, check_out_date, " +
                "num_adults, num_children, special_requests, status, total_amount, advance_paid, notes) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, b.getRoomId());
            ps.setInt(2, b.getGuestId());
            ps.setInt(3, b.getUserId());
            ps.setDate(4, Date.valueOf(b.getCheckInDate()));
            ps.setDate(5, Date.valueOf(b.getCheckOutDate()));
            ps.setInt(6, b.getNumAdults());
            ps.setInt(7, b.getNumChildren());
            ps.setString(8, b.getSpecialRequests());
            ps.setString(9, b.getStatus().name());
            ps.setDouble(10, b.getTotalAmount());
            ps.setDouble(11, b.getAdvancePaid());
            ps.setString(12, b.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            keys.close(); ps.close();
            return id;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert booking failed", e);
            throw new RuntimeException("Failed to create booking: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean updateStatus(int bookingId, Booking.BookingStatus status) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET status=? WHERE booking_id=?");
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateStatus booking failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean checkIn(int bookingId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET status='CHECKED_IN', actual_checkin=CURRENT_TIMESTAMP WHERE booking_id=?");
            ps.setInt(1, bookingId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "checkIn failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean checkOut(int bookingId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET status='CHECKED_OUT', actual_checkout=CURRENT_TIMESTAMP WHERE booking_id=?");
            ps.setInt(1, bookingId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "checkOut failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean update(Booking b) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET room_id=?, check_in_date=?, check_out_date=?, num_adults=?, " +
                "num_children=?, special_requests=?, total_amount=?, advance_paid=?, notes=? WHERE booking_id=?");
            ps.setInt(1, b.getRoomId());
            ps.setDate(2, Date.valueOf(b.getCheckInDate()));
            ps.setDate(3, Date.valueOf(b.getCheckOutDate()));
            ps.setInt(4, b.getNumAdults());
            ps.setInt(5, b.getNumChildren());
            ps.setString(6, b.getSpecialRequests());
            ps.setDouble(7, b.getTotalAmount());
            ps.setDouble(8, b.getAdvancePaid());
            ps.setString(9, b.getNotes());
            ps.setInt(10, b.getBookingId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "update booking failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // Row Mapper
    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setRoomId(rs.getInt("room_id"));
        b.setGuestId(rs.getInt("guest_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        b.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        Timestamp ci = rs.getTimestamp("actual_checkin");
        if (ci != null) b.setActualCheckin(ci.toLocalDateTime());
        Timestamp co = rs.getTimestamp("actual_checkout");
        if (co != null) b.setActualCheckout(co.toLocalDateTime());
        b.setNumAdults(rs.getInt("num_adults"));
        b.setNumChildren(rs.getInt("num_children"));
        b.setSpecialRequests(rs.getString("special_requests"));
        b.setStatus(Booking.BookingStatus.fromString(rs.getString("status")));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setAdvancePaid(rs.getDouble("advance_paid"));
        b.setNotes(rs.getString("notes"));
        Timestamp ba = rs.getTimestamp("booked_at");
        if (ba != null) b.setBookedAt(ba.toLocalDateTime());
        b.setGuestName(rs.getString("guest_name"));
        b.setGuestPhone(rs.getString("guest_phone"));
        b.setRoomNumber(rs.getString("room_number"));
        b.setRoomType(rs.getString("room_type"));
        return b;
    }
}
