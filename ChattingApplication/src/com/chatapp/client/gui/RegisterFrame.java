package com.chatapp.client.gui;

import com.chatapp.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backToLoginButton;
    private JLabel statusLabel;

    private final AuthService authService = new AuthService();

    public RegisterFrame() {
        setTitle("ChatApp - Register");
        setSize(420, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 46));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("📝 Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(166, 227, 161));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel formPanel = new JPanel(new GridLayout(5, 1, 0, 12));
        formPanel.setOpaque(false);

        usernameField = createStyledTextField("Username");
        passwordField = createStyledPasswordField("Password");
        confirmPasswordField = createStyledPasswordField("Confirm Password");

        registerButton = createStyledButton("Create Account", new Color(166, 227, 161));
        backToLoginButton = createStyledButton("Back to Login", new Color(137, 180, 250));

        registerButton.addActionListener(e -> handleRegister());
        backToLoginButton.addActionListener(e -> goBackToLogin());

        formPanel.add(usernameField);
        formPanel.add(passwordField);
        formPanel.add(confirmPasswordField);
        formPanel.add(registerButton);
        formPanel.add(backToLoginButton);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(243, 139, 168));
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
        getRootPane().setDefaultButton(registerButton);
    }

    // ─────────────────────────────────────────────────────────────
    // HANDLE REGISTER (FIXED)
    // ─────────────────────────────────────────────────────────────
    private void handleRegister() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirm  = new String(confirmPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()
                || username.equals("Username")
                || password.equals("Password")) {
            setStatus("Please fill in all fields.", false);
            return;
        }

        if (username.length() < 3) {
            setStatus("Username must be at least 3 characters.", false);
            return;
        }

        if (password.length() < 6) {
            setStatus("Password must be at least 6 characters.", false);
            return;
        }

        if (!password.equals(confirm)) {
            setStatus("Passwords do not match.", false);
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        SwingWorker<AuthService.AuthResult, Void> worker = new SwingWorker<>() {

            @Override
            protected AuthService.AuthResult doInBackground() {
                return authService.register(username, password);
            }

            @Override
            protected void done() {
                try {

                    AuthService.AuthResult result = get();

                    if (result.isSuccess()) {

                        setStatus(result.getMessage(), true);

                        Timer timer = new Timer(1500, e -> goBackToLogin());
                        timer.setRepeats(false);
                        timer.start();

                    } else {
                        setStatus(result.getMessage(), false);
                    }

                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage(), false);
                } finally {
                    registerButton.setEnabled(true);
                    registerButton.setText("Create Account");
                }
            }
        };

        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────
    private void goBackToLogin() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
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
        field.setText(placeholder);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(88, 91, 112), 1),
                new EmptyBorder(8, 12, 8, 12)));

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
            public void mouseEntered(MouseEvent e) { button.setBackground(bgColor.brighter()); }
            public void mouseExited(MouseEvent e)  { button.setBackground(bgColor); }
        });

        return button;
    }
}