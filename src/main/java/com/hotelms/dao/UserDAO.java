package com.hotelms.dao;

import com.hotelms.database.DatabaseConnection;
import com.hotelms.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private static final String SELECT_BASE =
        "SELECT user_id, username, password_hash, full_name, email, phone, role, " +
        "is_active, last_login, created_at FROM users ";

    // READ

    public Optional<User> findByUsername(String username) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByUsername failed for: " + username, e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findById failed for id: " + userId, e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY role, full_name");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll users failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return users;
    }

    public List<User> findByRole(User.Role role) {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE role = ? ORDER BY full_name");
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByRole failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return users;
    }

    public boolean existsByUsername(String username) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close(); ps.close();
            return exists;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "existsByUsername check failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean existsByEmail(String email) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close(); ps.close();
            return exists;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "existsByEmail check failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public int countAll() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE is_active = 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countAll users failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    // WRITE

    public int insert(User user) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, full_name, email, phone, role) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            keys.close(); ps.close();
            return id;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert user failed", e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean update(User user) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET full_name=?, email=?, phone=?, role=?, is_active=? WHERE user_id=?");
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            ps.setInt(6, user.getUserId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "update user failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean updatePassword(int userId, String newHash) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET password_hash=? WHERE user_id=?");
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updatePassword failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean updateLastLogin(int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id=?");
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "updateLastLogin failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean setActive(int userId, boolean active) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET is_active=? WHERE user_id=?");
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "setActive failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // Row Mapper
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(User.Role.fromString(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) u.setLastLogin(lastLogin.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) u.setCreatedAt(createdAt.toLocalDateTime());
        return u;
    }
}
