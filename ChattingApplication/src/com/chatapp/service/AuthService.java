
package com.chatapp.service;

import com.chatapp.dao.UserDAO;
import com.chatapp.model.User;
import com.chatapp.util.PasswordUtil;

import java.time.LocalDateTime;

/**
 * AuthService — Business logic for authentication.
 *
 * Handles:
 *  - User registration (validate → hash password → save)
 *  - User login (validate → fetch → verify hash)
 *  - Input validation rules
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // ─── Constants ────────────────────────────────────────────────────────────
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 64;

    // ─── Register ─────────────────────────────────────────────────────────────
    /**
     * Registers a new user.
     *
     * Steps:
     *  1. Validate username and password format
     *  2. Check if username is already taken
     *  3. Hash the password using BCrypt
     *  4. Save the new user to the database
     *
     * @param username  desired username
     * @param password  plain-text password (will be hashed)
     * @return AuthResult with success flag and message
     */
    public AuthResult register(String username, String password) {
        // ── Validate inputs ───────────────────────────────────────────────────
        AuthResult validation = validateInputs(username, password);
        if (!validation.isSuccess()) {
            return validation;
        }

        // ── Check username characters ─────────────────────────────────────────
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return AuthResult.fail("Username can only contain letters, numbers, and underscores.");
        }

        // ── Check if username already taken ───────────────────────────────────
        if (userDAO.usernameExists(username)) {
            return AuthResult.fail("Username '" + username + "' is already taken.");
        }

        // ── Hash password ─────────────────────────────────────────────────────
        String passwordHash;
        try {
            passwordHash = PasswordUtil.hash(password);
        } catch (Exception e) {
            System.err.println("[AuthService] Hashing error: " + e.getMessage());
            return AuthResult.fail("Registration failed. Please try again.");
        }

        // ── Build user object ─────────────────────────────────────────────────
        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setPasswordHash(passwordHash);
        newUser.setCreatedAt(LocalDateTime.now());

        // ── Persist to DB ─────────────────────────────────────────────────────
        boolean saved = userDAO.insertUser(newUser);
        if (!saved) {
            return AuthResult.fail("Could not create account. Please try again.");
        }

        System.out.println("[AuthService] New user registered: " + username);
        return AuthResult.success("Account created successfully! Welcome, " + username + "!");
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    /**
     * Authenticates a user.
     *
     * Steps:
     *  1. Validate inputs are not blank
     *  2. Fetch user from DB by username
     *  3. Verify the plain-text password against the stored hash
     *  4. Update last_seen timestamp on success
     *
     * @param username  entered username
     * @param password  entered plain-text password
     * @return AuthResult with success flag, message, and user object on success
     */
    public AuthResult login(String username, String password) {
        // ── Basic blank check ─────────────────────────────────────────────────
        if (username == null || username.trim().isEmpty()) {
            return AuthResult.fail("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            return AuthResult.fail("Password cannot be empty.");
        }

        // ── Fetch user ────────────────────────────────────────────────────────
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            // Return generic message to avoid username enumeration
            return AuthResult.fail("Invalid username or password.");
        }

        // ── Verify password ───────────────────────────────────────────────────
        boolean passwordMatches;
        try {
            passwordMatches = PasswordUtil.verify(password, user.getPasswordHash());
        } catch (Exception e) {
            System.err.println("[AuthService] Password verification error: " + e.getMessage());
            return AuthResult.fail("Login failed. Please try again.");
        }

        if (!passwordMatches) {
            System.out.println("[AuthService] Failed login attempt for: " + username);
            return AuthResult.fail("Invalid username or password.");
        }

        // ── Update last seen ──────────────────────────────────────────────────
        userDAO.updateLastSeen(username.trim());

        System.out.println("[AuthService] User logged in: " + username);
        return AuthResult.successWithUser("Login successful! Welcome back, " + username + "!", user);
    }

    // ─── Change Password ──────────────────────────────────────────────────────
    /**
     * Changes a user's password after verifying the old one.
     *
     * @param username    the user requesting the change
     * @param oldPassword current plain-text password
     * @param newPassword new plain-text password
     * @return AuthResult with success or failure message
     */
    public AuthResult changePassword(String username, String oldPassword, String newPassword) {
        // Verify old password first
        AuthResult loginCheck = login(username, oldPassword);
        if (!loginCheck.isSuccess()) {
            return AuthResult.fail("Current password is incorrect.");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            return AuthResult.fail("New password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (newPassword.equals(oldPassword)) {
            return AuthResult.fail("New password must be different from the current password.");
        }

        // Hash and save
        try {
            String newHash = PasswordUtil.hash(newPassword);
            boolean updated = userDAO.updatePassword(username, newHash);
            if (updated) {
                return AuthResult.success("Password changed successfully.");
            } else {
                return AuthResult.fail("Failed to update password. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("[AuthService] changePassword error: " + e.getMessage());
            return AuthResult.fail("An error occurred. Please try again.");
        }
    }

    // ─── Input Validation ─────────────────────────────────────────────────────
    private AuthResult validateInputs(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return AuthResult.fail("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            return AuthResult.fail("Password cannot be empty.");
        }
        if (username.trim().length() < MIN_USERNAME_LENGTH) {
            return AuthResult.fail("Username must be at least " + MIN_USERNAME_LENGTH + " characters.");
        }
        if (username.trim().length() > MAX_USERNAME_LENGTH) {
            return AuthResult.fail("Username cannot exceed " + MAX_USERNAME_LENGTH + " characters.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return AuthResult.fail("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return AuthResult.fail("Password cannot exceed " + MAX_PASSWORD_LENGTH + " characters.");
        }
        return AuthResult.success("Valid");
    }

    // ─── Inner Class: AuthResult ──────────────────────────────────────────────
    /**
     * Encapsulates the result of any auth operation.
     * Carries a success flag, a human-readable message, and optionally the User.
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        private AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user    = user;
        }

        public static AuthResult success(String message) {
            return new AuthResult(true, message, null);
        }

        public static AuthResult successWithUser(String message, User user) {
            return new AuthResult(true, message, user);
        }

        public static AuthResult fail(String message) {
            return new AuthResult(false, message, null);
        }

        public boolean isSuccess()  { return success; }
        public String getMessage()  { return message; }
        public User   getUser()     { return user;    }

        @Override
        public String toString() {
            return "AuthResult{success=" + success + ", message='" + message + "'}";
        }
    }
}