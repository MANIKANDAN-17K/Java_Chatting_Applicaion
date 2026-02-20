
package com.chatapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * User — Entity Model
 *
 * Represents a registered user in the ChatApp system.
 * Maps directly to the `users` table in the database.
 */
public class User {

    // ─── Fields ───────────────────────────────────────────────────────────────
    private int id;
    private String username;
    private String passwordHash;       // BCrypt hashed password (never plain text)
    private String displayName;        // Optional display name
    private String status;             // "online" | "offline" | "away"
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // ─── Constructors ─────────────────────────────────────────────────────────

    public User() {}

    // Used when registering a new user
    public User(String username, String passwordHash) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.displayName  = username;
        this.status       = "offline";
        this.createdAt    = LocalDateTime.now();
        this.lastSeen     = LocalDateTime.now();
    }

    // Full constructor (used when loading from DB)
    public User(int id, String username, String passwordHash,
                String displayName, String status,
                LocalDateTime createdAt, LocalDateTime lastSeen) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.displayName  = displayName;
        this.status       = status;
        this.createdAt    = createdAt;
        this.lastSeen     = lastSeen;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int getId()               { return id; }
    public String getUsername()      { return username; }
    public String getPasswordHash()  { return passwordHash; }
    public String getDisplayName()   { return displayName; }
    public String getStatus()        { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastSeen()  { return lastSeen; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                      { this.id = id; }
    public void setUsername(String username)        { this.username = username; }
    public void setPasswordHash(String passwordHash){ this.passwordHash = passwordHash; }
    public void setDisplayName(String displayName)  { this.displayName = displayName; }
    public void setStatus(String status)            { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    public void setLastSeen(LocalDateTime lastSeen)  { this.lastSeen = lastSeen; }

    // ─── Utility Methods ──────────────────────────────────────────────────────

    public boolean isOnline() {
        return "online".equalsIgnoreCase(status);
    }

    public void markOnline() {
        this.status   = "online";
        this.lastSeen = LocalDateTime.now();
    }

    public void markOffline() {
        this.status   = "offline";
        this.lastSeen = LocalDateTime.now();
    }

    public void markAway() {
        this.status = "away";
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(FORMATTER) : "N/A";
    }

    public String getFormattedLastSeen() {
        return lastSeen != null ? lastSeen.format(FORMATTER) : "N/A";
    }

    // ─── Status Icon (for GUI display) ────────────────────────────────────────
    public String getStatusIcon() {
        return switch (status.toLowerCase()) {
            case "online"  -> "🟢";
            case "away"    -> "🟡";
            case "offline" -> "🔴";
            default        -> "⚪";
        };
    }

    // ─── toString ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + getFormattedCreatedAt() +
                ", lastSeen=" + getFormattedLastSeen() +
                '}';
    }

    // ─── equals & hashCode (based on username) ────────────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return username != null && username.equalsIgnoreCase(other.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.toLowerCase().hashCode() : 0;
    }
}