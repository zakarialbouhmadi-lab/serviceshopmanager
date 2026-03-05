package com.zakilb.servicemanager.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Database Connection Manager (Singleton)
 * Manages SQLite database connection and initialization
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private static final String DB_NAME = "service_shop_db.db";
    private static final String APP_NAME = "ServiceShopManager";
    private Connection connection;
    private final String dbPath;

    private DatabaseManager() {
        // Load SQLite JDBC driver explicitly
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }

        // Get proper data directory for the OS
        Path dataDir = getAppDataDirectory();
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            this.dbPath = dataDir.resolve(DB_NAME).toString();
            System.out.println("Database location: " + dbPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create data directory: " + dataDir, e);
        }
    }

    /**
     * Get the appropriate application data directory for the current OS
     * Windows: %LOCALAPPDATA%\
     * macOS: ~/Library/Application Support/
     * Linux: ~/.local/share/
     */
    public static Path getAppDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        Path baseDir;

        if (os.contains("win")) {
            // Windows: use LOCALAPPDATA
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null) {
                baseDir = Paths.get(localAppData, APP_NAME);
            } else {
                // Fallback to user home
                baseDir = Paths.get(System.getProperty("user.home"), "AppData", "Local", APP_NAME);
            }
        } else if (os.contains("mac")) {
            // macOS: use Application Support
            baseDir = Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_NAME);
        } else {
            // Linux/Unix: use XDG_DATA_HOME or default
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            if (xdgDataHome != null) {
                baseDir = Paths.get(xdgDataHome, APP_NAME);
            } else {
                baseDir = Paths.get(System.getProperty("user.home"), ".local", "share", APP_NAME);
            }
        }

        return baseDir;
    }
    
    /**
     * Get singleton instance of DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Get database connection
     * Creates connection if not exists and initializes database
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
            initializeDatabase();
        }
        return connection;
    }
    
    /**
     * Establish connection to SQLite database
     */
    private void connect() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        connection = DriverManager.getConnection(url);
        
        // Enable foreign keys
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        
        System.out.println("Connected to SQLite database: " + dbPath);
    }
    
    /**
     * Initialize database schema if tables don't exist
     */
    private void initializeDatabase() {
        try {
            // Check if tables exist
            if (!tablesExist()) {
                executeSqlScript();
                System.out.println("Database schema initialized successfully");
            }

            // Run migrations for existing databases
            runMigrations();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Run database migrations for schema updates
     */
    private void runMigrations() {
        try {
            // Migration: Add amount_paid column to work_order if it doesn't exist
            if (!columnExists("work_order", "amount_paid")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE work_order ADD COLUMN amount_paid INTEGER NOT NULL DEFAULT 0");
                    System.out.println("Migration: Added amount_paid column to work_order");
                }
            }
            
            // Migration: Add discount_percent column if it doesn't exist
            if (!columnExists("work_order_item", "discount_percent")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE work_order_item ADD COLUMN discount_percent REAL NOT NULL DEFAULT 0.0");
                    System.out.println("Migration: Added discount_percent column to work_order_item");
                }
            }
            
            // Migration: Add notes column to order_status_history if it doesn't exist
            if (tableExists("order_status_history") && !columnExists("order_status_history", "notes")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE order_status_history ADD COLUMN notes TEXT");
                    System.out.println("Migration: Added notes column to order_status_history");
                }
            }
            
            // Migration: Create order_status_history table if it doesn't exist
            if (!tableExists("order_status_history")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS order_status_history (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            work_order_id INTEGER NOT NULL,
                            old_status TEXT NOT NULL,
                            new_status TEXT NOT NULL,
                            notes TEXT,
                            changed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (work_order_id) REFERENCES work_order(id) ON DELETE CASCADE
                        )
                    """);
                    stmt.execute("CREATE INDEX IF NOT EXISTS idx_status_history_order ON order_status_history(work_order_id)");
                    System.out.println("Migration: Created order_status_history table");
                }
            }
            
            // Migration: Add item_name column to work_order_item if it doesn't exist
            if (!columnExists("work_order_item", "item_name")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE work_order_item ADD COLUMN item_name TEXT");
                    System.out.println("Migration: Added item_name column to work_order_item");
                }
            }
            
            // Migration: Add bundle_count column to work_order_item if it doesn't exist
            if (!columnExists("work_order_item", "bundle_count")) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE work_order_item ADD COLUMN bundle_count INTEGER NOT NULL DEFAULT 1");
                    System.out.println("Migration: Added bundle_count column to work_order_item");
                }
            }
        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
        }
    }
    
    /**
     * Check if a table exists in the database
     */
    private boolean tableExists(String tableName) throws SQLException {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (var stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tableName);
            try (var rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Check if a column exists in a table
     */
    private boolean columnExists(String tableName, String columnName) throws SQLException {
        String query = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                if (columnName.equals(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if database tables exist
     */
    private boolean tablesExist() throws SQLException {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='customer'";
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(query)) {
            return rs.next();
        }
    }
    
    /**
     * Execute SQL schema script from resources
     */
    private void executeSqlScript() throws Exception {
        InputStream inputStream = getClass()
                .getResourceAsStream("/db/schema.sql");
        
        if (inputStream == null) {
            throw new RuntimeException("Schema file not found in resources");
        }
        
        String sql = new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining("\n"));
        
        // Execute the entire script at once
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    /**
     * Force close all connections and release file locks
     * Used before restore operations
     */
    public void forceCloseForRestore() {
        try {
            if (connection != null && !connection.isClosed()) {
                // Disable auto-commit and rollback any pending transactions
                try {
                    connection.rollback();
                } catch (SQLException ignored) {}

                connection.close();
                connection = null;
            }

            // Give SQLite time to release file locks
            Thread.sleep(500);

            // Force garbage collection to release any lingering references
            System.gc();
            Thread.sleep(200);

            System.out.println("Database connection force closed for restore");
        } catch (SQLException | InterruptedException e) {
            System.err.println("Error force closing database: " + e.getMessage());
        }
    }
    
    /**
     * Get database file path
     */
    public String getDbPath() {
        return dbPath;
    }
}
