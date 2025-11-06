package com.subscriptionmonitor.gui.dialog;

import com.subscriptionmonitor.gui.exception.ApiException;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;
import com.subscriptionmonitor.gui.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton exitButton;
    private RestClient restClient;
    private boolean authenticated = false;

    public LoginDialog(Frame parent, RestClient restClient) {
        super(parent, "Вход в систему", true);
        this.restClient = restClient;
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setSize(500, 350);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));

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

        JLabel subtitleLabel = new JLabel("Система мониторинга персональных подписок");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(StyleUtils.TEXT_SECONDARY);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(30));

        JPanel fieldPanel = new JPanel(new GridBagLayout());
        fieldPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
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

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        fieldPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(250, 35));
        passwordField.addActionListener(e -> handleLogin());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldPanel.add(passwordField, gbc);

        mainPanel.add(fieldPanel);

        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        loginButton = StyleUtils.createPrimaryButton("Войти");
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);

        registerButton = StyleUtils.createSuccessButton("Регистрация");
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.addActionListener(e -> showRegistrationDialog());
        buttonPanel.add(registerButton);

        exitButton = StyleUtils.createDangerButton("Отмена");
        exitButton.setPreferredSize(new Dimension(120, 40));
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Завершить работу приложения?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);

        getRootPane().setDefaultButton(loginButton);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        ValidationUtils.ValidationResult usernameValidation = ValidationUtils.validateNotEmpty(username, "Логин");
        if (!usernameValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    usernameValidation.getErrorMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult passwordValidation = ValidationUtils.validateNotEmpty(password, "Пароль");
        if (!passwordValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    passwordValidation.getErrorMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Вход...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return restClient.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    authenticated = get();
                    if (authenticated) {
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginDialog.this,
                                "Неверный логин или пароль",
                                "Ошибка авторизации",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String message;
                    if (cause instanceof ApiException) {
                        ApiException apiEx = (ApiException) cause;
                        if (apiEx.getStatusCode() == 401 || apiEx.getStatusCode() == 403) {
                            message = "Неверный логин или пароль";
                        } else {
                            message = apiEx.getUserFriendlyMessage();
                        }
                    } else {
                        message = ex.getMessage();
                    }
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Ошибка подключения: " + message,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    authenticated = false;
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Войти");
                }
            }
        };

        worker.execute();
    }

    private void showRegistrationDialog() {
        RegistrationDialog registrationDialog = new RegistrationDialog(this, restClient);
        registrationDialog.setVisible(true);

        if (registrationDialog.isRegistered()) {
            JOptionPane.showMessageDialog(this,
                    "Регистрация успешна! Теперь вы можете войти.",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
