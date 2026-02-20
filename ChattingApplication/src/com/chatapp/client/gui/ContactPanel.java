
package com.chatapp.client.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ContactPanel extends JPanel {

    private final ChatFrame chatFrame;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private String selectedUser = null;

    public ContactPanel(ChatFrame chatFrame) {
        this.chatFrame = chatFrame;
        setPreferredSize(new Dimension(220, 0));
        setBackground(new Color(24, 24, 37));
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // ── Header ───────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(17, 17, 27));
        header.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("🟢 Online Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(166, 227, 161));

        header.add(title, BorderLayout.WEST);

        // ── Global Chat Button ────────────────────────────────────────────────
        JButton globalBtn = new JButton("💬 Global Chat");
        globalBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        globalBtn.setBackground(new Color(137, 180, 250));
        globalBtn.setForeground(new Color(30, 30, 46));
        globalBtn.setFocusPainted(false);
        globalBtn.setBorderPainted(false);
        globalBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        globalBtn.setBorder(new EmptyBorder(10, 10, 10, 10));
        globalBtn.addActionListener(e -> {
            selectedUser = null;
            userList.clearSelection();
            chatFrame.setChatTarget("Global Chat");
        });

        // ── User List ─────────────────────────────────────────────────────────
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userList.setBackground(new Color(24, 24, 37));
        userList.setForeground(new Color(205, 214, 244));
        userList.setSelectionBackground(new Color(49, 50, 68));
        userList.setSelectionForeground(new Color(137, 180, 250));
        userList.setBorder(new EmptyBorder(4, 0, 4, 0));
        userList.setCellRenderer(new UserCellRenderer());
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = userList.getSelectedValue();
                if (selected != null) {
                    selectedUser = selected;
                    chatFrame.setChatTarget(selected);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(24, 24, 37));
        scrollPane.getVerticalScrollBar().setUI(new DarkScrollBarUI());

        // ── Online Count ──────────────────────────────────────────────────────
        JLabel onlineCount = new JLabel("0 online", SwingConstants.CENTER);
        onlineCount.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        onlineCount.setForeground(new Color(108, 112, 134));
        onlineCount.setBorder(new EmptyBorder(6, 0, 6, 0));

        add(header, BorderLayout.NORTH);
        add(globalBtn, BorderLayout.PAGE_START);
        add(scrollPane, BorderLayout.CENTER);
        add(onlineCount, BorderLayout.SOUTH);

        // store ref to update count
        DefaultListModel<String> model = (DefaultListModel<String>) userList.getModel();

        model.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                updateCount(onlineCount);
            }

            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                updateCount(onlineCount);
            }

            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                updateCount(onlineCount);
            }
        });
    }

    // ─── Update online user list from server ──────────────────────────────────
    public void updateUsers(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }

    public String getSelectedUser() {
        return selectedUser;
    }

    private void updateCount(JLabel label) {
        label.setText(userListModel.getSize() + " online");
    }

    // ─── Custom Cell Renderer ─────────────────────────────────────────────────
    static class UserCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            label.setText("  🟢 " + value.toString());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            label.setBackground(isSelected
                    ? new Color(49, 50, 68)
                    : new Color(24, 24, 37));
            label.setForeground(isSelected
                    ? new Color(137, 180, 250)
                    : new Color(205, 214, 244));
            return label;
        }
    }

    // ─── Dark Scrollbar ───────────────────────────────────────────────────────
    static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(88, 91, 112);
            this.trackColor = new Color(24, 24, 37);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
    }
}