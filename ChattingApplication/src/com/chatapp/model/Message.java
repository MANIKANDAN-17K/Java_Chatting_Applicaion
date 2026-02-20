
package com.chatapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message — Entity Model
 *
 * Represents a single chat message in the ChatApp system.
 * Supports both global (broadcast) messages and private (DM) messages.
 * Maps directly to the `messages` table in the database.
 */
public class Message {

    // ─── Message Type Enum ────────────────────────────────────────────────────
    public enum MessageType {
        GLOBAL,     // Broadcast to all users
        PRIVATE,    // Direct message between two users
        SYSTEM      // Server-generated notification
    }

    // ─── Fields ───────────────────────────────────────────────────────────────
    private int id;
    private String senderUsername;
    private String receiverUsername;   // null for GLOBAL / SYSTEM messages
    private String content;
    private MessageType type;
    private LocalDateTime sentAt;
    private boolean isRead;            // For private messages

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    // ─── Constructors ─────────────────────────────────────────────────────────

    public Message() {}

    // Global message
    public Message(String senderUsername, String content) {
        this.senderUsername   = senderUsername;
        this.receiverUsername = null;
        this.content          = content;
        this.type             = MessageType.GLOBAL;
        this.sentAt           = LocalDateTime.now();
        this.isRead           = true;
    }

    // Private (DM) message
    public Message(String senderUsername, String receiverUsername, String content) {
        this.senderUsername   = senderUsername;
        this.receiverUsername = receiverUsername;
        this.content          = content;
        this.type             = MessageType.PRIVATE;
        this.sentAt           = LocalDateTime.now();
        this.isRead           = false;
    }

    // System message
    public static Message systemMessage(String content) {
        Message msg = new Message();
        msg.senderUsername   = "SERVER";
        msg.receiverUsername = null;
        msg.content          = content;
        msg.type             = MessageType.SYSTEM;
        msg.sentAt           = LocalDateTime.now();
        msg.isRead           = true;
        return msg;
    }

    // Full constructor (used when loading from DB)
    public Message(int id, String senderUsername, String receiverUsername,
                   String content, MessageType type,
                   LocalDateTime sentAt, boolean isRead) {
        this.id               = id;
        this.senderUsername   = senderUsername;
        this.receiverUsername = receiverUsername;
        this.content          = content;
        this.type             = type;
        this.sentAt           = sentAt;
        this.isRead           = isRead;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int getId()                   { return id; }
    public String getSenderUsername()    { return senderUsername; }
    public String getReceiverUsername()  { return receiverUsername; }
    public String getContent()           { return content; }
    public MessageType getType()         { return type; }
    public LocalDateTime getSentAt()     { return sentAt; }
    public boolean isRead()              { return isRead; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                              { this.id = id; }
    public void setSenderUsername(String senderUsername)   { this.senderUsername = senderUsername; }
    public void setReceiverUsername(String receiverUsername){ this.receiverUsername = receiverUsername; }
    public void setContent(String content)                 { this.content = content; }
    public void setType(MessageType type)                  { this.type = type; }
    public void setSentAt(LocalDateTime sentAt)            { this.sentAt = sentAt; }
    public void setRead(boolean read)                      { this.isRead = read; }

    // ─── Utility Methods ──────────────────────────────────────────────────────

    public boolean isGlobal()  { return type == MessageType.GLOBAL; }
    public boolean isPrivate() { return type == MessageType.PRIVATE; }
    public boolean isSystem()  { return type == MessageType.SYSTEM; }

    public void markAsRead() {
        this.isRead = true;
    }

    // ─── Formatted Time (for GUI bubble display) ──────────────────────────────
    public String getFormattedTime() {
        return sentAt != null ? sentAt.format(TIME_FMT) : "";
    }

    public String getFormattedDateTime() {
        return sentAt != null ? sentAt.format(DISPLAY_FMT) : "N/A";
    }

    // ─── Format as wire string (sent over socket) ─────────────────────────────
    // Format: [sender] content  OR  [PM from sender] content
    public String toWireFormat() {
        return switch (type) {
            case GLOBAL  -> "[" + senderUsername + "] " + content;
            case PRIVATE -> "[PM from " + senderUsername + "] " + content;
            case SYSTEM  -> "[SERVER] " + content;
        };
    }

    // ─── toString ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", from='" + senderUsername + '\'' +
                (receiverUsername != null ? ", to='" + receiverUsername + '\'' : "") +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", sentAt=" + getFormattedDateTime() +
                ", isRead=" + isRead +
                '}';
    }

    // ─── equals & hashCode (based on id) ─────────────────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message other = (Message) o;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}