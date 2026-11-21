-- =============================================================
-- HOTEL MANAGEMENT SYSTEM - DATABASE SCHEMA
-- File: docs/schema.sql
-- Run this ONCE before starting the application
-- =============================================================

CREATE DATABASE IF NOT EXISTS hotel_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hotel_db;

-- =============================================================
-- TABLE: users  (system staff accounts)
-- =============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT           PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)   UNIQUE NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    full_name     VARCHAR(100)  NOT NULL,
    email         VARCHAR(100)  UNIQUE NOT NULL,
    phone         VARCHAR(20),
    role          ENUM('ADMIN','RECEPTIONIST','STAFF') NOT NULL DEFAULT 'STAFF',
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    last_login    TIMESTAMP     NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role     (role)
) ENGINE=InnoDB;

-- =============================================================
-- TABLE: rooms
-- =============================================================
CREATE TABLE IF NOT EXISTS rooms (
    room_id          INT           PRIMARY KEY AUTO_INCREMENT,
    room_number      VARCHAR(10)   UNIQUE NOT NULL,
    room_type        ENUM('STANDARD','DELUXE','SUITE','PENTHOUSE') NOT NULL,
    floor_number     INT           NOT NULL,
    price_per_night  DECIMAL(10,2) NOT NULL,
    max_occupancy    INT           NOT NULL DEFAULT 2,
    bed_type         ENUM('SINGLE','DOUBLE','QUEEN','KING','TWIN') NOT NULL DEFAULT 'DOUBLE',
    amenities        TEXT,
    description      TEXT,
    status           ENUM('AVAILABLE','OCCUPIED','MAINTENANCE','RESERVED') NOT NULL DEFAULT 'AVAILABLE',
    image_path       VARCHAR(255),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status      (status),
    INDEX idx_room_type   (room_type),
    INDEX idx_floor       (floor_number)
) ENGINE=InnoDB;

-- =============================================================
-- TABLE: guests
-- =============================================================
CREATE TABLE IF NOT EXISTS guests (
    guest_id        INT          PRIMARY KEY AUTO_INCREMENT,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(100),
    phone           VARCHAR(20)  NOT NULL,
    id_proof_type   ENUM('AADHAR','PASSPORT','DRIVING_LICENSE','PAN','VOTER_ID') NOT NULL,
    id_proof_number VARCHAR(50)  NOT NULL,
    address         TEXT,
    city            VARCHAR(50),
    state           VARCHAR(50),
    country         VARCHAR(50)  NOT NULL DEFAULT 'India',
    nationality     VARCHAR(50)  NOT NULL DEFAULT 'Indian',
    date_of_birth   DATE,
    gender          ENUM('MALE','FEMALE','OTHER'),
    registered_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone      (phone),
    INDEX idx_email      (email),
    INDEX idx_id_proof   (id_proof_number)
) ENGINE=InnoDB;

-- =============================================================
-- TABLE: bookings
-- =============================================================
CREATE TABLE IF NOT EXISTS bookings (
    booking_id       INT           PRIMARY KEY AUTO_INCREMENT,
    room_id          INT           NOT NULL,
    guest_id         INT           NOT NULL,
    user_id          INT           NOT NULL,
    check_in_date    DATE          NOT NULL,
    check_out_date   DATE          NOT NULL,
    actual_checkin   TIMESTAMP     NULL,
    actual_checkout  TIMESTAMP     NULL,
    num_adults       INT           NOT NULL DEFAULT 1,
    num_children     INT           NOT NULL DEFAULT 0,
    special_requests TEXT,
    status           ENUM('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'CONFIRMED',
    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    advance_paid     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes            TEXT,
    booked_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id)  REFERENCES rooms(room_id)  ON UPDATE CASCADE,
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id) ON UPDATE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES users(user_id)   ON UPDATE CASCADE,
    INDEX idx_room_id  (room_id),
    INDEX idx_guest_id (guest_id),
    INDEX idx_status   (status),
    INDEX idx_dates    (check_in_date, check_out_date),
    CONSTRAINT chk_dates CHECK (check_out_date > check_in_date)
) ENGINE=InnoDB;

-- =============================================================
-- TABLE: invoices
-- =============================================================
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id      INT           PRIMARY KEY AUTO_INCREMENT,
    booking_id      INT           UNIQUE NOT NULL,
    invoice_number  VARCHAR(30)   UNIQUE NOT NULL,
    room_charges    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    service_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_percent     DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    net_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_status  ENUM('PENDING','PARTIAL','PAID','REFUNDED') NOT NULL DEFAULT 'PENDING',
    notes           TEXT,
    generated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON UPDATE CASCADE,
    INDEX idx_invoice_number (invoice_number),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB;

