package com.subscriptionmonitor.gui.panel;

import com.subscriptionmonitor.dto.UserDto;
import com.subscriptionmonitor.gui.exception.ApiException;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;
import com.subscriptionmonitor.gui.util.ValidationUtils;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {
    private RestClient restClient;
    private com.subscriptionmonitor.gui.SubscriptionMonitorGUI mainGui;
    private JProgressBar progressBar;

    private JLabel usernameLabel;
    private JTextField emailField;
    private JLabel roleLabel;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ProfilePanel(RestClient restClient, com.subscriptionmonitor.gui.SubscriptionMonitorGUI mainGui) {
        this.restClient = restClient;
        this.mainGui = mainGui;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel titleLabel = new JLabel("Профиль");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(StyleUtils.TEXT_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));
        topPanel.add(progressBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        JPanel profileInfoPanel = createProfileInfoPanel();
        centerPanel.add(profileInfoPanel);

        centerPanel.add(Box.createVerticalStrut(20));

        JPanel changePasswordPanel = createChangePasswordPanel();
        centerPanel.add(changePasswordPanel);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Color.WHITE);
        wrapperPanel.add(centerPanel, BorderLayout.NORTH);

        add(wrapperPanel, BorderLayout.CENTER);
    }

    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 0, 15),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(StyleUtils.BORDER_COLOR, 1),
                                "Информация профиля",
                                javax.swing.border.TitledBorder.LEFT,
                                javax.swing.border.TitledBorder.TOP,
                                new Font("Arial", Font.BOLD, 14),
                                StyleUtils.TEXT_COLOR
                        ),
                        BorderFactory.createEmptyBorder(10, 15, 15, 15)
                )
        ));

        JPanel usernamePanel = new JPanel(new BorderLayout(10, 0));
        usernamePanel.setBackground(Color.WHITE);
        JLabel usernameTitle = new JLabel("Логин:");
        usernameTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        usernameTitle.setForeground(StyleUtils.TEXT_SECONDARY);
        usernameTitle.setPreferredSize(new Dimension(150, 30));
        usernameLabel = new JLabel();
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        usernameLabel.setForeground(StyleUtils.TEXT_COLOR);
        usernamePanel.add(usernameTitle, BorderLayout.WEST);
        usernamePanel.add(usernameLabel, BorderLayout.CENTER);
        panel.add(usernamePanel);

        panel.add(Box.createVerticalStrut(10));

        JPanel emailPanel = new JPanel(new BorderLayout(10, 0));
        emailPanel.setBackground(Color.WHITE);
        JLabel emailTitle = new JLabel("Email:");
        emailTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        emailTitle.setForeground(StyleUtils.TEXT_SECONDARY);
        emailTitle.setPreferredSize(new Dimension(150, 30));
        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(300, 30));
        emailField.setMaximumSize(new Dimension(300, 30));
        emailPanel.add(emailTitle, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);
        panel.add(emailPanel);

        panel.add(Box.createVerticalStrut(10));

        JPanel rolePanel = new JPanel(new BorderLayout(10, 0));
        rolePanel.setBackground(Color.WHITE);
        JLabel roleTitle = new JLabel("Роль:");
        roleTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        roleTitle.setForeground(StyleUtils.TEXT_SECONDARY);
        roleTitle.setPreferredSize(new Dimension(150, 30));
        roleLabel = new JLabel();
        roleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        roleLabel.setForeground(StyleUtils.PRIMARY_COLOR);
        rolePanel.add(roleTitle, BorderLayout.WEST);
        rolePanel.add(roleLabel, BorderLayout.CENTER);
        panel.add(rolePanel);

        panel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton saveEmailButton = StyleUtils.createPrimaryButton("Сохранить email");
        saveEmailButton.setPreferredSize(new Dimension(150, 35));
        saveEmailButton.addActionListener(e -> updateEmail());
        buttonPanel.add(saveEmailButton);
        panel.add(buttonPanel);

        panel.setMaximumSize(new Dimension(600, 200));

        return panel;
    }

    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 0, 15),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(StyleUtils.BORDER_COLOR, 1),
                                "Смена пароля",
                                javax.swing.border.TitledBorder.LEFT,
                                javax.swing.border.TitledBorder.TOP,
                                new Font("Arial", Font.BOLD, 14),
                                StyleUtils.TEXT_COLOR
                        ),
                        BorderFactory.createEmptyBorder(10, 15, 15, 15)
                )
        ));

        JPanel currentPasswordPanel = new JPanel(new BorderLayout(10, 0));
        currentPasswordPanel.setBackground(Color.WHITE);
        JLabel currentPasswordTitle = new JLabel("Текущий пароль:");
        currentPasswordTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        currentPasswordTitle.setForeground(StyleUtils.TEXT_SECONDARY);
        currentPasswordTitle.setPreferredSize(new Dimension(150, 30));
        currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        currentPasswordField.setPreferredSize(new Dimension(300, 30));
        currentPasswordField.setMaximumSize(new Dimension(300, 30));
        currentPasswordPanel.add(currentPasswordTitle, BorderLayout.WEST);
        currentPasswordPanel.add(currentPasswordField, BorderLayout.CENTER);
        panel.add(currentPasswordPanel);

        panel.add(Box.createVerticalStrut(10));

        JPanel newPasswordPanel = new JPanel(new BorderLayout(10, 0));
        newPasswordPanel.setBackground(Color.WHITE);
        JLabel newPasswordTitle = new JLabel("Новый пароль:");
        newPasswordTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        newPasswordTitle.setForeground(StyleUtils.TEXT_SECONDARY);
        newPasswordTitle.setPreferredSize(new Dimension(150, 30));
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        newPasswordField.setPreferredSize(new Dimension(300, 30));
        newPasswordField.setMaximumSize(new Dimension(300, 30));
        newPasswordPanel.add(newPasswordTitle, BorderLayout.WEST);
        newPasswordPanel.add(newPasswordField, BorderLayout.CENTER);
        panel.add(newPasswordPanel);

        panel.add(Box.createVerticalStrut(10));

        JPanel confirmPasswordPanel = new JPanel(new BorderLayout(10, 0));
        confirmPasswordPanel.setBackground(Color.WHITE);
        JLabel confirmPasswordTitle = new JLabel("Подтвердите пароль:");
        confirmPasswordTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        confirmPasswordTitle.setForeground(StyleUtils.TEXT_SECONDARY);
        confirmPasswordTitle.setPreferredSize(new Dimension(150, 30));
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        confirmPasswordField.setPreferredSize(new Dimension(300, 30));
        confirmPasswordField.setMaximumSize(new Dimension(300, 30));
        confirmPasswordPanel.add(confirmPasswordTitle, BorderLayout.WEST);
        confirmPasswordPanel.add(confirmPasswordField, BorderLayout.CENTER);
        panel.add(confirmPasswordPanel);

        panel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton changePasswordButton = StyleUtils.createSuccessButton("Изменить пароль");
        changePasswordButton.setPreferredSize(new Dimension(150, 35));
        changePasswordButton.addActionListener(e -> changePassword());
        buttonPanel.add(changePasswordButton);
        panel.add(buttonPanel);

        panel.setMaximumSize(new Dimension(600, 220));

        return panel;
    }

    public void loadData() {
        progressBar.setVisible(true);

        SwingWorker<UserDto, Void> worker = new SwingWorker<>() {
            @Override
            protected UserDto doInBackground() throws Exception {
                return restClient.getUserById(restClient.getCurrentUserId());
            }

            @Override
            protected void done() {
                try {
                    UserDto user = get();
                    usernameLabel.setText(user.getUsername());
                    emailField.setText(user.getEmail());
                    roleLabel.setText(user.getRole().name());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ProfilePanel.this,
                            "Ошибка загрузки данных профиля: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    private void updateEmail() {
        String newEmail = emailField.getText().trim();

        ValidationUtils.ValidationResult emailValidation = ValidationUtils.validateEmail(newEmail);
        if (!emailValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    emailValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        progressBar.setVisible(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                restClient.updateUserEmail(restClient.getCurrentUserId(), newEmail);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(ProfilePanel.this,
                            "Email успешно обновлён",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                    mainGui.refreshUserRelatedData();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String message;
                    if (cause instanceof ApiException) {
                        ApiException apiEx = (ApiException) cause;
                        message = apiEx.getUserFriendlyMessage();
                    } else {
                        message = ex.getMessage();
                    }
                    JOptionPane.showMessageDialog(ProfilePanel.this,
                            "Ошибка обновления email: " + message,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        ValidationUtils.ValidationResult currentPasswordValidation = ValidationUtils.validatePassword(currentPassword);
        if (!currentPasswordValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    "Текущий пароль: " + currentPasswordValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult newPasswordValidation = ValidationUtils.validatePassword(newPassword);
        if (!newPasswordValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    "Новый пароль: " + newPasswordValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult passwordsMatchValidation = ValidationUtils.validatePasswordsMatch(newPassword, confirmPassword);
        if (!passwordsMatchValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    passwordsMatchValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        progressBar.setVisible(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                restClient.changePassword(restClient.getCurrentUserId(), currentPassword, newPassword);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(ProfilePanel.this,
                            "Пароль успешно изменён",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                    currentPasswordField.setText("");
                    newPasswordField.setText("");
                    confirmPasswordField.setText("");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String message;
                    if (cause instanceof ApiException) {
                        ApiException apiEx = (ApiException) cause;
                        message = apiEx.getUserFriendlyMessage();
                    } else {
                        message = ex.getMessage();
                    }
                    JOptionPane.showMessageDialog(ProfilePanel.this,
                            "Ошибка смены пароля: " + message,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                }
            }
        };

        worker.execute();
    }
}
