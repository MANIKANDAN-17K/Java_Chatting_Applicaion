package com.chatapp.model;
import java.io.Serializable;
public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	public enum MessageType{
		JOIN,
		LEAVE,
		USER_LIST,
		CHAT_REQUEST,
		CHAT_ACCEPT,
		CHAT_DECLINE,
		MESSAGE
	}
	private MessageType type;
	private String from;
	private String to;
	private String content;
	
	public Message(MessageType type,String from, String to, String content) {
		this.type = type;
		this.from =from;
		this.to = to;
		this.content = content;
	}
	public MessageType getType() {
		return type;
	}
	public String getFrom() {
		return from;
	}
	public String getTo() {
		return to;
	}
	public String getContent() {
		return content;
	}
	@Override
	public String toString() {
		return "Message["+type+" | from = " +from+" | to "+to+"| content = " +content+"]";
	}
}
