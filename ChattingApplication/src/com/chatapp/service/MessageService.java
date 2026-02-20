
package com.chatapp.service;

import com.chatapp.dao.MessageDAO;
import com.chatapp.model.Message;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * MessageService — Business logic for chat messages.
 *
 * Handles:
 *  - Sending global messages
 *  - Sending private messages
 *  - Fetching chat history (global + private)
 *  - Message validation and sanitization
 *  - Deleting messages
 */
public class MessageService {

    private final MessageDAO messageDAO = new MessageDAO();

    // ─── Constants ────────────────────────────────────────────────────────────
    private static final int MAX_MESSAGE_LENGTH  = 1000;
    private static final int DEFAULT_HISTORY_LIMIT = 50;

    // ─── Send Global Message ──────────────────────────────────────────────────
    /**
     * Saves a global (broadcast) message to the database.
     *
     * @param sender   username of the sender
     * @param content  raw message text
     * @return MessageResult with saved Message on success
     */
    public MessageResult sendGlobalMessage(String sender, String content) {
        // ── Validate ──────────────────────────────────────────────────────────
        MessageResult validation = validateMessage(sender, content);
        if (!validation.isSuccess()) return validation;

        // ── Build message object ──────────────────────────────────────────────
        Message message = new Message();
        message.setSender(sender.trim());
        message.setReceiver(null);          // null = global
        message.setContent(sanitize(content));
        message.setPrivate(false);
        message.setSentAt(LocalDateTime.now());

        // ── Persist ───────────────────────────────────────────────────────────
        int generatedId = messageDAO.insertMessage(message);
        if (generatedId < 0) {
            return MessageResult.fail("Failed to save message.");
        }

        message.setId(generatedId);
        System.out.println("[MessageService] Global message saved | ID: " + generatedId
                + " | From: " + sender);
        return MessageResult.success("Message sent.", message);
    }

    // ─── Send Private Message ─────────────────────────────────────────────────
    /**
     * Saves a private message between two users to the database.
     *
     * @param sender   username of sender
     * @param receiver username of recipient
     * @param content  raw message text
     * @return MessageResult with saved Message on success
     */
    public MessageResult sendPrivateMessage(String sender, String receiver, String content) {
        // ── Validate ──────────────────────────────────────────────────────────
        MessageResult validation = validateMessage(sender, content);
        if (!validation.isSuccess()) return validation;

        if (receiver == null || receiver.trim().isEmpty()) {
            return MessageResult.fail("Recipient username cannot be empty.");
        }
        if (sender.trim().equalsIgnoreCase(receiver.trim())) {
            return MessageResult.fail("You cannot send a private message to yourself.");
        }

        // ── Build message object ──────────────────────────────────────────────
        Message message = new Message();
        message.setSender(sender.trim());
        message.setReceiver(receiver.trim());
        message.setContent(sanitize(content));
        message.setPrivate(true);
        message.setSentAt(LocalDateTime.now());

        // ── Persist ───────────────────────────────────────────────────────────
        int generatedId = messageDAO.insertMessage(message);
        if (generatedId < 0) {
            return MessageResult.fail("Failed to save private message.");
        }

        message.setId(generatedId);
        System.out.println("[MessageService] Private message saved | ID: " + generatedId
                + " | " + sender + " → " + receiver);
        return MessageResult.success("Private message sent.", message);
    }

