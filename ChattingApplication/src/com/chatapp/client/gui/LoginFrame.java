package com.chatapp.client.gui;

import com.chatapp.client.ChatClient;
import com.chatapp.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("ChatApp - Login");
        setSize(420, 340);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 46));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("💬 ChatApp", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(137, 180, 250));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 0, 12));
        formPanel.setOpaque(false);

        usernameField = createStyledTextField("Username");
        passwordField = createStyledPasswordField("Password");

        loginButton = createStyledButton("Login", new Color(137, 180, 250));
        loginButton.addActionListener(e -> handleLogin());

        registerButton = createStyledButton("Register", new Color(166, 227, 161));
        registerButton.addActionListener(e -> openRegister());

        formPanel.add(usernameField);
        formPanel.add(passwordField);
        formPanel.add(loginButton);
        formPanel.add(registerButton);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(243, 139, 168));
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);

        getRootPane().setDefaultButton(loginButton);
    }

    // ─────────────────────────────────────────────────────────────
    // HANDLE LOGIN (FIXED FOR AuthResult)
    // ─────────────────────────────────────────────────────────────
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()
                || username.equals("Username")
                || password.equals("Password")) {
            setStatus("Please fill in all fields.", false);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Connecting...");

        SwingWorker<AuthService.AuthResult, Void> worker = new SwingWorker<>() {

            @Override
            protected AuthService.AuthResult doInBackground() {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {

                    AuthService.AuthResult result = get();

                    if (result.isSuccess()) {

                        ChatClient client = new ChatClient();
                        boolean connected = client.connect(username);

                        if (connected) {
                            ChatFrame chatFrame = new ChatFrame(client);
                            chatFrame.setVisible(true);
                            dispose();
                        } else {
                            setStatus("Cannot connect to server. Is it running?", false);
                        }

                    } else {
                        setStatus(result.getMessage(), false);
                    }

                } catch (Exception ex) {
                    setStatus("Login error: " + ex.getMessage(), false);
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };

        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────
    private void openRegister() {
        RegisterFrame registerFrame = new RegisterFrame();
        registerFrame.setVisible(true);
        dispose();
    }

    private void setStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setForeground(success
                ? new Color(166, 227, 161)
                : new Color(243, 139, 168));
    }

    // ─────────────────────────────────────────────────────────────
    // STYLED COMPONENTS
    // ─────────────────────────────────────────────────────────────
    private JTextField createStyledTextField(String placeholder) {

        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(49, 50, 68));
        field.setForeground(Color.GRAY);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(88, 91, 112), 1),
                new EmptyBorder(8, 12, 8, 12)));

        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(205, 214, 244));
                }
            }

            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {

        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(49, 50, 68));
        field.setForeground(Color.GRAY);
        field.setCaretColor(Color.WHITE);
        field.setEchoChar((char) 0);
        field.setText(placeholder);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(88, 91, 112), 1),
                new EmptyBorder(8, 12, 8, 12)));

        field.addFocusListener(new FocusAdapter() {

            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('●');
                    field.setForeground(new Color(205, 214, 244));
                }
            }

            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {

        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(new Color(30, 30, 46));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}