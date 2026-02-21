package com.chatapp.client.gui;
import com.chatapp.client.ChatClient;
import com.chatapp.model.Message;
import com.chatapp.model.Message.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
public class DashboardFrame extends JFrame {
  private ChatClient client;
    private String username;

    private DefaultListModel<String> userListModel;
    private JList<String> userList;

    // track open chat frames
    private HashMap<String, ChatFrame> openChats = new HashMap<>();

    // dark theme colors
    private Color bgColor     = new Color(30, 30, 30);
    private Color panelColor  = new Color(45, 45, 45);
    private Color textColor   = new Color(220, 220, 220);
    private Color accentColor = new Color(88, 166, 255);
    public DashboardFrame(ChatClient client, String username) {
        this.client = client;
        this.username = username;

        setTitle("Chat App - " + username);
        setSize(300, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgColor);

        // top header
        JLabel headerLabel = new JLabel("  👥 Online Users", SwingConstants.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(accentColor);
        headerLabel.setBackground(panelColor);
        headerLabel.setOpaque(true);
        headerLabel.setPreferredSize(new Dimension(300, 45));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // user list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(bgColor);
        userList.setForeground(textColor);
        userList.setFont(new Font("Arial", Font.PLAIN, 14));
        userList.setSelectionBackground(accentColor);
        userList.setSelectionForeground(Color.WHITE);
        userList.setCellRenderer(new UserCellRenderer());

        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setBackground(bgColor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // bottom label
        JLabel selfLabel = new JLabel("  Logged in as: " + username, SwingConstants.LEFT);
        selfLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        selfLabel.setForeground(Color.GRAY);
        selfLabel.setBackground(panelColor);
        selfLabel.setOpaque(true);
        selfLabel.setPreferredSize(new Dimension(300, 35));
        mainPanel.add(selfLabel, BorderLayout.SOUTH);

        add(mainPanel);

        // double click user to send chat request
        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(username)) {
                        sendChatRequest(selectedUser);
                    }
                }
            }
        });

        setVisible(true);
    }
    private void sendChatRequest(String toUser) {
    	if(openChats.containsKey(toUser)) {
    		openChats.get(toUser).toFront();
    		return;
    	}
    	Message request = new Message(MessageType.CHAT_REQUEST, username, toUser, username + " wants to chat with you!");
        client.sendMessage(request);
        JOptionPane.showMessageDialog(this, "Chat request sent to " + toUser + "!", "Request Sent",
                JOptionPane.INFORMATION_MESSAGE);
    	
    }
    public void updateUserList(String userListData) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            if (userListData == null || userListData.isEmpty()) return;
            String[] users = userListData.split(",");
            for (String user : users) {
                if (!user.equals(username)) {
                    userListModel.addElement(user);
                }
            }
        });
    }
    public void handleChatRequest(String fromUser) {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(this,
                    fromUser + " wants to chat with you!",
                    "Chat Request",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                Message accept = new Message(MessageType.CHAT_ACCEPT, username, fromUser, "accepted");
                client.sendMessage(accept);
                openChatWindow(fromUser);
            } else {
                Message decline = new Message(MessageType.CHAT_DECLINE, username, fromUser, "declined");
                client.sendMessage(decline);
            }
        });
    }
    public void handleChatAccepted(String fromUser) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, fromUser + " accepted your chat request!", "Accepted",
                    JOptionPane.INFORMATION_MESSAGE);
            openChatWindow(fromUser);
        });
    }

    // called when our chat request is declined
    public void handleChatDeclined(String fromUser) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, fromUser + " declined your chat request.", "Declined",
                    JOptionPane.WARNING_MESSAGE);
        });
    }

    public void openChatWindow(String withUser) {
        if (!openChats.containsKey(withUser)) {
            ChatFrame chatFrame = new ChatFrame(client, username, withUser);
            openChats.put(withUser, chatFrame);
        } else {
            openChats.get(withUser).toFront();
        }
    }

    // deliver incoming message to correct chat window
    public void deliverMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            String fromUser = message.getFrom();
            if (openChats.containsKey(fromUser)) {
                openChats.get(fromUser).receiveMessage(message.getContent());
            }
        });
    }

    // custom cell renderer for user list
    class UserCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText("🟢  " + value);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            if (!isSelected) {
                label.setBackground(bgColor);
                label.setForeground(textColor);
            }
            return label;
        }
    }
    
}
