package com.subscriptionmonitor.gui.panel;

import com.subscriptionmonitor.dto.PaymentDto;
import com.subscriptionmonitor.dto.SubscriptionDto;
import com.subscriptionmonitor.gui.model.SubscriptionTableModel;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionPanel extends JPanel {
    private RestClient restClient;
    private com.subscriptionmonitor.gui.SubscriptionMonitorGUI mainGui;
    private SubscriptionTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JProgressBar progressBar;

    public SubscriptionPanel(RestClient restClient, com.subscriptionmonitor.gui.SubscriptionMonitorGUI mainGui) {
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

        JLabel titleLabel = new JLabel("Управление подписками");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(StyleUtils.TEXT_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));
        topPanel.add(progressBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new SubscriptionTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        StyleUtils.styleTable(table);

        table.removeColumn(table.getColumnModel().getColumn(4));
        table.removeColumn(table.getColumnModel().getColumn(3));
        table.removeColumn(table.getColumnModel().getColumn(2));
        table.removeColumn(table.getColumnModel().getColumn(0));

        if (!restClient.isAdmin()) {
            table.removeColumn(table.getColumnModel().getColumn(6));
        }

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);

        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);

        if (restClient.isAdmin()) {
            table.getColumnModel().getColumn(6).setPreferredWidth(100);
            table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(7).setPreferredWidth(150);
            table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        } else {
            table.getColumnModel().getColumn(6).setPreferredWidth(150);
            table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        }

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 9) {
                int row = e.getFirstRow();
                onActiveStatusChanged(row);
            }
        });

        JScrollPane scrollPane = StyleUtils.createStyledScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));

        addButton = StyleUtils.createPrimaryButton("Добавить");
        addButton.addActionListener(e -> addSubscription());
        buttonPanel.add(addButton);

        editButton = StyleUtils.createSecondaryButton("Редактировать");
        editButton.addActionListener(e -> editSubscription());
        editButton.setEnabled(false);
        buttonPanel.add(editButton);

        deleteButton = StyleUtils.createDangerButton("Удалить");
        deleteButton.addActionListener(e -> deleteSubscription());
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
    }

    public void loadData() {
        progressBar.setVisible(true);
        setButtonsEnabled(false);

        SwingWorker<List<SubscriptionTableModel.SubscriptionWithPayment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SubscriptionTableModel.SubscriptionWithPayment> doInBackground() throws Exception {
                List<SubscriptionDto> subscriptions = restClient.getAllSubscriptions();
                List<SubscriptionTableModel.SubscriptionWithPayment> result = new ArrayList<>();

                for (SubscriptionDto subscription : subscriptions) {
                    PaymentDto payment = null;
                    if (subscription.getPaymentId() != null) {
                        try {
                            payment = restClient.getPaymentById(subscription.getPaymentId());
                        } catch (Exception ex) {
                            payment = null;
                        }
                    }

                    String username = null;
                    if (subscription.getUserId() != null) {
                        try {
                            com.subscriptionmonitor.dto.UserDto user = restClient.getUserById(subscription.getUserId());
                            username = user.getUsername();
                        } catch (Exception ex) {
                            username = "Н/Д";
                        }
                    }

                    result.add(new SubscriptionTableModel.SubscriptionWithPayment(subscription, payment, username));
                }

                return result;
            }

            @Override
            protected void done() {
                try {
                    List<SubscriptionTableModel.SubscriptionWithPayment> data = get();
                    tableModel.setData(data);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SubscriptionPanel.this,
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

    private void addSubscription() {
        try {
            List<com.subscriptionmonitor.dto.CategoryDto> categories = restClient.getAllCategories();
            if (categories.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Сначала создайте хотя бы одну категорию",
                        "Нет категорий",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            com.subscriptionmonitor.gui.dialog.SubscriptionDialog dialog =
                    new com.subscriptionmonitor.gui.dialog.SubscriptionDialog(
                            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                            restClient,
                            categories,
                            null
                    );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                mainGui.refreshSubscriptionRelatedData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSubscription() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        try {
            SubscriptionDto subscription = tableModel.getSubscriptionAt(selectedRow);
            List<com.subscriptionmonitor.dto.CategoryDto> categories = restClient.getAllCategories();

            com.subscriptionmonitor.gui.dialog.SubscriptionDialog dialog =
                    new com.subscriptionmonitor.gui.dialog.SubscriptionDialog(
                            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                            restClient,
                            categories,
                            subscription
                    );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                mainGui.refreshSubscriptionRelatedData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSubscription() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        SubscriptionDto subscription = tableModel.getSubscriptionAt(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить подписку \"" + subscription.getName() + "\"?",
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
                restClient.deleteSubscription(subscription.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(SubscriptionPanel.this,
                            "Подписка успешно удалена",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                    mainGui.refreshSubscriptionRelatedData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SubscriptionPanel.this,
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
        addButton.setEnabled(enabled);
        editButton.setEnabled(enabled && table.getSelectedRow() != -1);
        deleteButton.setEnabled(enabled && table.getSelectedRow() != -1);
    }

    private void onActiveStatusChanged(int row) {
        SubscriptionDto subscription = tableModel.getSubscriptionAt(row);
        if (subscription == null) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                restClient.updateSubscription(subscription.getId(), subscription);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    mainGui.refreshSubscriptionRelatedData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SubscriptionPanel.this,
                            "Ошибка обновления статуса: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    loadData();
                }
            }
        };

        worker.execute();
    }
}
