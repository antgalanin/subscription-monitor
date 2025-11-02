package com.subscriptionmonitor.gui.dialog;

import com.subscriptionmonitor.dto.UserDto;
import com.subscriptionmonitor.gui.exception.ApiException;
import com.subscriptionmonitor.gui.util.ErrorDialogUtils;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;
import com.subscriptionmonitor.gui.util.ValidationUtils;
import com.subscriptionmonitor.model.enums.UserRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UserDialog extends JDialog {
    private RestClient restClient;
    private UserDto userDto;
    private boolean saved = false;

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<UserRole> roleComboBox;
    private JSpinner notificationDaysSpinner;
    private JButton saveButton;
    private JButton cancelButton;

    public UserDialog(Frame parent, RestClient restClient, UserDto user) {
        super(parent, user == null ? "Новый пользователь" : "Редактирование пользователя", true);
        this.restClient = restClient;
        this.userDto = user;
        initComponents();
        if (user != null) {
            loadUserData();
        }
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setSize(500, 430);
        setResizable(false);
        getContentPane().setBackground(StyleUtils.BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(userDto == null ? "Создать пользователя" : "Изменить пользователя", SwingConstants.CENTER);
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
        JLabel usernameLabel = new JLabel("Логин:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 13));
        usernameField.setPreferredSize(new Dimension(260, 32));
        if (userDto != null) {
            usernameField.setEnabled(false);
            usernameField.setToolTipText("Логин нельзя изменить");
        }
        mainPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(260, 32));
        mainPanel.add(emailField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setBackground(Color.WHITE);
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setPreferredSize(new Dimension(260, 32));
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        if (userDto != null) {
            JLabel passwordNote = new JLabel("<html><i>(оставьте пустым)</i></html>");
            passwordNote.setFont(new Font("Arial", Font.PLAIN, 11));
            passwordNote.setForeground(StyleUtils.TEXT_SECONDARY);
            passwordPanel.add(passwordNote, BorderLayout.SOUTH);
        }
        mainPanel.add(passwordPanel, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel roleLabel = new JLabel("Роль:");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(roleLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        roleComboBox = new JComboBox<>(UserRole.values());
        roleComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        roleComboBox.setPreferredSize(new Dimension(260, 32));
        if (userDto != null && !restClient.isAdmin()) {
            roleComboBox.setEnabled(false);
            roleComboBox.setToolTipText("Роль может изменить только администратор");
        }
        mainPanel.add(roleComboBox, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel notificationLabel = new JLabel("Уведомлять за дней:");
        notificationLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(notificationLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(3, 1, 30, 1);
        notificationDaysSpinner = new JSpinner(spinnerModel);
        notificationDaysSpinner.setFont(new Font("Arial", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) notificationDaysSpinner.getEditor()).getTextField().setPreferredSize(new Dimension(260, 32));
        mainPanel.add(notificationDaysSpinner, gbc);

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

    private void loadUserData() {
        usernameField.setText(userDto.getUsername());
        emailField.setText(userDto.getEmail());
        roleComboBox.setSelectedItem(userDto.getRole());
        notificationDaysSpinner.setValue(userDto.getNotificationDays());
    }

    private void handleSave() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        UserRole role = (UserRole) roleComboBox.getSelectedItem();
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

        if (userDto == null && password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Пароль обязателен для нового пользователя",
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.isEmpty()) {
            ValidationUtils.ValidationResult passwordValidation = ValidationUtils.validatePassword(password);
            if (!passwordValidation.isValid()) {
                JOptionPane.showMessageDialog(this,
                        passwordValidation.getErrorMessage(),
                        "Ошибка валидации",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        ValidationUtils.ValidationResult notificationDaysValidation = ValidationUtils.validateNotificationDays(notificationDays);
        if (!notificationDaysValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    notificationDaysValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Сохранение...");

        SwingWorker<UserDto, Void> worker = new SwingWorker<>() {
            @Override
            protected UserDto doInBackground() throws Exception {
                if (userDto == null) {
                    UserDto newUser = new UserDto();
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    newUser.setPassword(password);
                    newUser.setRole(role);
                    newUser.setNotificationDays(notificationDays);

                    return restClient.createUser(newUser);
                } else {
                    UserDto updateDto = new UserDto();
                    updateDto.setId(userDto.getId());
                    updateDto.setUsername(userDto.getUsername());
                    updateDto.setEmail(email);
                    if (!password.isEmpty()) {
                        updateDto.setPassword(password);
                    } else {
                        updateDto.setPassword(null);
                    }
                    updateDto.setRole(role);
                    updateDto.setNotificationDays(notificationDays);
                    updateDto.setCreatedAt(userDto.getCreatedAt());

                    return restClient.updateUser(updateDto.getId(), updateDto);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    saved = true;
                    dispose();
                } catch (Exception ex) {
                    ErrorDialogUtils.showErrorWithPrefix(UserDialog.this, ex, "Ошибка сохранения", "Ошибка");
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
