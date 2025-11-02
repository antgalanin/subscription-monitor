package com.subscriptionmonitor.gui.panel;

import com.subscriptionmonitor.dto.CategoryDto;
import com.subscriptionmonitor.gui.model.CategoryTableModel;
import com.subscriptionmonitor.gui.util.ErrorDialogUtils;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CategoryPanel extends JPanel {
    private RestClient restClient;
    private com.subscriptionmonitor.gui.SubscriptionMonitorGUI mainGui;
    private CategoryTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JProgressBar progressBar;

    public CategoryPanel(RestClient restClient, com.subscriptionmonitor.gui.SubscriptionMonitorGUI mainGui) {
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

        JLabel titleLabel = new JLabel("Управление категориями");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(StyleUtils.TEXT_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));
        topPanel.add(progressBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new CategoryTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        StyleUtils.styleTable(table);

        table.removeColumn(table.getColumnModel().getColumn(3));
        table.removeColumn(table.getColumnModel().getColumn(0));

        if (!restClient.isAdmin()) {
            table.removeColumn(table.getColumnModel().getColumn(3));
            table.removeColumn(table.getColumnModel().getColumn(2));
        }

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);

        if (restClient.isAdmin()) {
            table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        } else {
            table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
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

        addButton = StyleUtils.createPrimaryButton("Добавить");
        addButton.addActionListener(e -> addCategory());
        buttonPanel.add(addButton);

        editButton = StyleUtils.createSecondaryButton("Редактировать");
        editButton.addActionListener(e -> editCategory());
        editButton.setEnabled(false);
        buttonPanel.add(editButton);

        deleteButton = StyleUtils.createDangerButton("Удалить");
        deleteButton.addActionListener(e -> deleteCategory());
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            boolean hasSelection = selectedRow != -1;

            if (hasSelection) {
                CategoryDto category = tableModel.getCategoryAt(selectedRow);
                boolean canModify = canModifyCategory(category);
                editButton.setEnabled(canModify);
                deleteButton.setEnabled(canModify && category.getType() != com.subscriptionmonitor.model.enums.CategoryType.LEGACY);
            } else {
                editButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
        });
    }

    public void loadData() {
        progressBar.setVisible(true);
        setButtonsEnabled(false);

        SwingWorker<java.util.List<com.subscriptionmonitor.gui.model.CategoryTableModel.CategoryWithOwner>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<com.subscriptionmonitor.gui.model.CategoryTableModel.CategoryWithOwner> doInBackground() throws Exception {
                java.util.List<CategoryDto> categories = restClient.getAllCategories();
                java.util.List<com.subscriptionmonitor.gui.model.CategoryTableModel.CategoryWithOwner> result = new java.util.ArrayList<>();

                java.util.Map<java.util.UUID, Integer> subscriptionCounts = new java.util.HashMap<>();
                if (restClient.isAdmin()) {
                    java.util.List<com.subscriptionmonitor.dto.SubscriptionDto> subscriptions = restClient.getAllSubscriptions();
                    for (com.subscriptionmonitor.dto.SubscriptionDto subscription : subscriptions) {
                        if (subscription.getCategoryId() != null) {
                            subscriptionCounts.merge(subscription.getCategoryId(), 1, Integer::sum);
                        }
                    }
                }

                for (CategoryDto category : categories) {
                    String ownerUsername = null;
                    if (restClient.isAdmin() && category.getCreatedByUserId() != null) {
                        try {
                            com.subscriptionmonitor.dto.UserDto user = restClient.getUserById(category.getCreatedByUserId());
                            ownerUsername = user.getUsername();
                        } catch (Exception ex) {
                            ownerUsername = "Н/Д";
                        }
                    }

                    int subscriptionCount = subscriptionCounts.getOrDefault(category.getId(), 0);
                    result.add(new com.subscriptionmonitor.gui.model.CategoryTableModel.CategoryWithOwner(
                            category, ownerUsername, subscriptionCount));
                }

                return result;
            }

            @Override
            protected void done() {
                try {
                    java.util.List<com.subscriptionmonitor.gui.model.CategoryTableModel.CategoryWithOwner> data = get();
                    tableModel.setData(data);
                } catch (Exception ex) {
                    ErrorDialogUtils.showErrorWithPrefix(CategoryPanel.this, ex, "Ошибка загрузки данных", "Ошибка");
                } finally {
                    progressBar.setVisible(false);
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void addCategory() {
        com.subscriptionmonitor.gui.dialog.CategoryDialog dialog =
                new com.subscriptionmonitor.gui.dialog.CategoryDialog(
                        (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                        restClient,
                        null
                );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            mainGui.refreshCategoryRelatedData();
        }
    }

    private void editCategory() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        CategoryDto category = tableModel.getCategoryAt(selectedRow);

        if (category.getType() == com.subscriptionmonitor.model.enums.CategoryType.SYSTEM && !restClient.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Системные категории могут редактировать только администраторы",
                    "Доступ запрещен",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        com.subscriptionmonitor.gui.dialog.CategoryDialog dialog =
                new com.subscriptionmonitor.gui.dialog.CategoryDialog(
                        (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                        restClient,
                        category
                );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            mainGui.refreshCategoryRelatedData();
        }
    }

    private void deleteCategory() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        CategoryDto category = tableModel.getCategoryAt(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить категорию \"" + category.getName() + "\"?",
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
                restClient.deleteCategory(category.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    ErrorDialogUtils.showSuccess(CategoryPanel.this,
                            "Категория успешно удалена",
                            "Успех");
                    mainGui.refreshCategoryRelatedData();
                } catch (Exception ex) {
                    ErrorDialogUtils.showErrorWithPrefix(CategoryPanel.this, ex, "Ошибка удаления", "Ошибка");
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
        int selectedRow = table.getSelectedRow();
        if (enabled && selectedRow != -1) {
            CategoryDto category = tableModel.getCategoryAt(selectedRow);
            boolean canModify = canModifyCategory(category);
            editButton.setEnabled(canModify);
            deleteButton.setEnabled(canModify && category.getType() != com.subscriptionmonitor.model.enums.CategoryType.LEGACY);
        } else {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private boolean canModifyCategory(CategoryDto category) {
        if (category == null) {
            return false;
        }

        if (restClient.isAdmin()) {
            return true;
        }

        boolean isSystemCategory = category.getType() == com.subscriptionmonitor.model.enums.CategoryType.SYSTEM;
        boolean isOwnCategory = category.getCreatedByUserId() != null
                && category.getCreatedByUserId().equals(restClient.getCurrentUserId());

        return !isSystemCategory && isOwnCategory;
    }
}
