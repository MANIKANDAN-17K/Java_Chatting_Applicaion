package com.chatapp.client.gui;
import com.chatapp.client.ChatClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame{
	private JTextField usernameField;
	private JButton joinButton;
	public LoginFrame() {
		setTitle("Chat App - Login");
		setSize(400,250);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		
		Color bgColor = new Color(30,30,30);
		Color panelColor = new Color(45,45,45);
		Color textColor = new Color(220,220,220);
		Color accentColor = new Color(88,166,255);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(bgColor);
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10,10,10,10);
		
		JLabel titleLabel = new JLabel("💬 Chat App");
		titleLabel.setForeground(accentColor);
		titleLabel.setFont(new Font("Ariel",Font.BOLD,22));
		gbc.gridx = 0; 
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		mainPanel.add(titleLabel,gbc);
		
		JLabel userLabel = new JLabel("Enter your name :");
		userLabel.setForeground(textColor);
		userLabel.setFont(new Font("Ariel",Font.PLAIN,14));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		mainPanel.add(userLabel,gbc);
		
		usernameField = new JTextField(15);
		usernameField.setBackground(panelColor);
		usernameField.setForeground(textColor);
		usernameField.setCaretColor(textColor);
		usernameField.setBorder(BorderFactory.createLineBorder(accentColor));
		usernameField.setFont(new Font("Ariel",Font.PLAIN,14));
		gbc.gridx =1;
		gbc.gridy = 1;
		mainPanel.add(usernameField,gbc);
		
		joinButton = new JButton("join");
		joinButton.setBackground(accentColor);
		joinButton.setForeground(Color.WHITE);
		joinButton.setFont(new Font("Arial",Font.BOLD,14));
		joinButton.setFocusPainted(false);
		joinButton.setBorderPainted(false);
		joinButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		mainPanel.add(joinButton, gbc);
		add(mainPanel);
		
		joinButton.addActionListener(e -> handleJoin());
		usernameField.addActionListener(e -> handleJoin());
		
		setVisible(true);
		
	}
	private void handleJoin() {
		String username = usernameField.getText().trim();
		if(username.isEmpty()) {
			JOptionPane.showMessageDialog(this,"Please enter you name","Warning",JOptionPane.WARNING_MESSAGE);
			return;
		}
		dispose();
		new ChatClient(username);
		}
}
