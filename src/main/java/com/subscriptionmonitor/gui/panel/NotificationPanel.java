package com.subscriptionmonitor.gui.panel;

import com.subscriptionmonitor.dto.NotificationDto;
import com.subscriptionmonitor.gui.model.NotificationTableModel;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NotificationPanel extends JPanel {
    private RestClient restClient;
    private NotificationTableModel tableModel;
    private JTable table;
    private JButton deleteButton;
    private JProgressBar progressBar;

    public NotificationPanel(RestClient restClient) {
        this.restClient = restClient;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel titleLabel = new JLabel("Уведомления");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(StyleUtils.TEXT_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));
        topPanel.add(progressBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new NotificationTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        StyleUtils.styleTable(table);

        table.removeColumn(table.getColumnModel().getColumn(1));
        table.removeColumn(table.getColumnModel().getColumn(0));

        if (!restClient.isAdmin()) {
            table.removeColumn(table.getColumnModel().getColumn(5));
        }

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        if (restClient.isAdmin()) {
            table.getColumnModel().getColumn(5).setPreferredWidth(100);
            table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = StyleUtils.createStyledScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));

        deleteButton = StyleUtils.createDangerButton("Удалить");
        deleteButton.addActionListener(e -> deleteNotification());
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            deleteButton.setEnabled(hasSelection);
        });
    }

    public void loadData() {
        progressBar.setVisible(true);
        setButtonsEnabled(false);

        SwingWorker<List<com.subscriptionmonitor.gui.model.NotificationTableModel.NotificationWithUsername>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<com.subscriptionmonitor.gui.model.NotificationTableModel.NotificationWithUsername> doInBackground() throws Exception {
                if (restClient.isAdmin()) {
                    restClient.markPendingNotificationsAsSent();
                }

                List<NotificationDto> notifications;
                if (restClient.isAdmin()) {
                    notifications = restClient.getSentNotifications();
                } else {
                    notifications = restClient.getMyReceivedNotifications();
                }

                List<com.subscriptionmonitor.gui.model.NotificationTableModel.NotificationWithUsername> result = new java.util.ArrayList<>();

                for (NotificationDto notification : notifications) {
                    String username = null;
                    if (notification.getUserId() != null) {
                        try {
                            com.subscriptionmonitor.dto.UserDto user = restClient.getUserById(notification.getUserId());
                            username = user.getUsername();
                        } catch (Exception ex) {
                            username = "Н/Д";
                        }
                    }
                    result.add(new com.subscriptionmonitor.gui.model.NotificationTableModel.NotificationWithUsername(notification, username));
                }

                return result;
            }

            @Override
            protected void done() {
                try {
                    List<com.subscriptionmonitor.gui.model.NotificationTableModel.NotificationWithUsername> data = get();
                    tableModel.setData(data);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NotificationPanel.this,
                            "Ошибка загрузки данных: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void deleteNotification() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        NotificationDto notification = tableModel.getNotificationAt(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить уведомление?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        progressBar.setVisible(true);
        setButtonsEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                restClient.deleteNotification(notification.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    tableModel.removeNotification(selectedRow);
                    JOptionPane.showMessageDialog(NotificationPanel.this,
                            "Уведомление успешно удалено",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NotificationPanel.this,
                            "Ошибка удаления: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        deleteButton.setEnabled(enabled && table.getSelectedRow() != -1);
    }
}