-- =============================================================
-- TABLE: payments
-- =============================================================
CREATE TABLE IF NOT EXISTS payments (
    payment_id      INT           PRIMARY KEY AUTO_INCREMENT,
    invoice_id      INT           NOT NULL,
    amount_paid     DECIMAL(10,2) NOT NULL,
    payment_method  ENUM('CASH','CREDIT_CARD','DEBIT_CARD','UPI','NET_BANKING','CHEQUE','WALLET') NOT NULL,
    transaction_ref VARCHAR(100),
    paid_by         VARCHAR(100),
    processed_by    INT,
    remarks         TEXT,
    paid_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id)   REFERENCES invoices(invoice_id) ON UPDATE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES users(user_id)       ON UPDATE CASCADE,
    INDEX idx_invoice_id    (invoice_id),
    INDEX idx_payment_date  (paid_at)
) ENGINE=InnoDB;

-- =============================================================
-- TABLE: service_orders  (room service, laundry, food, etc.)
-- =============================================================
CREATE TABLE IF NOT EXISTS service_orders (
    order_id     INT           PRIMARY KEY AUTO_INCREMENT,
    booking_id   INT           NOT NULL,
    service_name VARCHAR(100)  NOT NULL,
    category     ENUM('FOOD','LAUNDRY','SPA','TRANSPORT','MINIBAR','OTHER') NOT NULL DEFAULT 'OTHER',
    quantity     INT           NOT NULL DEFAULT 1,
    unit_price   DECIMAL(10,2) NOT NULL,
    total_charge DECIMAL(10,2) NOT NULL,
    status       ENUM('PENDING','IN_PROGRESS','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    ordered_by   INT,
    notes        TEXT,
    ordered_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON UPDATE CASCADE,
    FOREIGN KEY (ordered_by) REFERENCES users(user_id)       ON UPDATE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_status     (status)
) ENGINE=InnoDB;

-- =============================================================
-- TABLE: feedback
-- =============================================================
CREATE TABLE IF NOT EXISTS feedback (
    feedback_id   INT          PRIMARY KEY AUTO_INCREMENT,
    booking_id    INT          NOT NULL,
    guest_id      INT          NOT NULL,
    cleanliness   INT          NOT NULL DEFAULT 5 CHECK (cleanliness BETWEEN 1 AND 5),
    service       INT          NOT NULL DEFAULT 5 CHECK (service BETWEEN 1 AND 5),
    food_quality  INT          NOT NULL DEFAULT 5 CHECK (food_quality BETWEEN 1 AND 5),
    value_money   INT          NOT NULL DEFAULT 5 CHECK (value_money BETWEEN 1 AND 5),
    overall       INT          NOT NULL DEFAULT 5 CHECK (overall BETWEEN 1 AND 5),
    comments      TEXT,
    submitted_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON UPDATE CASCADE,
    FOREIGN KEY (guest_id)   REFERENCES guests(guest_id)     ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =============================================================
-- TABLE: activity_logs  (audit trail)
-- =============================================================
CREATE TABLE IF NOT EXISTS activity_logs (
    log_id      INT          PRIMARY KEY AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    action      VARCHAR(255) NOT NULL,
    module      VARCHAR(50)  NOT NULL,
    entity_id   INT,
    description TEXT,
    ip_address  VARCHAR(45),
    logged_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE,
    INDEX idx_user_id   (user_id),
    INDEX idx_module    (module),
    INDEX idx_logged_at (logged_at)
) ENGINE=InnoDB;

-- =============================================================
-- SAMPLE DATA
-- =============================================================

-- Admin user (password: Admin@123)
-- BCrypt hash for "Admin@123" with cost factor 12
INSERT INTO users (username, password_hash, full_name, email, phone, role) VALUES
('admin',       '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oUHcQMnxK', 'Super Administrator', 'admin@hotelms.com',       '+91-9876543210', 'ADMIN'),
('receptionist','$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oUHcQMnxK', 'Priya Sharma',        'priya@hotelms.com',        '+91-9876543211', 'RECEPTIONIST'),
('staff1',      '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4oUHcQMnxK', 'Rahul Verma',         'rahul@hotelms.com',        '+91-9876543212', 'STAFF');

-- NOTE: The BCrypt hash above is a placeholder.
-- The DatabaseInitializer.java will INSERT the real admin with a proper hash at runtime.
-- Delete the above INSERTs if you use DatabaseInitializer auto-setup.

-- Rooms (48 rooms across 4 floors)
INSERT INTO rooms (room_number, room_type, floor_number, price_per_night, max_occupancy, bed_type, amenities, description) VALUES
-- Floor 1: Standard rooms
('101', 'STANDARD', 1, 2500.00, 2, 'DOUBLE', 'AC, TV, WiFi, Hot Water, Daily Housekeeping',               'Comfortable standard room with garden view'),
('102', 'STANDARD', 1, 2500.00, 2, 'TWIN',   'AC, TV, WiFi, Hot Water, Daily Housekeeping',               'Twin-bed standard room, ideal for colleagues'),
('103', 'STANDARD', 1, 2500.00, 2, 'DOUBLE', 'AC, TV, WiFi, Hot Water, Daily Housekeeping',               'Cozy standard room with city view'),
('104', 'STANDARD', 1, 2500.00, 3, 'QUEEN',  'AC, TV, WiFi, Hot Water, Sofa Bed, Daily Housekeeping',     'Spacious standard room for small families'),
('105', 'STANDARD', 1, 2500.00, 2, 'DOUBLE', 'AC, TV, WiFi, Hot Water, Daily Housekeeping',               'Standard room near the parking area'),
-- Floor 2: Deluxe rooms
('201', 'DELUXE',   2, 4500.00, 2, 'KING',   'AC, Smart TV, WiFi, Mini Bar, Safe, Bathtub, City View',    'Premium deluxe with king bed and city skyline view'),
('202', 'DELUXE',   2, 4500.00, 2, 'KING',   'AC, Smart TV, WiFi, Mini Bar, Safe, Bathtub, Pool View',    'Deluxe room overlooking the swimming pool'),
('203', 'DELUXE',   2, 4800.00, 3, 'QUEEN',  'AC, Smart TV, WiFi, Mini Bar, Safe, Shower, Balcony',       'Deluxe with private balcony and garden view'),
('204', 'DELUXE',   2, 4500.00, 2, 'KING',   'AC, Smart TV, WiFi, Mini Bar, Safe, Bathtub',               'Classic deluxe room with premium bedding'),
('205', 'DELUXE',   2, 4800.00, 4, 'KING',   'AC, Smart TV, WiFi, Mini Bar, Safe, Bathtub, Extra Sofa',   'Family deluxe room with additional sofa bed'),
-- Floor 3: Suites
('301', 'SUITE',    3, 8500.00, 2, 'KING',   'AC, Smart TV, WiFi, Jacuzzi, Mini Bar, Lounge, Butler',     'Executive suite with separate lounge and city view'),
('302', 'SUITE',    3, 8500.00, 2, 'KING',   'AC, Smart TV, WiFi, Jacuzzi, Mini Bar, Lounge, Butler',     'Premium suite with panoramic city skyline'),
('303', 'SUITE',    3, 9000.00, 4, 'KING',   'AC, Smart TV, WiFi, Jacuzzi, Mini Bar, 2 Rooms, Butler',    'Honeymoon suite with romantic decor and jacuzzi'),
-- Floor 4: Penthouse
('401', 'PENTHOUSE',4, 15000.00, 4, 'KING',  'AC, Smart TV, Private Pool, Full Kitchen, Butler, Gym',     'Luxury penthouse with private pool and panoramic views');

-- Sample Guests
INSERT INTO guests (first_name, last_name, email, phone, id_proof_type, id_proof_number, address, city, state, nationality) VALUES
('Anjali',  'Shah',    'anjali.shah@email.com',   '+91-9811234567', 'AADHAR',          '1234-5678-9012', '45 MG Road',         'Mumbai',    'Maharashtra', 'Indian'),
('Rajesh',  'Kumar',   'rajesh.kumar@email.com',  '+91-9822345678', 'PASSPORT',        'P1234567',       '12 Park Street',     'Delhi',     'Delhi',       'Indian'),
('Priya',   'Mehta',   'priya.mehta@email.com',   '+91-9833456789', 'DRIVING_LICENSE', 'DL-23456789',    '78 Gandhi Nagar',    'Ahmedabad', 'Gujarat',     'Indian'),
('Vikram',  'Desai',   'vikram.desai@email.com',  '+91-9844567890', 'AADHAR',          '2345-6789-0123', '23 Church Street',   'Bangalore', 'Karnataka',   'Indian'),
('Sunita',  'Patel',   'sunita.patel@email.com',  '+91-9855678901', 'PAN',             'ABCDE1234F',     '56 Nehru Marg',      'Pune',      'Maharashtra', 'Indian'),
('Amit',    'Sharma',  'amit.sharma@email.com',   '+91-9866789012', 'VOTER_ID',        'MH/23/456789',   '89 Sarojini Nagar',  'Lucknow',   'UP',          'Indian'),
('Kavita',  'Gupta',   'kavita.gupta@email.com',  '+91-9877890123', 'PASSPORT',        'P7654321',       '34 Connaught Place', 'Delhi',     'Delhi',       'Indian'),
('Sanjay',  'Nair',    'sanjay.nair@email.com',   '+91-9888901234', 'AADHAR',          '3456-7890-1234', '67 Marine Drive',    'Kochi',     'Kerala',      'Indian');
