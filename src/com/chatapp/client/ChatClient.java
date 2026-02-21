package com.chatapp.client;

import com.chatapp.client.gui.DashboardFrame;
import com.chatapp.model.Message;
import com.chatapp.model.Message.MessageType;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 12345;

    private String username;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    private DashboardFrame dashboard;

    public ChatClient(String username) {
        this.username = username;

        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out    = new ObjectOutputStream(socket.getOutputStream());
            in     = new ObjectInputStream(socket.getInputStream());

            // first thing — send JOIN message
            sendMessage(new Message(MessageType.JOIN, username, "SERVER", ""));

            // open dashboard
            dashboard = new DashboardFrame(this, username);

            // start listening for messages from server
            Thread listenerThread = new Thread(this::listenFromServer);
            listenerThread.setDaemon(true);
            listenerThread.start();

        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }

    private void listenFromServer() {
        try {
            while (true) {
                Message message = (Message) in.readObject();
                handleIncoming(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Disconnected from server.");
        }
    }

    private void handleIncoming(Message message) {
        switch (message.getType()) {

            case USER_LIST:
                dashboard.updateUserList(message.getContent());
                break;

            case CHAT_REQUEST:
                dashboard.handleChatRequest(message.getFrom());
                break;

            case CHAT_ACCEPT:
                dashboard.handleChatAccepted(message.getFrom());
                break;

            case CHAT_DECLINE:
                dashboard.handleChatDeclined(message.getFrom());
                break;

            case MESSAGE:
                dashboard.deliverMessage(message);
                break;

            default:
                break;
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message: " + e.getMessage());
        }
    }

    // entry point
    public static void main(String[] args) {
        new com.chatapp.client.gui.LoginFrame();
    }
}