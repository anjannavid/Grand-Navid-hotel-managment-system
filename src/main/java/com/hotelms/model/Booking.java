package com.hotelms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Booking {

    public enum BookingStatus {
        CONFIRMED("Confirmed"),
        CHECKED_IN("Checked In"),
        CHECKED_OUT("Checked Out"),
        CANCELLED("Cancelled"),
        NO_SHOW("No Show");

        private final String display;
        BookingStatus(String d) { this.display = d; }
        public String getDisplay() { return display; }

        public static BookingStatus fromString(String v) {
            return switch (v.toUpperCase()) {
                case "CONFIRMED"    -> CONFIRMED;
                case "CHECKED_IN"   -> CHECKED_IN;
                case "CHECKED_OUT"  -> CHECKED_OUT;
                case "CANCELLED"    -> CANCELLED;
                case "NO_SHOW"      -> NO_SHOW;
                default             -> CONFIRMED;
            };
        }
    }

    private int bookingId;
    private int roomId;
    private int guestId;
    private int userId;          // staff who created it
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime actualCheckin;
    private LocalDateTime actualCheckout;
    private int numAdults    = 1;
    private int numChildren  = 0;
    private String specialRequests;
    private BookingStatus status = BookingStatus.CONFIRMED;
    private double totalAmount;
    private double advancePaid;
    private String notes;
    private LocalDateTime bookedAt;

    // Denormalized convenience fields (populated by JOIN queries)
    private String guestName;
    private String guestPhone;
    private String roomNumber;
    private String roomType;

    public Booking() {}

    // Getters & Setters
    public int getBookingId()                         { return bookingId; }
    public void setBookingId(int id)                  { this.bookingId = id; }
    public int getRoomId()                            { return roomId; }
    public void setRoomId(int id)                     { this.roomId = id; }
    public int getGuestId()                           { return guestId; }
    public void setGuestId(int id)                    { this.guestId = id; }
    public int getUserId()                            { return userId; }
    public void setUserId(int id)                     { this.userId = id; }
    public LocalDate getCheckInDate()                 { return checkInDate; }
    public void setCheckInDate(LocalDate d)           { this.checkInDate = d; }
    public LocalDate getCheckOutDate()                { return checkOutDate; }
    public void setCheckOutDate(LocalDate d)          { this.checkOutDate = d; }
    public LocalDateTime getActualCheckin()           { return actualCheckin; }
    public void setActualCheckin(LocalDateTime t)     { this.actualCheckin = t; }
    public LocalDateTime getActualCheckout()          { return actualCheckout; }
    public void setActualCheckout(LocalDateTime t)    { this.actualCheckout = t; }
    public int getNumAdults()                         { return numAdults; }
    public void setNumAdults(int n)                   { this.numAdults = n; }
    public int getNumChildren()                       { return numChildren; }
    public void setNumChildren(int n)                 { this.numChildren = n; }
    public String getSpecialRequests()                { return specialRequests; }
    public void setSpecialRequests(String s)          { this.specialRequests = s; }
    public BookingStatus getStatus()                  { return status; }
    public void setStatus(BookingStatus s)            { this.status = s; }
    public double getTotalAmount()                    { return totalAmount; }
    public void setTotalAmount(double a)              { this.totalAmount = a; }
    public double getAdvancePaid()                    { return advancePaid; }
    public void setAdvancePaid(double a)              { this.advancePaid = a; }
    public String getNotes()                          { return notes; }
    public void setNotes(String n)                    { this.notes = n; }
    public LocalDateTime getBookedAt()                { return bookedAt; }
    public void setBookedAt(LocalDateTime t)          { this.bookedAt = t; }
    public String getGuestName()                      { return guestName; }
    public void setGuestName(String n)                { this.guestName = n; }
    public String getGuestPhone()                     { return guestPhone; }
    public void setGuestPhone(String p)               { this.guestPhone = p; }
    public String getRoomNumber()                     { return roomNumber; }
    public void setRoomNumber(String n)               { this.roomNumber = n; }
    public String getRoomType()                       { return roomType; }
    public void setRoomType(String t)                 { this.roomType = t; }

    /** Calculates number of nights booked */
    public long getNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public double getBalanceDue() {
        return Math.max(0, totalAmount - advancePaid);
    }

    @Override
    public String toString() {
        return "Booking{id=" + bookingId + ", room=" + roomNumber
             + ", guest=" + guestName + ", " + checkInDate + " → " + checkOutDate + "}";
    }
}
