package com.subscriptionmonitor.gui.dialog;

import com.subscriptionmonitor.dto.CategoryDto;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;
import com.subscriptionmonitor.model.enums.CategoryType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CategoryDialog extends JDialog {
    private RestClient restClient;
    private CategoryDto categoryDto;
    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<CategoryType> typeComboBox;
    private JButton saveButton;
    private JButton cancelButton;

    public CategoryDialog(Frame parent, RestClient restClient, CategoryDto category) {
        super(parent, category == null ? "Новая категория" : "Редактирование категории", true);
        this.restClient = restClient;
        this.categoryDto = category;
        initComponents();
        if (category != null) {
            loadCategoryData();
        }
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setSize(450, 280);
        setResizable(false);
        getContentPane().setBackground(StyleUtils.BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(categoryDto == null ? "Создать категорию" : "Изменить категорию", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(StyleUtils.PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel nameLabel = new JLabel("Название:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 13));
        nameField.setPreferredSize(new Dimension(250, 32));
        mainPanel.add(nameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel typeLabel = new JLabel("Тип:");
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        if (categoryDto == null) {
            if (restClient.isAdmin()) {
                typeComboBox = new JComboBox<>(new CategoryType[]{CategoryType.SYSTEM, CategoryType.CUSTOM});
            } else {
                typeComboBox = new JComboBox<>(new CategoryType[]{CategoryType.CUSTOM});
            }
        } else {
            if (restClient.isAdmin()) {
                typeComboBox = new JComboBox<>(new CategoryType[]{CategoryType.SYSTEM, CategoryType.CUSTOM, CategoryType.LEGACY});
            } else {
                typeComboBox = new JComboBox<>(new CategoryType[]{CategoryType.CUSTOM, CategoryType.LEGACY});
            }
        }
        typeComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        typeComboBox.setPreferredSize(new Dimension(250, 32));
        mainPanel.add(typeComboBox, gbc);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));

        saveButton = StyleUtils.createPrimaryButton("Сохранить");
        saveButton.setPreferredSize(new Dimension(130, 38));
        saveButton.addActionListener(e -> handleSave());
        buttonPanel.add(saveButton);

        cancelButton = StyleUtils.createSecondaryButton("Отмена");
        cancelButton.setPreferredSize(new Dimension(130, 38));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
    }

    private void loadCategoryData() {
        nameField.setText(categoryDto.getName());
        typeComboBox.setSelectedItem(categoryDto.getType());

        boolean isOwner = categoryDto.getCreatedByUserId() != null
                && categoryDto.getCreatedByUserId().equals(restClient.getCurrentUserId());
        boolean isSystemCategory = categoryDto.getType() == CategoryType.SYSTEM;

        if (!restClient.isAdmin()) {
            if (isSystemCategory) {
                nameField.setEnabled(false);
                typeComboBox.setEnabled(false);
                saveButton.setEnabled(false);
                JOptionPane.showMessageDialog(this,
                        "Вы не можете редактировать системные категории",
                        "Предупреждение",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!isOwner) {
                nameField.setEnabled(false);
                typeComboBox.setEnabled(false);
                saveButton.setEnabled(false);
                JOptionPane.showMessageDialog(this,
                        "Вы можете редактировать только свои категории",
                        "Предупреждение",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        CategoryType type = (CategoryType) typeComboBox.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Название категории обязательно",
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (name.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Название категории не должно превышать 100 символов",
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Сохранение...");

        SwingWorker<CategoryDto, Void> worker = new SwingWorker<>() {
            @Override
            protected CategoryDto doInBackground() throws Exception {
                if (categoryDto == null) {
                    CategoryDto newCategory = new CategoryDto();
                    newCategory.setName(name);
                    newCategory.setType(type);

                    return restClient.createCategory(newCategory);
                } else {
                    categoryDto.setName(name);
                    categoryDto.setType(type);

                    return restClient.updateCategory(categoryDto.getId(), categoryDto);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    saved = true;
                    dispose();
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage();
                    if (errorMessage.contains("403") || errorMessage.contains("Forbidden")) {
                        JOptionPane.showMessageDialog(CategoryDialog.this,
                                "У вас нет прав для выполнения этой операции",
                                "Ошибка доступа",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(CategoryDialog.this,
                                "Ошибка сохранения: " + errorMessage,
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } finally {
                    saveButton.setEnabled(true);
                    saveButton.setText("Сохранить");
                }
            }
        };

        worker.execute();
    }

    public boolean isSaved() {
        return saved;
    }
}
