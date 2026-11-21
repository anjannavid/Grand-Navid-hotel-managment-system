<div align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Swing-FlatLaf-4F7BE8?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
</div>

<br />

<div align="center">
  <h1 align="center">HotelOS - Management System</h1>
  <p align="center">
    A premium, enterprise-grade desktop application designed to streamline hotel operations, built entirely in Java.
  </p>
</div>

---

## 📖 Overview

HotelOS is a comprehensive, production-ready desktop software built to handle modern hotel management workflows. Designed with a gorgeous, dark-themed SaaS aesthetic, it provides robust features for receptionists and administrators alike—ranging from room inventory and guest check-ins to advanced analytics and automated PDF billing.

*Note: This repository contains the source code. Passwords and sensitive environment variables have been removed for security.*

## ✨ Key Features

- **📊 Real-time Dashboard:** Track total revenue, occupancy rates, and immediate check-ins/check-outs at a glance.
- **🛏️ Room & Booking Engine:** Visual occupancy bars and intelligent double-booking prevention.
- **💳 Billing & PDF Generation:** Seamless checkout flows generating professional `iText` PDF invoices automatically.
- **📈 Analytics Module:** Data visualization utilizing `JFreeChart` to display revenue trends and room utilization.
- **🛡️ Role-Based Security:** Secure authentication (BCrypt) distinguishing between Admin and Receptionist permissions.
- **📋 Audit Logging:** Complete activity trail tracking user actions.

## 💻 Tech Stack

- **Language:** Java 21+
- **GUI Framework:** Java Swing
- **Theming:** [FlatLaf](https://www.formdev.com/flatlaf/) (Flat Mac Dark Theme)
- **Database:** MySQL 8.0 (JDBC)
- **Security:** jBCrypt
- **Build Tool:** Maven

## 🚀 Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/prasadkundekar/hotel-management-system.git
   cd hotel-management-system
   ```

2. **Configure Database**
   - Install MySQL and start the server.
   - Open `src/main/java/com/hotelms/config/AppConfig.java`.
   - Update `DB_USER` and `DB_PASSWORD` with your local MySQL credentials.
   - *The application will automatically create the schema (`hotel_db`) and necessary tables upon first launch!*

3. **Build the Application**
   ```bash
   mvn clean compile
   ```

4. **Run the Application**
   ```bash
   mvn exec:java -Dexec.mainClass="com.hotelms.Main"
   ```

## 📸 Screenshots

*(Add screenshots of your application here. You can capture screenshots while running the app and place them in an `/assets` folder, then link them below.)*

> **Tip:** Go to the app, take screenshots of the Dashboard, Booking Panel, and Analytics Panel, and upload them to GitHub to make this README shine!

## 🤝 Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
