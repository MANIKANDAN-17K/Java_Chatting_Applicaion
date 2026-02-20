package com.chatapp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil — Handles all password hashing and verification.
 *
 * Uses BCrypt — a slow, salted hashing algorithm designed for passwords.
 * Never store or compare plain-text passwords anywhere in the app.
 *
 * Dependency: jbcrypt-0.4.jar  (add to /lib folder)
 * Maven:      org.mindrot:jbcrypt:0.4
 */
public final class PasswordUtil {

    // Prevent instantiation
    private PasswordUtil() {}

    // ─── Hash Password ────────────────────────────────────────────────────────
    /**
     * Hashes a plain-text password using BCrypt with a random salt.
     *
     * BCrypt automatically embeds the salt into the returned hash string,
     * so you never need to store the salt separately.
     *
     * @param plainPassword the raw password entered by the user
     * @return a BCrypt hash string (60 characters)
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        String salt = BCrypt.gensalt(Constants.BCRYPT_COST);
        return BCrypt.hashpw(plainPassword, salt);
    }

    // ─── Verify Password ──────────────────────────────────────────────────────
    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainPassword  the raw password entered during login
     * @param hashedPassword the hash stored in the database
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) return false;
        if (hashedPassword == null || hashedPassword.isEmpty()) return false;

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Malformed hash in DB — log and return false
            System.err.println(Constants.LOG_PREFIX_AUTH
                    + " Invalid hash format during verification: " + e.getMessage());
            return false;
        }
    }

    // ─── Validate Password Strength ───────────────────────────────────────────
    /**
     * Checks if a password meets the app's minimum strength requirements.
     *
     * Rules:
     *  - At least MIN_PASSWORD_LEN characters
     *  - At most MAX_PASSWORD_LEN characters
     *  - Contains at least one letter
     *  - Contains at least one digit
     *
     * @param password the plain-text password to check
     * @return StrengthResult with a pass/fail flag and feedback message
     */
    public static StrengthResult checkStrength(String password) {
        if (password == null || password.isEmpty()) {
            return StrengthResult.weak("Password cannot be empty.");
        }
        if (password.length() < Constants.MIN_PASSWORD_LEN) {
            return StrengthResult.weak(
                    "Password must be at least " + Constants.MIN_PASSWORD_LEN + " characters.");
        }
        if (password.length() > Constants.MAX_PASSWORD_LEN) {
            return StrengthResult.weak(
                    "Password cannot exceed " + Constants.MAX_PASSWORD_LEN + " characters.");
        }

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit  = password.chars().anyMatch(Character::isDigit);

        if (!hasLetter) {
            return StrengthResult.weak("Password must contain at least one letter.");
        }
        if (!hasDigit) {
            return StrengthResult.weak("Password must contain at least one number.");
        }

        // Bonus: check for special character (strong password)
        boolean hasSpecial = password.chars().anyMatch(c ->
                !Character.isLetterOrDigit(c));

        if (hasSpecial && password.length() >= 10) {
            return StrengthResult.strong("Strong password!");
        }

        return StrengthResult.ok("Password is acceptable.");
    }

    // ─── Sanitize Before Hashing ──────────────────────────────────────────────
    /**
     * Trims leading/trailing whitespace from a password before hashing.
     * Call this before hash() if you want consistent behavior regardless
     * of accidental spaces entered by the user.
     */
    public static String sanitize(String password) {
        return password == null ? "" : password.trim();
    }

    // ─── Inner Class: StrengthResult ──────────────────────────────────────────
    /**
     * Encapsulates the result of a password strength check.
     */
    public static class StrengthResult {

        public enum Level { WEAK, OK, STRONG }

        private final boolean passes;
        private final String  feedback;
        private final Level   level;

        private StrengthResult(boolean passes, String feedback, Level level) {
            this.passes   = passes;
            this.feedback = feedback;
            this.level    = level;
        }

        public static StrengthResult weak(String feedback) {
            return new StrengthResult(false, feedback, Level.WEAK);
        }

        public static StrengthResult ok(String feedback) {
            return new StrengthResult(true, feedback, Level.OK);
        }

        public static StrengthResult strong(String feedback) {
            return new StrengthResult(true, feedback, Level.STRONG);
        }

        public boolean passes()    { return passes;   }
        public String  getFeedback() { return feedback; }
        public Level   getLevel()  { return level;    }
        public boolean isStrong()  { return level == Level.STRONG; }

        @Override
        public String toString() {
            return "StrengthResult{level=" + level + ", feedback='" + feedback + "'}";
        }
    }
}