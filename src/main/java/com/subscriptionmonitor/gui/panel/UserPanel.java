package com.subscriptionmonitor.gui.panel;

import com.subscriptionmonitor.dto.UserDto;
import com.subscriptionmonitor.gui.model.UserTableModel;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {
    private RestClient restClient;
    private com.subscriptionmonitor.gui.SubscriptionMonitorGUI mainGui;
    private UserTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JProgressBar progressBar;

    public UserPanel(RestClient restClient, com.subscriptionmonitor.gui.SubscriptionMonitorGUI mainGui) {
        this.restClient = restClient;
        this.mainGui = mainGui;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        String title = restClient.isAdmin() ? "Управление пользователями" : "Мой профиль";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(StyleUtils.TEXT_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));
        topPanel.add(progressBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        if (restClient.isAdmin()) {
            tableModel = new UserTableModel();
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setReorderingAllowed(false);
            StyleUtils.styleTable(table);

            table.removeColumn(table.getColumnModel().getColumn(0));

            javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);

            table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

            JScrollPane scrollPane = StyleUtils.createStyledScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
            add(scrollPane, BorderLayout.CENTER);

            table.getSelectionModel().addListSelectionListener(e -> {
                boolean hasSelection = table.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            });
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));

        if (restClient.isAdmin()) {
            addButton = StyleUtils.createPrimaryButton("Добавить");
            addButton.addActionListener(e -> addUser());
            buttonPanel.add(addButton);

            editButton = StyleUtils.createSecondaryButton("Редактировать");
            editButton.addActionListener(e -> editUser());
            editButton.setEnabled(false);
            buttonPanel.add(editButton);

            deleteButton = StyleUtils.createDangerButton("Удалить");
            deleteButton.addActionListener(e -> deleteUser());
            deleteButton.setEnabled(false);
            buttonPanel.add(deleteButton);
        } else {
            editButton = StyleUtils.createPrimaryButton("Редактировать профиль");
            editButton.addActionListener(e -> editCurrentUser());
            buttonPanel.add(editButton);
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadData() {
        if (!restClient.isAdmin()) {
            return;
        }

        progressBar.setVisible(true);
        setButtonsEnabled(false);

        SwingWorker<List<UserDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserDto> doInBackground() throws Exception {
                return restClient.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<UserDto> users = get();
                    tableModel.setData(users);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserPanel.this,
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

    private void editCurrentUser() {
        progressBar.setVisible(true);
        editButton.setEnabled(false);

        SwingWorker<UserDto, Void> worker = new SwingWorker<>() {
            @Override
            protected UserDto doInBackground() throws Exception {
                return restClient.getUserById(restClient.getCurrentUserId());
            }

            @Override
            protected void done() {
                try {
                    UserDto user = get();
                    com.subscriptionmonitor.gui.dialog.UserDialog dialog =
                            new com.subscriptionmonitor.gui.dialog.UserDialog(
                                    (java.awt.Frame) SwingUtilities.getWindowAncestor(UserPanel.this),
                                    restClient,
                                    user
                            );
                    dialog.setVisible(true);

                    if (dialog.isSaved()) {
                        JOptionPane.showMessageDialog(UserPanel.this,
                                "Профиль успешно обновлен",
                                "Успех",
                                JOptionPane.INFORMATION_MESSAGE);
                        mainGui.refreshUserRelatedData();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserPanel.this,
                            "Ошибка загрузки данных пользователя: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    editButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void addUser() {
        com.subscriptionmonitor.gui.dialog.UserDialog dialog =
                new com.subscriptionmonitor.gui.dialog.UserDialog(
                        (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                        restClient,
                        null
                );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            mainGui.refreshUserRelatedData();
        }
    }

    private void editUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        UserDto user = tableModel.getUserAt(selectedRow);

        com.subscriptionmonitor.gui.dialog.UserDialog dialog =
                new com.subscriptionmonitor.gui.dialog.UserDialog(
                        (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                        restClient,
                        user
                );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            mainGui.refreshUserRelatedData();
        }
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        UserDto user = tableModel.getUserAt(selectedRow);

        if (user.getId().equals(restClient.getCurrentUserId())) {
            JOptionPane.showMessageDialog(this,
                    "Вы не можете удалить самого себя!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить пользователя \"" + user.getUsername() + "\"?",
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
                restClient.deleteUser(user.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(UserPanel.this,
                            "Пользователь успешно удален",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                    mainGui.refreshUserRelatedData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserPanel.this,
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
        if (restClient.isAdmin()) {
            addButton.setEnabled(enabled);
            editButton.setEnabled(enabled && table.getSelectedRow() != -1);
            deleteButton.setEnabled(enabled && table.getSelectedRow() != -1);
        } else {
            editButton.setEnabled(enabled);
        }
    }
}
