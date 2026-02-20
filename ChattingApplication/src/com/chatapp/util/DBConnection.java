
package com.chatapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection — Manages SQLite JDBC connections.
 *
 * - Loads the SQLite driver once on startup
 * - Creates the database file automatically if it doesn't exist
 * - Auto-creates all required tables (schema-on-startup)
 * - Provides a getConnection() method for all DAO classes
 */
public class DBConnection {

    private static boolean initialized = false;

    // ─── Static Initializer ───────────────────────────────────────────────────
    static {
        initializeDatabase();
    }

    // Prevent instantiation
    private DBConnection() {}

    // ─── Get Connection ───────────────────────────────────────────────────────
    /**
     * Returns a new JDBC Connection to the SQLite database.
     * Always call this inside a try-with-resources block in DAOs.
     *
     * Usage:
     *   try (Connection conn = DBConnection.getConnection()) { ... }
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initializeDatabase();
        }
        return DriverManager.getConnection(Constants.DB_URL);
    }

    // ─── Initialize Database ──────────────────────────────────────────────────
    /**
     * Loads the JDBC driver and creates all tables if they don't exist.
     * Called once automatically via static block.
     */
    private static void initializeDatabase() {
        try {
            // Load SQLite JDBC driver
            Class.forName(Constants.DB_DRIVER);
            System.out.println(Constants.LOG_PREFIX_DB + " SQLite driver loaded.");

            // Create tables
            try (Connection conn = DriverManager.getConnection(Constants.DB_URL);
                 Statement stmt  = conn.createStatement()) {

                // Enable WAL mode for better concurrent read performance
                stmt.execute("PRAGMA journal_mode=WAL");
                // Enforce foreign keys
                stmt.execute("PRAGMA foreign_keys=ON");

                createUsersTable(stmt);
                createMessagesTable(stmt);

                initialized = true;
                System.out.println(Constants.LOG_PREFIX_DB + " Database initialized: " + Constants.DB_FILE_NAME);
            }

        } catch (ClassNotFoundException e) {
            System.err.println(Constants.LOG_PREFIX_DB + " SQLite JDBC driver not found!");
            System.err.println("  → Add sqlite-jdbc.jar to your /lib folder.");
            throw new RuntimeException("SQLite driver missing.", e);

        } catch (SQLException e) {
            System.err.println(Constants.LOG_PREFIX_DB + " Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Database setup failed.", e);
        }
    }

    // ─── Create Users Table ───────────────────────────────────────────────────
    private static void createUsersTable(Statement stmt) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    username      TEXT    NOT NULL UNIQUE COLLATE NOCASE,
                    password_hash TEXT    NOT NULL,
                    created_at    DATETIME DEFAULT (datetime('now')),
                    last_seen     DATETIME
                );
                """;
        stmt.execute(sql);

        // Index for fast username lookups (login, duplicate check)
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);");

        System.out.println(Constants.LOG_PREFIX_DB + " Table ready: users");
    }

    // ─── Create Messages Table ────────────────────────────────────────────────
    private static void createMessagesTable(Statement stmt) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS messages (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender     TEXT    NOT NULL,
                    receiver   TEXT,
                    content    TEXT    NOT NULL,
                    is_private INTEGER NOT NULL DEFAULT 0,
                    sent_at    DATETIME DEFAULT (datetime('now')),
                    FOREIGN KEY (sender)   REFERENCES users(username) ON DELETE CASCADE,
                    FOREIGN KEY (receiver) REFERENCES users(username) ON DELETE SET NULL
                );
                """;
        stmt.execute(sql);

        // Index for fast history queries
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_sender   ON messages(sender);");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_receiver ON messages(receiver);");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_sent_at  ON messages(sent_at);");

        System.out.println(Constants.LOG_PREFIX_DB + " Table ready: messages");
    }

    // ─── Test Connection (utility for debugging) ──────────────────────────────
    /**
     * Quick sanity check — call this in main() to verify DB is reachable.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean ok = conn != null && !conn.isClosed();
            if (ok) {
                System.out.println(Constants.LOG_PREFIX_DB + " Connection test: OK");
            }
            return ok;
        } catch (SQLException e) {
            System.err.println(Constants.LOG_PREFIX_DB + " Connection test FAILED: " + e.getMessage());
            return false;
        }
    }
}