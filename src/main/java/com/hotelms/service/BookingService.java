package com.hotelms.service;

import com.hotelms.dao.BookingDAO;
import com.hotelms.dao.InvoiceDAO;
import com.hotelms.dao.RoomDAO;
import com.hotelms.model.Booking;
import com.hotelms.model.Invoice;
import com.hotelms.model.Room;
import com.hotelms.utils.ActivityLogger;
import com.hotelms.utils.FormatUtils;
import com.hotelms.utils.SessionManager;
import com.hotelms.utils.ValidationUtils;
import com.hotelms.config.AppConfig;

import java.time.LocalDate;
import java.util.List;

public class BookingService {

    private final BookingDAO bookingDAO;
    private final RoomDAO roomDAO;
    private final InvoiceDAO invoiceDAO;
    private final BillingService billingService;

    public BookingService() {
        this.bookingDAO     = new BookingDAO();
        this.roomDAO        = new RoomDAO();
        this.invoiceDAO     = new InvoiceDAO();
        this.billingService = new BillingService();
    }

    /** Returns all bookings */
    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingDAO.findByStatus(status);
    }

    public List<Booking> getRecentBookings(int limit) {
        return bookingDAO.findRecentBookings(limit);
    }

    public List<Booking> searchBookings(String keyword) {
        return bookingDAO.search(keyword);
    }

    public List<Booking> getTodayCheckIns()  { return bookingDAO.findTodayCheckIns(); }
    public List<Booking> getTodayCheckOuts() { return bookingDAO.findTodayCheckOuts(); }

    public int countByStatus(Booking.BookingStatus status) { return bookingDAO.countByStatus(status); }
    public int countTotal()   { return bookingDAO.countTotal(); }
    public double getTodayRevenue() { return bookingDAO.getTodayRevenue(); }
    public List<double[]> getMonthlyRevenue(int year) { return bookingDAO.getMonthlyRevenue(year); }

    /**
     * Creates a new booking with full validation.
     * @return null on success, error message on failure
     */
    public String createBooking(int roomId, int guestId, LocalDate checkIn,
                                LocalDate checkOut, int numAdults, int numChildren,
                                String specialRequests, double advancePaid) {
        // 1. Date validation
        String dateErr = ValidationUtils.validateDateRange(checkIn, checkOut);
        if (dateErr != null) return dateErr;

        // 2. Room exists
        var roomOpt = roomDAO.findById(roomId);
        if (roomOpt.isEmpty()) return "Selected room not found";
        Room room = roomOpt.get();

        // 3. Room not in maintenance
        if (room.getStatus() == Room.RoomStatus.MAINTENANCE) {
            return "Room " + room.getRoomNumber() + " is under maintenance and cannot be booked";
        }

        // 4. Conflict detection — THE CRITICAL CHECK
        if (bookingDAO.hasConflict(roomId, checkIn, checkOut, -1)) {
            return "Room " + room.getRoomNumber() + " is not available for the selected dates. " +
                   "Please choose different dates or another room.";
        }

        // 5. Occupancy check
        if (numAdults <= 0) return "At least 1 adult is required";
        if ((numAdults + numChildren) > room.getMaxOccupancy()) {
            return "Room " + room.getRoomNumber() + " has a maximum occupancy of " +
                   room.getMaxOccupancy() + " persons";
        }

        // 6. Calculate total
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalAmount = nights * room.getPricePerNight();

        // 7. Advance paid cannot exceed total
        if (advancePaid > totalAmount) return "Advance payment cannot exceed total amount";

        // 8. Build and save
        Booking booking = new Booking();
        booking.setRoomId(roomId);
        booking.setGuestId(guestId);
        booking.setUserId(SessionManager.getInstance().getUserId());
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumAdults(numAdults);
        booking.setNumChildren(numChildren);
        booking.setSpecialRequests(specialRequests);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setTotalAmount(totalAmount);
        booking.setAdvancePaid(advancePaid);

        int bookingId = bookingDAO.insert(booking);
        if (bookingId <= 0) return "Failed to create booking. Please try again.";

        // 9. Mark room as reserved
        roomDAO.updateStatus(roomId, Room.RoomStatus.RESERVED);

        // 10. Auto-generate invoice
        billingService.generateInvoice(bookingId, room.getPricePerNight(), nights);

        ActivityLogger.log(SessionManager.getInstance().getUserId(),
            "Created booking #" + bookingId + " for room " + room.getRoomNumber(),
            ActivityLogger.MODULE_BOOKING, bookingId, null);

        return null;
    }

    /**
     * Performs guest check-in.
     * Transitions: CONFIRMED → CHECKED_IN, Room: RESERVED → OCCUPIED
     */
    public String performCheckIn(int bookingId) {
        var bookingOpt = bookingDAO.findById(bookingId);
        if (bookingOpt.isEmpty()) return "Booking #" + bookingId + " not found";

        Booking booking = bookingOpt.get();

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            return "Booking is in " + booking.getStatus().getDisplay() + " status and cannot be checked in";
        }

        // Allow check-in from today onwards (and one day before for early check-in)
        LocalDate today = LocalDate.now();
        if (booking.getCheckInDate().isAfter(today.plusDays(1))) {
            return "Check-in date is " + FormatUtils.formatDate(booking.getCheckInDate()) +
                   ". Early check-in is only allowed 1 day before.";
        }

        boolean ok = bookingDAO.checkIn(bookingId);
        if (!ok) return "Failed to perform check-in. Please try again.";

        roomDAO.updateStatus(booking.getRoomId(), Room.RoomStatus.OCCUPIED);

        ActivityLogger.log(SessionManager.getInstance().getUserId(),
            "Check-in for booking #" + bookingId + " room " + booking.getRoomNumber(),
            ActivityLogger.MODULE_BOOKING, bookingId, null);

        return null;
    }

    /**
     * Performs guest check-out.
     * Transitions: CHECKED_IN → CHECKED_OUT, Room: OCCUPIED → AVAILABLE
     */
    public String performCheckOut(int bookingId) {
        var bookingOpt = bookingDAO.findById(bookingId);
        if (bookingOpt.isEmpty()) return "Booking #" + bookingId + " not found";

        Booking booking = bookingOpt.get();

        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            return "Booking is in " + booking.getStatus().getDisplay() + " status and cannot be checked out";
        }

        // Ensure invoice is settled
        var invoiceOpt = invoiceDAO.findByBookingId(bookingId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            if (invoice.getPaymentStatus() == Invoice.PaymentStatus.PENDING) {
                return "Outstanding invoice (" + invoice.getInvoiceNumber() + ") must be settled before check-out. " +
                       "Amount due: " + FormatUtils.formatCurrency(invoice.getNetAmount());
            }
        }

        boolean ok = bookingDAO.checkOut(bookingId);
        if (!ok) return "Failed to perform check-out. Please try again.";

        roomDAO.updateStatus(booking.getRoomId(), Room.RoomStatus.AVAILABLE);

        ActivityLogger.log(SessionManager.getInstance().getUserId(),
            "Check-out for booking #" + bookingId + " room " + booking.getRoomNumber(),
            ActivityLogger.MODULE_BOOKING, bookingId, null);

        return null;
    }

    /**
     * Cancels a booking. Only CONFIRMED bookings can be cancelled.
     */
    public String cancelBooking(int bookingId, String reason) {
        var bookingOpt = bookingDAO.findById(bookingId);
        if (bookingOpt.isEmpty()) return "Booking not found";

        Booking booking = bookingOpt.get();

        if (booking.getStatus() == Booking.BookingStatus.CHECKED_IN) {
            return "Cannot cancel a booking that has already been checked in. Perform check-out instead.";
        }
        if (booking.getStatus() == Booking.BookingStatus.CHECKED_OUT) {
            return "Booking is already checked out";
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            return "Booking is already cancelled";
        }

        boolean ok = bookingDAO.updateStatus(bookingId, Booking.BookingStatus.CANCELLED);
        if (!ok) return "Failed to cancel booking";

        roomDAO.updateStatus(booking.getRoomId(), Room.RoomStatus.AVAILABLE);

        ActivityLogger.log(SessionManager.getInstance().getUserId(),
            "Cancelled booking #" + bookingId + ": " + reason,
            ActivityLogger.MODULE_BOOKING, bookingId, reason);

        return null;
    }
}
