package com.hotelms.database;

import com.hotelms.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Thread-safe blocking queue acts as our connection pool
    private final BlockingQueue<Connection> connectionPool;

    // Volatile ensures the single instance is visible across threads (double-checked locking)
    private static volatile DatabaseConnection instance;

    // Constructor — private, creates the pool on first use
    private DatabaseConnection() {
        connectionPool = new ArrayBlockingQueue<>(AppConfig.DB_POOL_MAX);
        initializePool();
    }

    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    // Pool Initialization
    private void initializePool() {
        try {
            // Load the MySQL JDBC driver explicitly (required for some environments)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Pre-warm the pool with minimum connections
            for (int i = 0; i < AppConfig.DB_POOL_MIN; i++) {
                connectionPool.offer(createNewConnection());
            }
            LOGGER.info("Database connection pool initialized with " + AppConfig.DB_POOL_MIN + " connections.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found! Add mysql-connector-java to classpath.", e);
            throw new RuntimeException("MySQL driver not found", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database: " + AppConfig.DB_URL, e);
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    /** Creates a brand-new JDBC connection with optimized properties */
    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD);
    }

    // Public API

    /**
     * Borrows a connection from the pool.
     * If pool is empty, creates a new one (up to DB_POOL_MAX).
     * If max is reached, blocks until one is released.
     *
     * @throws RuntimeException if a valid connection cannot be obtained
     */
    public Connection getConnection() {
        try {
            // Try to get from pool first
            Connection conn = connectionPool.poll();

            if (conn == null || conn.isClosed() || !conn.isValid(AppConfig.DB_TIMEOUT_SEC)) {
                // Pool was empty or connection was stale — create a fresh one
                conn = createNewConnection();
                LOGGER.fine("Created new database connection (pool was empty or stale).");
            }

            // Ensure auto-commit is on for standard operations
            conn.setAutoCommit(true);
            return conn;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to obtain database connection", e);
            throw new RuntimeException("Cannot connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a connection back to the pool for reuse.
     * Always call this in a finally{} block.
     *
     * @param connection the connection to return
     */
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    // Reset any stale transaction state before returning to pool
                    if (!connection.getAutoCommit()) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                    }
                    // Return to pool; if pool is full, close the connection
                    if (!connectionPool.offer(connection)) {
                        connection.close();
                        LOGGER.fine("Pool full — closed excess connection.");
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error releasing connection back to pool", e);
            }
        }
    }

    /**
     * Test whether the database is reachable.
     * Called at startup to give a friendly error instead of a cryptic NPE.
     */
    public boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Database connectivity test failed", e);
            return false;
        } finally {
            releaseConnection(conn);
        }
    }

    
    public void closeAllConnections() {
        connectionPool.forEach(conn -> {
            try {
                if (!conn.isClosed()) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing pooled connection", e);
            }
        });
        connectionPool.clear();
        LOGGER.info("All database connections closed.");
    }

    /** @return current number of connections sitting idle in the pool */
    public int getPoolSize() {
        return connectionPool.size();
    }
}
