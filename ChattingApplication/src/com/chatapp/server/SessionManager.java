
package com.chatapp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager — Singleton
 *
 * Keeps track of all currently connected clients.
 * Thread-safe using ConcurrentHashMap.
 * Provides broadcast utilities for global, targeted, and user-list messages.
 */
public class SessionManager {

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static SessionManager instance;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ─── Active Clients Map: username → ClientHandler ─────────────────────────
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    // ─── Add Client ───────────────────────────────────────────────────────────
    public void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        ChatServer.log("Session registered: " + username);
    }

    // ─── Remove Client ────────────────────────────────────────────────────────
    public void removeClient(String username) {
        clients.remove(username);
        ChatServer.log("Session removed: " + username);
    }

    // ─── Get a Specific Client ────────────────────────────────────────────────
    public ClientHandler getClient(String username) {
        return clients.get(username);
    }

    // ─── Check if User is Online ──────────────────────────────────────────────
    public boolean isUserOnline(String username) {
        return clients.containsKey(username);
    }

    // ─── Get Online Count ─────────────────────────────────────────────────────
    public int getOnlineCount() {
        return clients.size();
    }

    // ─── Get All Online Usernames ─────────────────────────────────────────────
    public String[] getOnlineUsernames() {
        return clients.keySet().toArray(new String[0]);
    }

    // ─── Broadcast to ALL clients ─────────────────────────────────────────────
    public void broadcastAll(String message) {
        ChatServer.log("[BROADCAST] " + message);
        for (ClientHandler handler : clients.values()) {
            if (handler.isConnected()) {
                handler.sendMessage(message);
            }
        }
    }

    // ─── Broadcast to everyone EXCEPT one user ────────────────────────────────
    public void broadcastExcept(String excludeUsername, String message) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(excludeUsername) && entry.getValue().isConnected()) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    // ─── Broadcast system message to ALL ─────────────────────────────────────
    public void broadcastSystemMessage(String message) {
        broadcastAll("[SERVER] " + message);
    }

    // ─── Broadcast updated user list to ALL clients ───────────────────────────
    // Format: /userlist user1,user2,user3
    // Client side should parse this to update the ContactPanel
    public void broadcastUserList() {
        String[] usernames = getOnlineUsernames();
        String userListMessage = "/userlist " + String.join(",", usernames);

        for (ClientHandler handler : clients.values()) {
            if (handler.isConnected()) {
                handler.sendMessage(userListMessage);
            }
        }

        ChatServer.log("User list pushed to all clients. Online: " + clients.size());
    }

    // ─── Send message to a specific user ─────────────────────────────────────
    public boolean sendToUser(String username, String message) {
        ClientHandler handler = clients.get(username);
        if (handler != null && handler.isConnected()) {
            handler.sendMessage(message);
            return true;
        }
        return false;
    }

    // ─── Get snapshot of all sessions (for admin/debug) ──────────────────────
    public List<String> getSessionSnapshot() {
        List<String> snapshot = new ArrayList<>();
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            snapshot.add(entry.getKey() + " [connected=" + entry.getValue().isConnected() + "]");
        }
        return snapshot;
    }

    // ─── Disconnect all (on server shutdown) ─────────────────────────────────
    public void disconnectAll() {
        ChatServer.log("Disconnecting all " + clients.size() + " client(s)...");
        for (ClientHandler handler : clients.values()) {
            handler.forceClose();
        }
        clients.clear();
    }
}