    // ─── Get Global Chat History ──────────────────────────────────────────────
    /**
     * Returns the last N global messages (oldest first).
     * Used when a user first opens the chat to load history.
     *
     * @param limit number of messages (default: 50)
     */
    public List<Message> getGlobalHistory(int limit) {
        int safeLimit = (limit <= 0) ? DEFAULT_HISTORY_LIMIT : Math.min(limit, 200);
        try {
            return messageDAO.getGlobalMessages(safeLimit);
        } catch (Exception e) {
            System.err.println("[MessageService] getGlobalHistory error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Message> getGlobalHistory() {
        return getGlobalHistory(DEFAULT_HISTORY_LIMIT);
    }

    // ─── Get Private Conversation History ─────────────────────────────────────
    /**
     * Returns the last N private messages between two users (oldest first).
     *
     * @param user1  first participant
     * @param user2  second participant
     * @param limit  max number of messages
     */
    public List<Message> getPrivateHistory(String user1, String user2, int limit) {
        if (user1 == null || user2 == null) return Collections.emptyList();

        int safeLimit = (limit <= 0) ? DEFAULT_HISTORY_LIMIT : Math.min(limit, 200);
        try {
            return messageDAO.getPrivateMessages(user1.trim(), user2.trim(), safeLimit);
        } catch (Exception e) {
            System.err.println("[MessageService] getPrivateHistory error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Message> getPrivateHistory(String user1, String user2) {
        return getPrivateHistory(user1, user2, DEFAULT_HISTORY_LIMIT);
    }

    // ─── Get Messages Since Timestamp ─────────────────────────────────────────
    /**
     * Fetches global messages sent after a given timestamp.
     * Useful for syncing missed messages when a user reconnects.
     *
     * @param since the cutoff timestamp
     */
    public List<Message> getMessagesSince(LocalDateTime since) {
        if (since == null) return Collections.emptyList();
        try {
            return messageDAO.getMessagesSince(since);
        } catch (Exception e) {
            System.err.println("[MessageService] getMessagesSince error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // ─── Format Message for Display ───────────────────────────────────────────
    /**
     * Converts a Message object into the display string format used by the GUI.
     * Matches the format expected by MessagePanel.
     *
     * Global:  "[username] message text"
     * Private: "[PM from username] message text"
     */
    public String formatForDisplay(Message message) {
        if (message == null) return "";

        if (message.isPrivate()) {
            return "[PM from " + message.getSender() + "] " + message.getContent();
        } else {
            return "[" + message.getSender() + "] " + message.getContent();
        }
    }

    // ─── Delete Message ───────────────────────────────────────────────────────
    /**
     * Deletes a message by ID.
     * Only the sender or an admin should be allowed to call this.
     *
     * @param messageId  the ID of the message to delete
     * @param requestingUser  username of who is requesting deletion
     * @return MessageResult indicating success or failure
     */
    public MessageResult deleteMessage(int messageId, String requestingUser) {
        // Fetch message to verify ownership
        Message existing = messageDAO.getMessageById(messageId);
        if (existing == null) {
            return MessageResult.fail("Message not found.");
        }
        if (!existing.getSender().equalsIgnoreCase(requestingUser)) {
            return MessageResult.fail("You can only delete your own messages.");
        }

        boolean deleted = messageDAO.deleteMessage(messageId);
        if (deleted) {
            System.out.println("[MessageService] Message deleted | ID: " + messageId
                    + " | By: " + requestingUser);
            return MessageResult.success("Message deleted.", null);
        }
        return MessageResult.fail("Failed to delete message.");
    }

    // ─── Get Message Count for User ───────────────────────────────────────────
    public int getMessageCount(String username) {
        return messageDAO.countMessagesBySender(username);
    }

    // ─── Validate Message ─────────────────────────────────────────────────────
    private MessageResult validateMessage(String sender, String content) {
        if (sender == null || sender.trim().isEmpty()) {
            return MessageResult.fail("Sender cannot be empty.");
        }
        if (content == null || content.trim().isEmpty()) {
            return MessageResult.fail("Message cannot be empty.");
        }
        if (content.trim().length() > MAX_MESSAGE_LENGTH) {
            return MessageResult.fail("Message is too long. Max " + MAX_MESSAGE_LENGTH + " characters.");
        }
        return MessageResult.success("Valid", null);
    }

    // ─── Sanitize Message Content ─────────────────────────────────────────────
    /**
     * Trims whitespace and strips any null characters from content.
     */
    private String sanitize(String content) {
        return content.trim().replace("\0", "");
    }

    // ─── Inner Class: MessageResult ───────────────────────────────────────────
    /**
     * Wraps the outcome of a message operation.
     * Carries a success flag, message string, and optionally the saved Message.
     */
    public static class MessageResult {
        private final boolean success;
        private final String  message;
        private final Message data;

        private MessageResult(boolean success, String message, Message data) {
            this.success = success;
            this.message = message;
            this.data    = data;
        }

        public static MessageResult success(String message, Message data) {
            return new MessageResult(true, message, data);
        }

        public static MessageResult fail(String message) {
            return new MessageResult(false, message, null);
        }

        public boolean isSuccess()  { return success; }
        public String  getMessage() { return message; }
        public Message getData()    { return data;    }

        @Override
        public String toString() {
            return "MessageResult{success=" + success + ", message='" + message + "'}";
        }
    }
}