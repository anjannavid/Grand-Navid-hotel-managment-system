package com.hotelms.service;

import com.hotelms.dao.GuestDAO;
import com.hotelms.model.Guest;
import com.hotelms.utils.ActivityLogger;
import com.hotelms.utils.SessionManager;
import com.hotelms.utils.ValidationUtils;

import java.util.List;
import java.util.Optional;

public class GuestService {

    private final GuestDAO guestDAO;

    public GuestService() {
        this.guestDAO = new GuestDAO();
    }

    public List<Guest> getAllGuests()            { return guestDAO.findAll(); }
    public Optional<Guest> getGuestById(int id)  { return guestDAO.findById(id); }
    public List<Guest> searchGuests(String keyword) { return guestDAO.search(keyword); }
    public int countAll()                         { return guestDAO.countAll(); }

    public String addGuest(Guest guest) {
        String err = validateGuest(guest);
        if (err != null) return err;

        int id = guestDAO.insert(guest);
        if (id > 0) {
            ActivityLogger.log(SessionManager.getInstance().getUserId(),
                "Registered guest: " + guest.getFullName(), ActivityLogger.MODULE_GUEST, id, null);
            return null;
        }
        return "Failed to register guest. Please try again.";
    }

    public String updateGuest(Guest guest) {
        String err = validateGuest(guest);
        if (err != null) return err;

        boolean ok = guestDAO.update(guest);
        if (ok) {
            ActivityLogger.log(SessionManager.getInstance().getUserId(),
                "Updated guest: " + guest.getFullName(), ActivityLogger.MODULE_GUEST, guest.getGuestId(), null);
            return null;
        }
        return "Failed to update guest";
    }

    private String validateGuest(Guest g) {
        if (g.getFirstName() == null || g.getFirstName().isBlank()) return "First name is required";
        if (g.getLastName()  == null || g.getLastName().isBlank())  return "Last name is required";

        String phoneErr = ValidationUtils.validatePhone(g.getPhone());
        if (phoneErr != null) return phoneErr;

        String emailErr = ValidationUtils.validateEmail(g.getEmail());
        if (emailErr != null) return emailErr;

        if (g.getIdProofType() == null)   return "ID proof type is required";
        if (g.getIdProofNumber() == null || g.getIdProofNumber().isBlank()) return "ID proof number is required";

        return null;
    }
}
