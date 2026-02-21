package com.chatapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class ChatServer {
	private static final int PORT = 12345;
	public static void main(String[] arg) {
		System.out.println("chat Server started on port "+PORT);
		try(ServerSocket serverSocket = new ServerSocket(PORT)){
			while(true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("New client connected: "+clientSocket.getInetAddress());
				ClientHandler handler = new ClientHandler(clientSocket);
				Thread thread = new Thread(handler);
				thread.start();
			}
		}catch(IOException e) {
			System.out.println("Server error : "+ e.getMessage());
		}
	}
}
