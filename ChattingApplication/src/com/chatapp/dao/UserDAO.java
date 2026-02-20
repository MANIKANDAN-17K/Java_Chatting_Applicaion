
package com.chatapp.dao;

import com.chatapp.model.User;
import com.chatapp.util.DBConnection;
import com.sun.jdi.connect.spi.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — Data Access Object for User table.
 *
 * Handles all CRUD operations related to users:
 * insert, find by username, find by id, update, delete, list all.
 */
public class UserDAO {

    // ─── Insert / Register New User ───────────────────────────────────────────
    /**
     * Inserts a new user into the database.
     * Password should already be hashed before calling this.
     *
     * @return true if inserted successfully, false if username already exists
     */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setTimestamp(3, Timestamp.valueOf(
                    user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            // Unique constraint violation — username taken
            if (e.getErrorCode() == 19) { // SQLite: SQLITE_CONSTRAINT
                System.err.println("[UserDAO] Username already exists: " + user.getUsername());
            } else {
                System.err.println("[UserDAO] insertUser error: " + e.getMessage());
            }
            return false;
        }
    }

    // ─── Find User by Username ────────────────────────────────────────────────
    /**
     * Fetches a user by their username.
     *
     * @return User object or null if not found
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] findByUsername error: " + e.getMessage());
        }

        return null;
    }

    // ─── Find User by ID ──────────────────────────────────────────────────────
    /**
     * Fetches a user by their primary key ID.
     *
     * @return User object or null if not found
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] findById error: " + e.getMessage());
        }

        return null;
    }

    // ─── Check if Username Exists ─────────────────────────────────────────────
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] usernameExists error: " + e.getMessage());
        }

        return false;
    }

    // ─── Get All Users ────────────────────────────────────────────────────────
    /**
     * Returns a list of all registered users.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }

        return users;
    }

    // ─── Update Password ──────────────────────────────────────────────────────
    /**
     * Updates a user's hashed password.
     *
     * @return true if updated successfully
     */
    public boolean updatePassword(String username, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setString(2, username);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updatePassword error: " + e.getMessage());
            return false;
        }
    }

    // ─── Update Last Seen ─────────────────────────────────────────────────────
    /**
     * Updates the last_seen timestamp for a user (called on login/logout).
     */
    public boolean updateLastSeen(String username) {
        String sql = "UPDATE users SET last_seen = ? WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, username);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updateLastSeen error: " + e.getMessage());
            return false;
        }
    }

    // ─── Delete User ──────────────────────────────────────────────────────────
    /**
     * Deletes a user from the database by username.
     *
     * @return true if deleted successfully
     */
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] deleteUser error: " + e.getMessage());
            return false;
        }
    }

    // ─── Map ResultSet Row → User Object ─────────────────────────────────────
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp lastSeen = rs.getTimestamp("last_seen");
        if (lastSeen != null) {
            user.setLastSeen(lastSeen.toLocalDateTime());
        }

        return user;
    }
}