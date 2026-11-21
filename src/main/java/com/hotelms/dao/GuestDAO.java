package com.hotelms.dao;

import com.hotelms.database.DatabaseConnection;
import com.hotelms.model.Guest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuestDAO {

    private static final Logger LOGGER = Logger.getLogger(GuestDAO.class.getName());

    private static final String SELECT_BASE =
        "SELECT guest_id, first_name, last_name, email, phone, id_proof_type, id_proof_number, " +
        "address, city, state, country, nationality, date_of_birth, gender, registered_at FROM guests ";

    public List<Guest> findAll() {
        List<Guest> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY registered_at DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll guests failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public Optional<Guest> findById(int guestId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE guest_id = ?");
            ps.setInt(1, guestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findById guest failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public List<Guest> search(String keyword) {
        List<Guest> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String pattern = "%" + keyword.toLowerCase() + "%";
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE +
                "WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? " +
                "OR phone LIKE ? OR LOWER(email) LIKE ? OR id_proof_number LIKE ? " +
                "ORDER BY first_name");
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "guest search failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public int countAll() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM guests");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countAll guests failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public int insert(Guest g) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO guests (first_name, last_name, email, phone, id_proof_type, " +
                "id_proof_number, address, city, state, country, nationality, date_of_birth, gender) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, g.getFirstName());
            ps.setString(2, g.getLastName());
            ps.setString(3, g.getEmail());
            ps.setString(4, g.getPhone());
            ps.setString(5, g.getIdProofType() != null ? g.getIdProofType().name() : "AADHAR");
            ps.setString(6, g.getIdProofNumber());
            ps.setString(7, g.getAddress());
            ps.setString(8, g.getCity());
            ps.setString(9, g.getState());
            ps.setString(10, g.getCountry() != null ? g.getCountry() : "Bangladesh");
            ps.setString(11, g.getNationality() != null ? g.getNationality() : "Bangladeshi");
            if (g.getDateOfBirth() != null) ps.setDate(12, Date.valueOf(g.getDateOfBirth()));
            else ps.setNull(12, Types.DATE);
            ps.setString(13, g.getGender() != null ? g.getGender().name() : null);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            keys.close(); ps.close();
            return id;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert guest failed", e);
            throw new RuntimeException("Failed to add guest: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean update(Guest g) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE guests SET first_name=?, last_name=?, email=?, phone=?, id_proof_type=?, " +
                "id_proof_number=?, address=?, city=?, state=?, country=?, nationality=? WHERE guest_id=?");
            ps.setString(1, g.getFirstName());
            ps.setString(2, g.getLastName());
            ps.setString(3, g.getEmail());
            ps.setString(4, g.getPhone());
            ps.setString(5, g.getIdProofType() != null ? g.getIdProofType().name() : "AADHAR");
            ps.setString(6, g.getIdProofNumber());
            ps.setString(7, g.getAddress());
            ps.setString(8, g.getCity());
            ps.setString(9, g.getState());
            ps.setString(10, g.getCountry());
            ps.setString(11, g.getNationality());
            ps.setInt(12, g.getGuestId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "update guest failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    private Guest mapRow(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId(rs.getInt("guest_id"));
        g.setFirstName(rs.getString("first_name"));
        g.setLastName(rs.getString("last_name"));
        g.setEmail(rs.getString("email"));
        g.setPhone(rs.getString("phone"));
        String idt = rs.getString("id_proof_type");
        if (idt != null) g.setIdProofType(Guest.IdProofType.fromString(idt));
        g.setIdProofNumber(rs.getString("id_proof_number"));
        g.setAddress(rs.getString("address"));
        g.setCity(rs.getString("city"));
        g.setState(rs.getString("state"));
        g.setCountry(rs.getString("country"));
        g.setNationality(rs.getString("nationality"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) g.setDateOfBirth(dob.toLocalDate());
        String gender = rs.getString("gender");
        if (gender != null) {
            try { g.setGender(Guest.Gender.valueOf(gender)); } catch (Exception ignored) {}
        }
        Timestamp reg = rs.getTimestamp("registered_at");
        if (reg != null) g.setRegisteredAt(reg.toLocalDateTime());
        return g;
    }
}
