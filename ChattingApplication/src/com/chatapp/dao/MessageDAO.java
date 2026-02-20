
package com.chatapp.dao;

import com.chatapp.model.Message;
import com.chatapp.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MessageDAO — Data Access Object for Message table.
 *
 * Handles saving, fetching, and deleting chat messages.
 * Supports both global messages and private messages.
 */
public class MessageDAO {

    // ─── Insert Message ───────────────────────────────────────────────────────
    /**
     * Saves a new message to the database.
     * Works for both global (receiver = null) and private messages.
     *
     * @return generated message ID, or -1 on failure
     */
    public int insertMessage(Message message) {
        String sql = "INSERT INTO messages (sender, receiver, content, is_private, sent_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, message.getSender());
            stmt.setString(2, message.getReceiver()); // null for global
            stmt.setString(3, message.getContent());
            stmt.setBoolean(4, message.isPrivate());
            stmt.setTimestamp(5, Timestamp.valueOf(
                    message.getSentAt() != null ? message.getSentAt() : LocalDateTime.now()));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[MessageDAO] insertMessage error: " + e.getMessage());
        }

        return -1;
    }

    // ─── Get Global Chat History ──────────────────────────────────────────────
    /**
     * Fetches the last N global (non-private) messages ordered oldest→newest.
     *
     * @param limit number of messages to retrieve
     */
    public List<Message> getGlobalMessages(int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages " +
                     "WHERE is_private = 0 " +
                     "ORDER BY sent_at DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(0, mapRow(rs)); // reverse to get oldest first
                }
            }

        } catch (SQLException e) {
            System.err.println("[MessageDAO] getGlobalMessages error: " + e.getMessage());
        }

        return messages;
    }

    // ─── Get Private Conversation History ─────────────────────────────────────
    /**
     * Fetches the last N private messages exchanged between two users.
     * Retrieves messages in both directions (user1→user2 and user2→user1).
     *
     * @param user1  first participant
     * @param user2  second participant
     * @param limit  number of messages to retrieve
     */
    public List<Message> getPrivateMessages(String user1, String user2, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages " +
                     "WHERE is_private = 1 " +
                     "AND ((sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)) " +
                     "ORDER BY sent_at DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user1);
            stmt.setString(2, user2);
            stmt.setString(3, user2);
            stmt.setString(4, user1);
            stmt.setInt(5, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(0, mapRow(rs)); // reverse to get oldest first
                }
            }

        } catch (SQLException e) {
            System.err.println("[MessageDAO] getPrivateMessages error: " + e.getMessage());
        }

        return messages;
    }

    // ─── Get Messages by Sender ───────────────────────────────────────────────
    /**
     * Fetches all messages sent by a specific user.
     */
    public List<Message> getMessagesBySender(String sender) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE sender = ? ORDER BY sent_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sender);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[MessageDAO] getMessagesBySender error: " + e.getMessage());
        }

        return messages;
    }

    // ─── Get Message by ID ────────────────────────────────────────────────────
    public Message getMessageById(int id) {
        String sql = "SELECT * FROM messages WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[MessageDAO] getMessageById error: " + e.getMessage());
        }

        return null;
    }

    // ─── Get Recent Messages Since Timestamp ──────────────────────────────────
    /**
     * Useful for loading messages a user missed while offline.
     */
    public List<Message> getMessagesSince(LocalDateTime since) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages " +
                     "WHERE is_private = 0 AND sent_at >= ? " +
                     "ORDER BY sent_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(since));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[MessageDAO] getMessagesSince error: " + e.getMessage());
        }

        return messages;
    }

    // ─── Count Messages by User ───────────────────────────────────────────────
    public int countMessagesBySender(String sender) {
        String sql = "SELECT COUNT(*) FROM messages WHERE sender = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sender);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[MessageDAO] countMessagesBySender error: " + e.getMessage());
        }

        return 0;
    }

    // ─── Delete Message by ID ─────────────────────────────────────────────────
    /**
     * Deletes a specific message by its ID.
     *
     * @return true if deleted successfully
     */
    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[MessageDAO] deleteMessage error: " + e.getMessage());
            return false;
        }
    }

    // ─── Delete All Messages by User ──────────────────────────────────────────
    /**
     * Deletes all messages sent by a user (e.g., on account deletion).
     */
    public boolean deleteAllByUser(String username) {
        String sql = "DELETE FROM messages WHERE sender = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[MessageDAO] deleteAllByUser error: " + e.getMessage());
            return false;
        }
    }

    // ─── Map ResultSet Row → Message Object ──────────────────────────────────
    private Message mapRow(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setId(rs.getInt("id"));
        msg.setSender(rs.getString("sender"));
        msg.setReceiver(rs.getString("receiver"));
        msg.setContent(rs.getString("content"));
        msg.setPrivate(rs.getBoolean("is_private"));

        Timestamp sentAt = rs.getTimestamp("sent_at");
        if (sentAt != null) {
            msg.setSentAt(sentAt.toLocalDateTime());
        }

        return msg;
    }
}