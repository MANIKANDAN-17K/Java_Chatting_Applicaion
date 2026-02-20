
package com.chatapp.server;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // ─── Run (Thread Entry) ───────────────────────────────────────────────────
    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // ── Step 1: Read username sent by client on connect ───────────────
            username = reader.readLine();
            if (username == null || username.trim().isEmpty()) {
                socket.close();
                return;
            }
            username = username.trim();

            // ── Step 2: Check if username is already taken ────────────────────
            SessionManager sessions = SessionManager.getInstance();
            if (sessions.isUserOnline(username)) {
                sendMessage("[SYSTEM] Username '" + username + "' is already in use.");
                socket.close();
                return;
            }

            // ── Step 3: Register this client ──────────────────────────────────
            sessions.addClient(username, this);
            ChatServer.log("User joined: " + username +
                    " | Online: " + sessions.getOnlineCount());

            // ── Step 4: Welcome this client ───────────────────────────────────
            sendMessage("[SYSTEM] Welcome, " + username + "! You are now connected.");
            sendMessage("[SYSTEM] " + sessions.getOnlineCount() + " user(s) online.");

            // ── Step 5: Announce to everyone else ─────────────────────────────
            sessions.broadcastExcept(username,
                    "[SERVER] 🟢 " + username + " has joined the chat.");

            // ── Step 6: Push updated user list to all ─────────────────────────
            sessions.broadcastUserList();

            // ── Step 7: Listen for messages ───────────────────────────────────
            String incoming;
            while ((incoming = reader.readLine()) != null) {
                incoming = incoming.trim();
                if (incoming.isEmpty()) continue;

                ChatServer.log("[" + username + "]: " + incoming);

                if (incoming.equalsIgnoreCase("/quit")) {
                    break;

                } else if (incoming.startsWith("/pm ")) {
                    handlePrivateMessage(incoming);

                } else if (incoming.equalsIgnoreCase("/list")) {
                    handleListUsers();

                } else if (incoming.startsWith("/")) {
                    sendMessage("[SYSTEM] Unknown command: " + incoming);

                } else {
                    // Global broadcast with timestamp
                    String time = LocalTime.now().format(TIME_FMT);
                    String formatted = "[" + username + "] " + incoming;
                    sessions.broadcastAll(formatted);
                }
            }

        } catch (IOException e) {
            ChatServer.log("Connection lost for " + username + ": " + e.getMessage());
        } finally {
            handleDisconnect();
        }
    }

    // ─── Handle Private Message (/pm <target> <message>) ─────────────────────
    private void handlePrivateMessage(String raw) {
        // Format: /pm username message text here
        String[] parts = raw.split(" ", 3);
        if (parts.length < 3) {
            sendMessage("[SYSTEM] Usage: /pm <username> <message>");
            return;
        }

        String targetUser = parts[1].trim();
        String message    = parts[2].trim();

        SessionManager sessions = SessionManager.getInstance();

        if (targetUser.equalsIgnoreCase(username)) {
            sendMessage("[SYSTEM] You cannot send a private message to yourself.");
            return;
        }

        ClientHandler target = sessions.getClient(targetUser);
        if (target == null) {
            sendMessage("[SYSTEM] User '" + targetUser + "' is not online.");
            return;
        }

        String pm = "[PM from " + username + "] " + message;
        target.sendMessage(pm);
        ChatServer.log("[PM] " + username + " → " + targetUser + ": " + message);
    }

    // ─── Handle /list command ─────────────────────────────────────────────────
    private void handleListUsers() {
        SessionManager sessions = SessionManager.getInstance();
        String[] users = sessions.getOnlineUsernames();
        StringBuilder sb = new StringBuilder("[SYSTEM] Online users (" + users.length + "): ");
        for (int i = 0; i < users.length; i++) {
            sb.append(users[i]);
            if (i < users.length - 1) sb.append(", ");
        }
        sendMessage(sb.toString());
    }

    // ─── Handle Disconnect ────────────────────────────────────────────────────
    private void handleDisconnect() {
        SessionManager sessions = SessionManager.getInstance();

        if (username != null) {
            sessions.removeClient(username);
            ChatServer.log("User left: " + username +
                    " | Online: " + sessions.getOnlineCount());

            sessions.broadcastExcept(username,
                    "[SERVER] 🔴 " + username + " has left the chat.");
            sessions.broadcastUserList();
        }

        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            ChatServer.log("Error closing socket for " + username);
        }
    }

    // ─── Send Message to THIS client ─────────────────────────────────────────
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    // ─── Getters ─────────────────────────────────────────────────────────────
    public String getUsername() {
        return username;
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    // ─── Force close (called by SessionManager on shutdown) ──────────────────
    public void forceClose() {
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}