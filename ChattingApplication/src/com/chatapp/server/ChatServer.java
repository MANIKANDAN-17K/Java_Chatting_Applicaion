
package com.chatapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatServer {

    private static final int PORT = 9090;
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private ServerSocket serverSocket;
    private boolean running = false;

    // ─── Start Server ─────────────────────────────────────────────────────────
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            log("Server started on port " + PORT);
            log("Waiting for clients...");

            // Shutdown hook for graceful exit
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    log("New connection from: " + clientSocket.getInetAddress().getHostAddress());

                    // Each client gets its own handler thread
                    ClientHandler handler = new ClientHandler(clientSocket);
                    Thread clientThread = new Thread(handler);
                    clientThread.setDaemon(true);
                    clientThread.start();

                } catch (IOException e) {
                    if (running) {
                        log("Error accepting client: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            log("Failed to start server on port " + PORT + ": " + e.getMessage());
        }
    }

    // ─── Shutdown ─────────────────────────────────────────────────────────────
    public void shutdown() {
        running = false;
        log("Server shutting down...");

        // Notify all connected clients
        SessionManager.getInstance().broadcastSystemMessage("[SERVER] Server is shutting down. Goodbye!");
        SessionManager.getInstance().disconnectAll();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error closing server socket: " + e.getMessage());
        }

        log("Server stopped.");
    }

    // ─── Logger ───────────────────────────────────────────────────────────────
    public static void log(String message) {
        String time = LocalTime.now().format(TIME_FMT);
        System.out.println("[" + time + "] [SERVER] " + message);
    }

    // ─── Main Entry Point ─────────────────────────────────────────────────────
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}