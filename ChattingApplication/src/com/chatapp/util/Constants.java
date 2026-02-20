
package com.chatapp.util;

/**
 * Constants — Central place for all app-wide configuration values.
 *
 * Never hardcode these values elsewhere in the app.
 * Change them here and they reflect everywhere.
 */
public final class Constants {

    // Prevent instantiation
    private Constants() {}

    // ─── Server Config ────────────────────────────────────────────────────────
    public static final String SERVER_HOST         = "localhost";
    public static final int    SERVER_PORT         = 9090;
    public static final int    SERVER_BACKLOG      = 50;   // max queued connections
    public static final int    SOCKET_TIMEOUT_MS   = 0;    // 0 = no timeout

    // ─── Database Config ──────────────────────────────────────────────────────
    public static final String DB_FILE_NAME        = "chatapp.db";
    public static final String DB_URL              = "jdbc:sqlite:" + DB_FILE_NAME;
    public static final String DB_DRIVER           = "org.sqlite.JDBC";

    // ─── Auth / Security ──────────────────────────────────────────────────────
    public static final int    BCRYPT_COST         = 12;   // BCrypt work factor
    public static final int    MIN_USERNAME_LEN    = 3;
    public static final int    MAX_USERNAME_LEN    = 20;
    public static final int    MIN_PASSWORD_LEN    = 6;
    public static final int    MAX_PASSWORD_LEN    = 64;
    public static final String USERNAME_REGEX      = "^[a-zA-Z0-9_]+$";

    // ─── Chat / Messaging ─────────────────────────────────────────────────────
    public static final int    MAX_MESSAGE_LENGTH  = 1000;
    public static final int    HISTORY_LOAD_LIMIT  = 50;   // messages loaded on connect
    public static final String PM_COMMAND          = "/pm";
    public static final String QUIT_COMMAND        = "/quit";
    public static final String LIST_COMMAND        = "/list";
    public static final String USERLIST_PREFIX     = "/userlist";

    // ─── GUI / UI ─────────────────────────────────────────────────────────────
    public static final String APP_NAME            = "ChatApp";
    public static final String APP_VERSION         = "1.0.0";
    public static final int    LOGIN_WINDOW_WIDTH  = 420;
    public static final int    LOGIN_WINDOW_HEIGHT = 340;
    public static final int    CHAT_WINDOW_WIDTH   = 900;
    public static final int    CHAT_WINDOW_HEIGHT  = 620;
    public static final int    CONTACT_PANEL_WIDTH = 220;

    // ─── UI Colors (as hex strings for reference) ─────────────────────────────
    public static final String COLOR_BG_DARK       = "#1E1E2E";
    public static final String COLOR_BG_DARKER     = "#181825";
    public static final String COLOR_SURFACE        = "#313244";
    public static final String COLOR_BLUE           = "#89B4FA";
    public static final String COLOR_GREEN          = "#A6E3A1";
    public static final String COLOR_RED            = "#F38BA8";
    public static final String COLOR_TEXT           = "#CDD6F4";
    public static final String COLOR_SUBTLE         = "#6C7086";

    // ─── Logging ─────────────────────────────────────────────────────────────
    public static final String LOG_PREFIX_SERVER   = "[SERVER]";
    public static final String LOG_PREFIX_CLIENT   = "[CLIENT]";
    public static final String LOG_PREFIX_DB       = "[DB]";
    public static final String LOG_PREFIX_AUTH     = "[AUTH]";
}