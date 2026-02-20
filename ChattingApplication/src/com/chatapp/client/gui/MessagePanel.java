
package com.chatapp.client.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessagePanel extends JPanel {

    private final String currentUser;
    private JPanel messagesContainer;
    private JScrollPane scrollPane;

    private static final Color BG_COLOR        = new Color(30, 30, 46);
    private static final Color MY_BUBBLE       = new Color(137, 180, 250);
    private static final Color OTHER_BUBBLE    = new Color(49, 50, 68);
    private static final Color SYSTEM_COLOR    = new Color(108, 112, 134);
    private static final Color MY_TEXT         = new Color(30, 30, 46);
    private static final Color OTHER_TEXT      = new Color(205, 214, 244);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public MessagePanel(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        initUI();
    }

    private void initUI() {
        // ── Messages Container (grows downward) ───────────────────────────────
        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(BG_COLOR);
        messagesContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ── Scroll Pane ───────────────────────────────────────────────────────
        scrollPane = new JScrollPane(messagesContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUI(new ContactPanel.DarkScrollBarUI());

        // ── Welcome Message ───────────────────────────────────────────────────
        addSystemMessage("Welcome to ChatApp! You are connected as " + currentUser);

        add(scrollPane, BorderLayout.CENTER);
    }

    // ─── Add Message (called from ChatFrame) ──────────────────────────────────
    public void addMessage(String rawMessage, boolean isMine) {
        SwingUtilities.invokeLater(() -> {
            // Detect system messages
            if (rawMessage.startsWith("[SYSTEM]") || rawMessage.startsWith("[SERVER]")) {
                addSystemMessage(rawMessage);
                return;
            }

            // Parse sender and content
            String sender  = parseSender(rawMessage);
            String content = parseContent(rawMessage);
            boolean isMe   = isMine || sender.equals(currentUser) || sender.equals("You");

            JPanel bubbleRow = createBubbleRow(sender, content, isMe);
            messagesContainer.add(bubbleRow);
            messagesContainer.add(Box.createVerticalStrut(6));
            messagesContainer.revalidate();
            messagesContainer.repaint();
            scrollToBottom();
        });
    }

    // ─── System message (center-aligned, gray) ────────────────────────────────
    private void addSystemMessage(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(SYSTEM_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(4, 0, 4, 0));

        messagesContainer.add(label);
        messagesContainer.add(Box.createVerticalStrut(4));
        messagesContainer.revalidate();
        scrollToBottom();
    }

    // ─── Create Bubble Row ────────────────────────────────────────────────────
    private JPanel createBubbleRow(String sender, String content, boolean isMe) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel bubble = createBubble(sender, content, isMe);

        if (isMe) {
            row.add(Box.createHorizontalGlue(), BorderLayout.WEST);
            row.add(bubble, BorderLayout.EAST);
        } else {
            row.add(bubble, BorderLayout.WEST);
            row.add(Box.createHorizontalGlue(), BorderLayout.EAST);
        }

        return row;
    }

    // ─── Create Bubble ────────────────────────────────────────────────────────
    private JPanel createBubble(String sender, String content, boolean isMe) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(isMe ? MY_BUBBLE : OTHER_BUBBLE);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        isMe ? MY_BUBBLE.darker() : OTHER_BUBBLE.brighter(), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        bubble.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));

        // Sender name (not shown for "You")
        if (!isMe) {
            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            senderLabel.setForeground(new Color(137, 180, 250));
            senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubble.add(senderLabel);
            bubble.add(Box.createVerticalStrut(3));
        }

        // Message content (wrappable)
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentArea.setForeground(isMe ? MY_TEXT : OTHER_TEXT);
        contentArea.setBackground(isMe ? MY_BUBBLE : OTHER_BUBBLE);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(null);
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Timestamp
        JLabel timeLabel = new JLabel(TIME_FORMAT.format(new Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(isMe
                ? new Color(30, 30, 46, 160)
                : new Color(108, 112, 134));
        timeLabel.setAlignmentX(isMe ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        bubble.add(contentArea);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(timeLabel);

        return bubble;
    }

    // ─── Parse Sender from Message ────────────────────────────────────────────
    private String parseSender(String raw) {
        if (raw.startsWith("[You")) return "You";
        if (raw.startsWith("[") && raw.contains("]")) {
            return raw.substring(1, raw.indexOf("]"));
        }
        return "Unknown";
    }

    // ─── Parse Content from Message ───────────────────────────────────────────
    private String parseContent(String raw) {
        int idx = raw.indexOf("] ");
        return idx >= 0 ? raw.substring(idx + 2) : raw;
    }

    // ─── Clear all messages ───────────────────────────────────────────────────
    public void clearMessages() {
        SwingUtilities.invokeLater(() -> {
            messagesContainer.removeAll();
            addSystemMessage("Chat cleared. Start a new conversation!");
            messagesContainer.revalidate();
            messagesContainer.repaint();
        });
    }

    // ─── Scroll to Bottom ─────────────────────────────────────────────────────
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}