package com.hotelms.dao;

import com.hotelms.database.DatabaseConnection;
import com.hotelms.model.Room;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomDAO {

    private static final Logger LOGGER = Logger.getLogger(RoomDAO.class.getName());

    private static final String SELECT_BASE =
        "SELECT room_id, room_number, room_type, floor_number, price_per_night, " +
        "max_occupancy, bed_type, amenities, description, status, created_at FROM rooms ";

    // READ

    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY floor_number, room_number");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll rooms failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return rooms;
    }

    public Optional<Room> findById(int roomId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE room_id = ?");
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findById room failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public Optional<Room> findByNumber(String roomNumber) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE room_number = ?");
            ps.setString(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByNumber failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public List<Room> findByStatus(Room.RoomStatus status) {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE status = ? ORDER BY room_number");
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByStatus failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return rooms;
    }

    public List<Room> findByType(Room.RoomType type) {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE room_type = ? ORDER BY room_number");
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByType failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return rooms;
    }

    
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = SELECT_BASE +
                "WHERE status != 'MAINTENANCE' " +
                "AND room_id NOT IN (" +
                "  SELECT DISTINCT room_id FROM bookings " +
                "  WHERE status IN ('CONFIRMED','CHECKED_IN') " +
                "  AND check_in_date < ? AND check_out_date > ?" +
                ") ORDER BY room_type, price_per_night";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(checkOut));
            ps.setDate(2, Date.valueOf(checkIn));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAvailableRooms failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return rooms;
    }

    public List<Room> findAvailableByType(LocalDate checkIn, LocalDate checkOut, Room.RoomType type) {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = SELECT_BASE +
                "WHERE room_type = ? AND status != 'MAINTENANCE' " +
                "AND room_id NOT IN (" +
                "  SELECT DISTINCT room_id FROM bookings " +
                "  WHERE status IN ('CONFIRMED','CHECKED_IN') " +
                "  AND check_in_date < ? AND check_out_date > ?" +
                ") ORDER BY price_per_night";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, type.name());
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAvailableByType failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return rooms;
    }

    /** For dashboard stats: count by each status */
    public Map<Room.RoomStatus, Integer> getStatusCounts() {
        Map<Room.RoomStatus, Integer> counts = new HashMap<>();
        for (Room.RoomStatus s : Room.RoomStatus.values()) counts.put(s, 0);
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT status, COUNT(*) as cnt FROM rooms GROUP BY status");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Room.RoomStatus s = Room.RoomStatus.fromString(rs.getString("status"));
                counts.put(s, rs.getInt("cnt"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getStatusCounts failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return counts;
    }

    public int countTotal() {
        return countByQuery("SELECT COUNT(*) FROM rooms");
    }

    public int countAvailable() {
        return countByQuery("SELECT COUNT(*) FROM rooms WHERE status = 'AVAILABLE'");
    }

    public int countOccupied() {
        return countByQuery("SELECT COUNT(*) FROM rooms WHERE status = 'OCCUPIED'");
    }

    private int countByQuery(String query) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "count query failed: " + query, e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    public List<Room> search(String keyword) {
        List<Room> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String pattern = "%" + keyword.toLowerCase() + "%";
            PreparedStatement ps = conn.prepareStatement(SELECT_BASE +
                "WHERE LOWER(room_number) LIKE ? OR LOWER(room_type) LIKE ? OR LOWER(amenities) LIKE ? " +
                "ORDER BY room_number");
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
            rs.close(); ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "room search failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return rooms;
    }

    // WRITE

    public int insert(Room room) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO rooms (room_number, room_type, floor_number, price_per_night, " +
                "max_occupancy, bed_type, amenities, description, status) VALUES (?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setInt(3, room.getFloorNumber());
            ps.setDouble(4, room.getPricePerNight());
            ps.setInt(5, room.getMaxOccupancy());
            ps.setString(6, room.getBedType() != null ? room.getBedType().name() : "DOUBLE");
            ps.setString(7, room.getAmenities());
            ps.setString(8, room.getDescription());
            ps.setString(9, room.getStatus().name());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            keys.close(); ps.close();
            return id;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert room failed", e);
            throw new RuntimeException("Failed to add room: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean update(Room room) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE rooms SET room_number=?, room_type=?, floor_number=?, price_per_night=?, " +
                "max_occupancy=?, bed_type=?, amenities=?, description=?, status=? WHERE room_id=?");
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setInt(3, room.getFloorNumber());
            ps.setDouble(4, room.getPricePerNight());
            ps.setInt(5, room.getMaxOccupancy());
            ps.setString(6, room.getBedType() != null ? room.getBedType().name() : "DOUBLE");
            ps.setString(7, room.getAmenities());
            ps.setString(8, room.getDescription());
            ps.setString(9, room.getStatus().name());
            ps.setInt(10, room.getRoomId());
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "update room failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean updateStatus(int roomId, Room.RoomStatus status) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE rooms SET status=? WHERE room_id=?");
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateStatus failed", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public boolean delete(int roomId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM rooms WHERE room_id=?");
            ps.setInt(1, roomId);
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "delete room failed — may have existing bookings", e);
            return false;
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // Row Mapper
    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(Room.RoomType.fromString(rs.getString("room_type")));
        r.setFloorNumber(rs.getInt("floor_number"));
        r.setPricePerNight(rs.getDouble("price_per_night"));
        r.setMaxOccupancy(rs.getInt("max_occupancy"));
        String bedType = rs.getString("bed_type");
        if (bedType != null) r.setBedType(Room.BedType.fromString(bedType));
        r.setAmenities(rs.getString("amenities"));
        r.setDescription(rs.getString("description"));
        r.setStatus(Room.RoomStatus.fromString(rs.getString("status")));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());
        return r;
    }
}
