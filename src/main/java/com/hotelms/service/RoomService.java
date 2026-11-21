package com.hotelms.service;

import com.hotelms.dao.RoomDAO;
import com.hotelms.model.Room;
import com.hotelms.utils.ActivityLogger;
import com.hotelms.utils.SessionManager;
import com.hotelms.utils.ValidationUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoomService {

    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
    }

    public List<Room> getAllRooms()           { return roomDAO.findAll(); }
    public Optional<Room> getRoomById(int id) { return roomDAO.findById(id); }
    public List<Room> getRoomsByStatus(Room.RoomStatus s) { return roomDAO.findByStatus(s); }
    public Map<Room.RoomStatus, Integer> getStatusCounts() { return roomDAO.getStatusCounts(); }
    public int countTotal()     { return roomDAO.countTotal(); }
    public int countAvailable() { return roomDAO.countAvailable(); }
    public int countOccupied()  { return roomDAO.countOccupied(); }
    public List<Room> search(String keyword) { return roomDAO.search(keyword); }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomDAO.findAvailableRooms(checkIn, checkOut);
    }

    public String addRoom(Room room) {
        if (!SessionManager.getInstance().isAdmin()) return "Only admins can add rooms";
        String err = validateRoom(room);
        if (err != null) return err;

        if (roomDAO.findByNumber(room.getRoomNumber()).isPresent())
            return "Room number " + room.getRoomNumber() + " already exists";

        int id = roomDAO.insert(room);
        if (id > 0) {
            ActivityLogger.log(SessionManager.getInstance().getUserId(),
                "Added room " + room.getRoomNumber(), ActivityLogger.MODULE_ROOM, id, null);
            return null;
        }
        return "Failed to add room";
    }

    public String updateRoom(Room room) {
        if (!SessionManager.getInstance().isAdmin()) return "Only admins can update rooms";
        String err = validateRoom(room);
        if (err != null) return err;

        boolean ok = roomDAO.update(room);
        if (ok) {
            ActivityLogger.log(SessionManager.getInstance().getUserId(),
                "Updated room " + room.getRoomNumber(), ActivityLogger.MODULE_ROOM, room.getRoomId(), null);
            return null;
        }
        return "Failed to update room";
    }

    public String updateRoomStatus(int roomId, Room.RoomStatus status) {
        if (!SessionManager.getInstance().isReceptionist()) return "Insufficient permissions";
        boolean ok = roomDAO.updateStatus(roomId, status);
        return ok ? null : "Failed to update room status";
    }

    public String deleteRoom(int roomId) {
        if (!SessionManager.getInstance().isAdmin()) return "Only admins can delete rooms";
        boolean ok = roomDAO.delete(roomId);
        if (ok) {
            ActivityLogger.log(SessionManager.getInstance().getUserId(),
                "Deleted room ID " + roomId, ActivityLogger.MODULE_ROOM, roomId, null);
            return null;
        }
        return "Cannot delete room — it may have existing bookings";
    }

    private String validateRoom(Room room) {
        if (room.getRoomNumber() == null || room.getRoomNumber().isBlank()) return "Room number is required";
        if (room.getRoomType() == null) return "Room type is required";
        if (room.getPricePerNight() <= 0) return "Price must be greater than zero";
        if (room.getFloorNumber() <= 0)   return "Floor number must be a positive integer";
        if (room.getMaxOccupancy() <= 0)  return "Max occupancy must be a positive integer";
        return null;
    }
}
