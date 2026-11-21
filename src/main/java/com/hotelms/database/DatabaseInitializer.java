package com.hotelms.database;

import com.hotelms.config.AppConfig;
import com.hotelms.utils.PasswordUtils;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    public static void initialize() {
        LOGGER.info("Running database initialization...");
        createTables();
        seedDefaultAdmin();
        createFileDirectories();
        LOGGER.info("Database initialization complete.");
    }

    // Table Creation (DDL)
    private static void createTables() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();

            // users
            stmt.executeUpdate("""
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
                    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // rooms
            stmt.executeUpdate("""
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
                    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // guests
            stmt.executeUpdate("""
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
                    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // bookings
            stmt.executeUpdate("""
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
                    status           ENUM('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW')
                                     NOT NULL DEFAULT 'CONFIRMED',
                    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    advance_paid     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    notes            TEXT,
                    booked_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (room_id)  REFERENCES rooms(room_id)  ON UPDATE CASCADE,
                    FOREIGN KEY (guest_id) REFERENCES guests(guest_id) ON UPDATE CASCADE,
                    FOREIGN KEY (user_id)  REFERENCES users(user_id)   ON UPDATE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // invoices
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS invoices (
                    invoice_id       INT           PRIMARY KEY AUTO_INCREMENT,
                    booking_id       INT           UNIQUE NOT NULL,
                    invoice_number   VARCHAR(30)   UNIQUE NOT NULL,
                    room_charges     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    service_charges  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    tax_percent      DECIMAL(5,2)  NOT NULL DEFAULT 18.00,
                    tax_amount       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    discount_percent DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
                    discount_amount  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    net_amount       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                    payment_status   ENUM('PENDING','PARTIAL','PAID','REFUNDED') NOT NULL DEFAULT 'PENDING',
                    notes            TEXT,
                    generated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON UPDATE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // payments
            stmt.executeUpdate("""
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
                    FOREIGN KEY (processed_by) REFERENCES users(user_id)       ON UPDATE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // service_orders
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS service_orders (
                    order_id      INT           PRIMARY KEY AUTO_INCREMENT,
                    booking_id    INT           NOT NULL,
                    service_name  VARCHAR(100)  NOT NULL,
                    category      ENUM('FOOD','LAUNDRY','SPA','TRANSPORT','MINIBAR','OTHER') NOT NULL DEFAULT 'OTHER',
                    quantity      INT           NOT NULL DEFAULT 1,
                    unit_price    DECIMAL(10,2) NOT NULL,
                    total_charge  DECIMAL(10,2) NOT NULL,
                    status        ENUM('PENDING','IN_PROGRESS','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
                    ordered_by    INT,
                    notes         TEXT,
                    ordered_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON UPDATE CASCADE,
                    FOREIGN KEY (ordered_by) REFERENCES users(user_id)       ON UPDATE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // feedback
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS feedback (
                    feedback_id  INT          PRIMARY KEY AUTO_INCREMENT,
                    booking_id   INT          NOT NULL,
                    guest_id     INT          NOT NULL,
                    cleanliness  INT          NOT NULL DEFAULT 5,
                    service      INT          NOT NULL DEFAULT 5,
                    food_quality INT          NOT NULL DEFAULT 5,
                    value_money  INT          NOT NULL DEFAULT 5,
                    overall      INT          NOT NULL DEFAULT 5,
                    comments     TEXT,
                    submitted_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON UPDATE CASCADE,
                    FOREIGN KEY (guest_id)   REFERENCES guests(guest_id)     ON UPDATE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // activity_logs
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS activity_logs (
                    log_id      INT          PRIMARY KEY AUTO_INCREMENT,
                    user_id     INT          NOT NULL,
                    action      VARCHAR(255) NOT NULL,
                    module      VARCHAR(50)  NOT NULL,
                    entity_id   INT,
                    description TEXT,
                    ip_address  VARCHAR(45),
                    logged_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            stmt.close();
            LOGGER.info("All tables verified/created successfully.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create database tables", e);
            throw new RuntimeException("Database table creation failed: " + e.getMessage(), e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // Seed Data
    private static void seedDefaultAdmin() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Check if any users exist
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM users");
            ResultSet rs = check.executeQuery();
            rs.next();
            int userCount = rs.getInt(1);
            rs.close();
            check.close();

            if (userCount == 0) {
                // Hash the default password with BCrypt
                String hashedPassword = PasswordUtils.hashPassword("Admin@123");

                PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, email, phone, role) VALUES (?,?,?,?,?,?)");
                insert.setString(1, "admin");
                insert.setString(2, hashedPassword);
                insert.setString(3, "Super Administrator");
                insert.setString(4, "admin@hotelms.com");
                insert.setString(5, "+91-9876543210");
                insert.setString(6, "ADMIN");
                insert.executeUpdate();
                insert.close();

                // Seed receptionist
                String hash2 = PasswordUtils.hashPassword("Admin@123");
                insert = conn.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, email, phone, role) VALUES (?,?,?,?,?,?)");
                insert.setString(1, "receptionist");
                insert.setString(2, hash2);
                insert.setString(3, "Priya Sharma");
                insert.setString(4, "priya@hotelms.com");
                insert.setString(5, "+91-9876543211");
                insert.setString(6, "RECEPTIONIST");
                insert.executeUpdate();
                insert.close();

                LOGGER.info("Default admin and receptionist accounts created. Username: admin, Password: Admin@123");
            }

            // Seed sample rooms if none exist
            PreparedStatement roomCheck = conn.prepareStatement("SELECT COUNT(*) FROM rooms");
            ResultSet rr = roomCheck.executeQuery();
            rr.next();
            int roomCount = rr.getInt(1);
            rr.close();
            roomCheck.close();

            if (roomCount == 0) {
                seedSampleRooms(conn);
                LOGGER.info("Sample rooms seeded.");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to seed default data", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    private static void seedSampleRooms(Connection conn) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, floor_number, price_per_night, max_occupancy, bed_type, amenities, description) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        Object[][] rooms = {
            {"101","STANDARD",1,2500.00,2,"DOUBLE","AC, TV, WiFi, Hot Water","Comfortable standard room with garden view"},
            {"102","STANDARD",1,2500.00,2,"TWIN",  "AC, TV, WiFi, Hot Water","Twin-bed standard room"},
            {"103","STANDARD",1,2500.00,2,"DOUBLE","AC, TV, WiFi, Hot Water","Cozy standard room with city view"},
            {"104","STANDARD",1,2800.00,3,"QUEEN", "AC, TV, WiFi, Hot Water, Sofa","Spacious standard room for families"},
            {"105","STANDARD",1,2500.00,2,"DOUBLE","AC, TV, WiFi, Hot Water","Standard room with parking view"},
            {"201","DELUXE",  2,4500.00,2,"KING",  "AC, Smart TV, WiFi, Mini Bar, Bathtub","Premium deluxe city view"},
            {"202","DELUXE",  2,4500.00,2,"KING",  "AC, Smart TV, WiFi, Mini Bar, Bathtub","Deluxe overlooking pool"},
            {"203","DELUXE",  2,4800.00,3,"QUEEN", "AC, Smart TV, WiFi, Mini Bar, Balcony","Deluxe with private balcony"},
            {"204","DELUXE",  2,4500.00,2,"KING",  "AC, Smart TV, WiFi, Mini Bar, Bathtub","Classic deluxe room"},
            {"205","DELUXE",  2,4800.00,4,"KING",  "AC, Smart TV, WiFi, Mini Bar, Sofa","Family deluxe room"},
            {"301","SUITE",   3,8500.00,2,"KING",  "AC, Smart TV, WiFi, Jacuzzi, Lounge, Butler","Executive suite city view"},
            {"302","SUITE",   3,8500.00,2,"KING",  "AC, Smart TV, WiFi, Jacuzzi, Lounge, Butler","Premium panoramic suite"},
            {"303","SUITE",   3,9000.00,2,"KING",  "AC, Smart TV, WiFi, Jacuzzi, 2 Rooms, Butler","Honeymoon suite"},
            {"401","PENTHOUSE",4,15000.00,4,"KING","AC, Smart TV, Private Pool, Full Kitchen, Butler","Luxury penthouse"}
        };

        for (Object[] room : rooms) {
            ps.setString(1, (String) room[0]);
            ps.setString(2, (String) room[1]);
            ps.setInt(3, (Integer) room[2]);
            ps.setDouble(4, (Double) room[3]);
            ps.setInt(5, (Integer) room[4]);
            ps.setString(6, (String) room[5]);
            ps.setString(7, (String) room[6]);
            ps.setString(8, (String) room[7]);
            ps.addBatch();
        }
        ps.executeBatch();
        ps.close();
    }

    // Filesystem Directories
    private static void createFileDirectories() {
        String[] dirs = {
            AppConfig.PDF_OUTPUT_DIR,
            AppConfig.LOG_DIR,
            AppConfig.BACKUP_DIR
        };
        for (String dir : dirs) {
            java.io.File f = new java.io.File(dir);
            if (!f.exists() && f.mkdirs()) {
                LOGGER.info("Created directory: " + dir);
            }
        }
    }
}
