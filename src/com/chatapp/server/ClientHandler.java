package com.chatapp.server;
import com.chatapp.model.Message;
import com.chatapp.model.Message.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
public class ClientHandler implements Runnable{
	public static ConcurrentHashMap<String,ClientHandler> connectedClients = new ConcurrentHashMap<>();
	private Socket socket;
	private String username;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public ClientHandler(Socket socket) {
		this.socket = socket;
	}
	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			Message firstMessage = (Message) in.readObject();
			if(firstMessage.getType() == MessageType.JOIN) {
				username = firstMessage.getFrom();
				connectedClients.put(username,this);
				System.out.println(username+ "joined");
				broadcastUserList();
			}
			while(true) {
				Message message = (Message) in.readObject();
				handleMessage(message);
			}
		}catch(IOException | ClassNotFoundException e) {
			System.out.println(username+ " disconnected");
		}finally {
			if(username != null) {
				connectedClients.remove(username);
				broadcastUserList();
			}
			try {
				socket.close();
			}catch(IOException e) {
				
			}
		}
	}
	private void handleMessage(Message message) throws IOException {
		// TODO Auto-generated method stub
		switch(message.getType()) {
		case CHAT_REQUEST:
		case CHAT_ACCEPT:
		case CHAT_DECLINE:
		case MESSAGE:
			sendToUser(message.getTo(),message);
			break;
		default:
			break;
		}
	}
	private void sendToUser(String targetUsername,Message message) throws IOException {
		ClientHandler target = connectedClients.get(targetUsername);
		if(target != null) target.sendMessage(message);
	}
	private void sendMessage(Message message) throws IOException{
		out.writeObject(message);
		out.flush();
	}
	private void broadcastUserList() {
		// TODO Auto-generated method stub
		String userList = String.join(",",connectedClients.keySet());
		Message msg = new Message(MessageType.USER_LIST,"SERVER","ALL",userList);
		for(ClientHandler client : connectedClients.values()) {
			try {
				client.sendMessage(msg);
			}catch(IOException e) {
				System.out.println("Failed to send user list to"+client.username);
			}
		}
	}
		
	
}
