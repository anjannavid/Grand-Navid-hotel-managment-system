package com.hotelms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Guest {

    public enum IdProofType {
        AADHAR, PASSPORT, DRIVING_LICENSE, PAN, VOTER_ID;
        public static IdProofType fromString(String v) {
            try { return valueOf(v.toUpperCase()); } catch (Exception e) { return AADHAR; }
        }
    }

    public enum Gender { MALE, FEMALE, OTHER }

    private int guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private IdProofType idProofType;
    private String idProofNumber;
    private String address;
    private String city;
    private String state;
    private String country  = "Bangladesh";
    private String nationality = "Bangladeshi";
    private LocalDate dateOfBirth;
    private Gender gender;
    private LocalDateTime registeredAt;

    public Guest() {}

    public Guest(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.phone     = phone;
    }

    // Getters & Setters
    public int getGuestId()                       { return guestId; }
    public void setGuestId(int id)                { this.guestId = id; }
    public String getFirstName()                  { return firstName; }
    public void setFirstName(String n)            { this.firstName = n; }
    public String getLastName()                   { return lastName; }
    public void setLastName(String n)             { this.lastName = n; }
    public String getFullName()                   { return firstName + " " + lastName; }
    public String getEmail()                      { return email; }
    public void setEmail(String e)                { this.email = e; }
    public String getPhone()                      { return phone; }
    public void setPhone(String p)                { this.phone = p; }
    public IdProofType getIdProofType()           { return idProofType; }
    public void setIdProofType(IdProofType t)     { this.idProofType = t; }
    public String getIdProofNumber()              { return idProofNumber; }
    public void setIdProofNumber(String n)        { this.idProofNumber = n; }
    public String getAddress()                    { return address; }
    public void setAddress(String a)              { this.address = a; }
    public String getCity()                       { return city; }
    public void setCity(String c)                 { this.city = c; }
    public String getState()                      { return state; }
    public void setState(String s)                { this.state = s; }
    public String getCountry()                    { return country; }
    public void setCountry(String c)              { this.country = c; }
    public String getNationality()                { return nationality; }
    public void setNationality(String n)          { this.nationality = n; }
    public LocalDate getDateOfBirth()             { return dateOfBirth; }
    public void setDateOfBirth(LocalDate d)       { this.dateOfBirth = d; }
    public Gender getGender()                     { return gender; }
    public void setGender(Gender g)               { this.gender = g; }
    public LocalDateTime getRegisteredAt()        { return registeredAt; }
    public void setRegisteredAt(LocalDateTime t)  { this.registeredAt = t; }

    public String getInitials() {
        return (firstName != null ? String.valueOf(firstName.charAt(0)) : "")
             + (lastName  != null ? String.valueOf(lastName.charAt(0))  : "");
    }

    @Override public String toString() { return getFullName() + " (" + phone + ")"; }
}
