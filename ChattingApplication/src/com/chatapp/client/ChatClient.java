package com.chatapp.client;

import com.chatapp.client.gui.ChatFrame;
import com.chatapp.client.gui.LoginFrame;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ChatClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9090;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private ChatFrame chatFrame;

    public ChatClient() {}

    // ─── Connect to Server ───────────────────────────────────────────────────
    public boolean connect(String username) {
        try {
            this.username = username;
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Send username to server on connect
            writer.println(username);

            System.out.println("[CLIENT] Connected to server as: " + username);
            return true;

        } catch (IOException e) {
            System.err.println("[CLIENT] Connection failed: " + e.getMessage());
            return false;
        }
    }

    // ─── Send Message ────────────────────────────────────────────────────────
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    // ─── Send Private Message ────────────────────────────────────────────────
    public void sendPrivateMessage(String toUser, String message) {
        if (writer != null) {
            writer.println("/pm " + toUser + " " + message);
        }
    }

    // ─── Start Listening for Incoming Messages ───────────────────────────────
    public void startListening(ChatFrame frame) {
        this.chatFrame = frame;

        Thread listenerThread = new Thread(() -> {
            try {
                String incoming;
                while ((incoming = reader.readLine()) != null) {
                    final String msg = incoming;
                    SwingUtilities.invokeLater(() -> chatFrame.receiveMessage(msg));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        chatFrame.receiveMessage("[SYSTEM] Disconnected from server."));
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    // ─── Disconnect ──────────────────────────────────────────────────────────
    public void disconnect() {
        try {
            if (writer != null) writer.println("/quit");
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("[CLIENT] Disconnected.");
        } catch (IOException e) {
            System.err.println("[CLIENT] Error during disconnect: " + e.getMessage());
        }
    }

    // ─── Getters ─────────────────────────────────────────────────────────────
    public String getUsername() {
        return username;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // ─── Main Entry Point ────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}