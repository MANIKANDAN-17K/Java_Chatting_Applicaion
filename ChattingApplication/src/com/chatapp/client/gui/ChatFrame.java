
package com.chatapp.client.gui;

import com.chatapp.client.ChatClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ChatFrame extends JFrame {

    private final ChatClient client;

    private ContactPanel contactPanel;
    private MessagePanel messagePanel;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel headerLabel;

    public ChatFrame(ChatClient client) {
        this.client = client;
        setTitle("ChatApp - " + client.getUsername());
        setSize(900, 620);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(700, 500));
        initUI();
        setupWindowListener();

        // Start listening for messages from server
        client.startListening(this);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 46));

        // ── Top Header ───────────────────────────────────────────────────────
        JPanel header = createHeader();

        // ── Center: Contact List + Message Panel ─────────────────────────────
        contactPanel = new ContactPanel(this);
        messagePanel = new MessagePanel(client.getUsername());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                contactPanel, messagePanel);
        splitPane.setDividerLocation(220);
        splitPane.setDividerSize(3);
        splitPane.setBorder(null);
        splitPane.setBackground(new Color(30, 30, 46));

        // ── Bottom Input Bar ─────────────────────────────────────────────────
        JPanel inputBar = createInputBar();

        add(header, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(inputBar, BorderLayout.SOUTH);
    }

    // ─── Header Panel ─────────────────────────────────────────────────────────
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 24, 37));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        headerLabel = new JLabel("💬 Global Chat");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(137, 180, 250));

        JLabel userLabel = new JLabel("👤 " + client.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(166, 227, 161));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setBackground(new Color(243, 139, 168));
        logoutBtn.setForeground(new Color(30, 30, 46));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        logoutBtn.addActionListener(e -> handleLogout());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(userLabel);
        rightPanel.add(logoutBtn);

        header.add(headerLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // ─── Input Bar ────────────────────────────────────────────────────────────
    private JPanel createInputBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(24, 24, 37));
        bar.setBorder(new EmptyBorder(12, 16, 12, 16));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBackground(new Color(49, 50, 68));
        inputField.setForeground(new Color(205, 214, 244));
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(88, 91, 112), 1),
                new EmptyBorder(10, 14, 10, 14)));
        inputField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send ➤");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setBackground(new Color(137, 180, 250));
        sendButton.setForeground(new Color(30, 30, 46));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        sendButton.addActionListener(e -> sendMessage());

        bar.add(inputField, BorderLayout.CENTER);
        bar.add(sendButton, BorderLayout.EAST);

        return bar;
    }

    // ─── Send Message Logic ───────────────────────────────────────────────────
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        String selectedUser = contactPanel.getSelectedUser();

        if (selectedUser != null && !selectedUser.equals("Global Chat")) {
            // Private message
            client.sendPrivateMessage(selectedUser, text);
            messagePanel.addMessage("[You → " + selectedUser + "] " + text, true);
        } else {
            // Global message
            client.sendMessage(text);
            messagePanel.addMessage("[You] " + text, true);
        }

        inputField.setText("");
        inputField.requestFocus();
    }

    // ─── Receive Message (called by ChatClient listener thread) ──────────────
    public void receiveMessage(String message) {
        messagePanel.addMessage(message, false);
    }

    // ─── Update Contact List ──────────────────────────────────────────────────
    public void updateOnlineUsers(String[] users) {
        contactPanel.updateUsers(users);
    }

    // ─── Set Chat Target Header ───────────────────────────────────────────────
    public void setChatTarget(String target) {
        headerLabel.setText(target.equals("Global Chat")
                ? "💬 Global Chat"
                : "🔒 Private: " + target);
        messagePanel.clearMessages();
    }

    // ─── Logout ───────────────────────────────────────────────────────────────
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            client.disconnect();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }

    // ─── Window Close Listener ────────────────────────────────────────────────
    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                handleLogout();
            }
        });
    }
}