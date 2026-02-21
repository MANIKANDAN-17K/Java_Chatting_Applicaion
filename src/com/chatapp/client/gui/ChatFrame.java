package com.chatapp.client.gui;

import com.chatapp.client.ChatClient;
import com.chatapp.model.Message;
import com.chatapp.model.Message.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatFrame extends JFrame {

    private ChatClient client;
    private String username;
    private String withUser;

    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;

    // dark theme colors
    private Color bgColor       = new Color(30, 30, 30);
    private Color panelColor    = new Color(45, 45, 45);
    private Color textColor     = new Color(220, 220, 220);
    private Color accentColor   = new Color(88, 166, 255);
    private Color sentColor     = new Color(0, 132, 255);
    private Color receivedColor = new Color(55, 55, 55);

    public ChatFrame(ChatClient client, String username, String withUser) {
        this.client   = client;
        this.username = username;
        this.withUser = withUser;

        setTitle("Chat with " + withUser);
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgColor);

        // ── top header ──────────────────────────────────────
        JLabel headerLabel = new JLabel("  💬 " + withUser, SwingConstants.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(accentColor);
        headerLabel.setBackground(panelColor);
        headerLabel.setOpaque(true);
        headerLabel.setPreferredSize(new Dimension(450, 45));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // ── messages area ────────────────────────────────────
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(bgColor);

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBackground(bgColor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ── bottom input area ────────────────────────────────
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(panelColor);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        inputField = new JTextField();
        inputField.setBackground(new Color(60, 60, 60));
        inputField.setForeground(textColor);
        inputField.setCaretColor(textColor);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        sendButton = new JButton("Send");
        sendButton.setBackground(accentColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.BOLD, 13));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(80, 36));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // ── actions ──────────────────────────────────────────
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        setVisible(true);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        // show on our side
        addMessageBubble("You: " + text, true);

        // send to server
        Message msg = new Message(MessageType.MESSAGE, username, withUser, text);
        client.sendMessage(msg);

        inputField.setText("");
        scrollToBottom();
    }

    // called from DashboardFrame when message arrives
    public void receiveMessage(String content) {
        SwingUtilities.invokeLater(() -> {
            addMessageBubble(withUser + ": " + content, false);
            scrollToBottom();
        });
    }

    private void addMessageBubble(String text, boolean isSent) {
        JPanel bubbleWrapper = new JPanel(new FlowLayout(
                isSent ? FlowLayout.RIGHT : FlowLayout.LEFT));
        bubbleWrapper.setBackground(bgColor);

        JLabel bubble = new JLabel("<html><body style='width:200px; padding:6px'>" + text + "</body></html>");
        bubble.setFont(new Font("Arial", Font.PLAIN, 13));
        bubble.setForeground(Color.WHITE);
        bubble.setBackground(isSent ? sentColor : receivedColor);
        bubble.setOpaque(true);
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // rounded look using border
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isSent ? sentColor : receivedColor, 8, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        bubbleWrapper.add(bubble);
        messagesPanel.add(bubbleWrapper);
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}