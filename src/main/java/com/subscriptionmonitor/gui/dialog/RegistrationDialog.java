package com.subscriptionmonitor.gui.dialog;

import com.subscriptionmonitor.dto.UserDto;
import com.subscriptionmonitor.gui.exception.ApiException;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;
import com.subscriptionmonitor.gui.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegistrationDialog extends JDialog {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JSpinner notificationDaysSpinner;
    private JButton registerButton;
    private JButton cancelButton;
    private RestClient restClient;
    private boolean registered = false;

    public RegistrationDialog(Window parent, RestClient restClient) {
        super(parent, "Регистрация нового пользователя", ModalityType.APPLICATION_MODAL);
        this.restClient = restClient;
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setSize(500, 480);
        setResizable(false);
        getContentPane().setBackground(StyleUtils.BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Subscription Monitor");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(StyleUtils.PRIMARY_COLOR);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel("Регистрация нового пользователя");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(StyleUtils.TEXT_SECONDARY);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(25));

        JPanel fieldPanel = new JPanel(new GridBagLayout());
        fieldPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Логин:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        fieldPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldPanel.add(usernameField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        fieldPanel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        fieldPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldPanel.add(passwordField, gbc);

        JLabel confirmLabel = new JLabel("Подтверждение:");
        confirmLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        fieldPanel.add(confirmLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        confirmPasswordField.setPreferredSize(new Dimension(250, 35));
        confirmPasswordField.addActionListener(e -> handleRegistration());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldPanel.add(confirmPasswordField, gbc);

        JLabel notificationLabel = new JLabel("Уведомлять за дней:");
        notificationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        fieldPanel.add(notificationLabel, gbc);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(3, 1, 30, 1);
        notificationDaysSpinner = new JSpinner(spinnerModel);
        notificationDaysSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) notificationDaysSpinner.getEditor()).getTextField().setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldPanel.add(notificationDaysSpinner, gbc);

        mainPanel.add(fieldPanel);

        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        registerButton = StyleUtils.createPrimaryButton("Зарегистрироваться");
        registerButton.setPreferredSize(new Dimension(180, 40));
        registerButton.addActionListener(e -> handleRegistration());
        buttonPanel.add(registerButton);

        cancelButton = StyleUtils.createSecondaryButton("Отмена");
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);

        getRootPane().setDefaultButton(registerButton);
    }

    private void handleRegistration() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        Integer notificationDays = (Integer) notificationDaysSpinner.getValue();

        ValidationUtils.ValidationResult usernameValidation = ValidationUtils.validateUsername(username);
        if (!usernameValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    usernameValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult emailValidation = ValidationUtils.validateEmail(email);
        if (!emailValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    emailValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult passwordValidation = ValidationUtils.validatePassword(password);
        if (!passwordValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    passwordValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult passwordsMatchValidation = ValidationUtils.validatePasswordsMatch(password, confirmPassword);
        if (!passwordsMatchValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    passwordsMatchValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult notificationDaysValidation = ValidationUtils.validateNotificationDays(notificationDays);
        if (!notificationDaysValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    notificationDaysValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Регистрация...");

        SwingWorker<UserDto, Void> worker = new SwingWorker<>() {
            @Override
            protected UserDto doInBackground() throws Exception {
                UserDto userDto = new UserDto();
                userDto.setUsername(username);
                userDto.setEmail(email);
                userDto.setPassword(password);
                userDto.setNotificationDays(notificationDays);

                return restClient.createUser(userDto);
            }

            @Override
            protected void done() {
                try {
                    UserDto registeredUser = get();
                    registered = true;
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof ApiException) {
                        ApiException apiEx = (ApiException) cause;
                        String message;
                        String title;

                        if (apiEx.hasErrorCode("DUPLICATE_ENTRY")) {
                            message = "Пользователь с таким логином или email уже существует";
                            title = "Дубликат данных";
                        } else if (apiEx.hasErrorCode("VALIDATION_ERROR") || apiEx.hasErrorCode("USER_VALIDATION_ERROR")) {
                            message = apiEx.getUserFriendlyMessage();
                            title = "Ошибка валидации";
                        } else {
                            message = apiEx.getUserFriendlyMessage();
                            title = "Ошибка регистрации";
                        }

                        JOptionPane.showMessageDialog(RegistrationDialog.this,
                                message,
                                title,
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(RegistrationDialog.this,
                                "Ошибка регистрации: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    registered = false;
                } finally {
                    registerButton.setEnabled(true);
                    registerButton.setText("Зарегистрироваться");
                }
            }
        };

        worker.execute();
    }

    public boolean isRegistered() {
        return registered;
    }
}
