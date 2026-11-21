package com.hotelms.model;

import java.time.LocalDateTime;

public class Room {

    public enum RoomType {
        STANDARD("Standard", "STD"),
        DELUXE("Deluxe", "DLX"),
        SUITE("Suite", "STE"),
        PENTHOUSE("Penthouse", "PH");

        private final String displayName;
        private final String code;

        RoomType(String displayName, String code) {
            this.displayName = displayName;
            this.code        = code;
        }

        public String getDisplayName() { return displayName; }
        public String getCode()        { return code; }

        public static RoomType fromString(String value) {
            return switch (value.toUpperCase()) {
                case "DELUXE"     -> DELUXE;
                case "SUITE"      -> SUITE;
                case "PENTHOUSE"  -> PENTHOUSE;
                default           -> STANDARD;
            };
        }
    }

    public enum RoomStatus {
        AVAILABLE("Available"),
        OCCUPIED("Occupied"),
        MAINTENANCE("Maintenance"),
        RESERVED("Reserved");

        private final String displayName;
        RoomStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }

        public static RoomStatus fromString(String value) {
            return switch (value.toUpperCase()) {
                case "OCCUPIED"    -> OCCUPIED;
                case "MAINTENANCE" -> MAINTENANCE;
                case "RESERVED"    -> RESERVED;
                default            -> AVAILABLE;
            };
        }
    }

    public enum BedType {
        SINGLE, DOUBLE, QUEEN, KING, TWIN;
        public static BedType fromString(String v) {
            try { return BedType.valueOf(v.toUpperCase()); } catch (Exception e) { return DOUBLE; }
        }
    }

    // Fields
    private int roomId;
    private String roomNumber;
    private RoomType roomType;
    private int floorNumber;
    private double pricePerNight;
    private int maxOccupancy;
    private BedType bedType;
    private String amenities;
    private String description;
    private RoomStatus status;
    private LocalDateTime createdAt;

    // Constructors
    public Room() {}

    public Room(String roomNumber, RoomType roomType, int floor, double price) {
        this.roomNumber  = roomNumber;
        this.roomType    = roomType;
        this.floorNumber = floor;
        this.pricePerNight = price;
        this.status      = RoomStatus.AVAILABLE;
    }

    // Getters & Setters
    public int getRoomId()                        { return roomId; }
    public void setRoomId(int roomId)             { this.roomId = roomId; }

    public String getRoomNumber()                 { return roomNumber; }
    public void setRoomNumber(String n)           { this.roomNumber = n; }

    public RoomType getRoomType()                 { return roomType; }
    public void setRoomType(RoomType t)           { this.roomType = t; }

    public int getFloorNumber()                   { return floorNumber; }
    public void setFloorNumber(int f)             { this.floorNumber = f; }

    public double getPricePerNight()              { return pricePerNight; }
    public void setPricePerNight(double p)        { this.pricePerNight = p; }

    public int getMaxOccupancy()                  { return maxOccupancy; }
    public void setMaxOccupancy(int m)            { this.maxOccupancy = m; }

    public BedType getBedType()                   { return bedType; }
    public void setBedType(BedType b)             { this.bedType = b; }

    public String getAmenities()                  { return amenities; }
    public void setAmenities(String a)            { this.amenities = a; }

    public String getDescription()                { return description; }
    public void setDescription(String d)          { this.description = d; }

    public RoomStatus getStatus()                 { return status; }
    public void setStatus(RoomStatus s)           { this.status = s; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime c)     { this.createdAt = c; }

    // Convenience
    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }

    
    public String getDisplayLabel() {
        return roomNumber + " — " + roomType.getDisplayName()
             + (bedType != null ? " (" + bedType.name() + ")" : "");
    }

    @Override
    public String toString() {
        return "Room{" + roomNumber + ", " + roomType + ", " + status + ", ৳" + pricePerNight + "/night}";
    }
}
