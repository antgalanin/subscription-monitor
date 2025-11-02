package com.subscriptionmonitor.gui.dialog;

import com.subscriptionmonitor.dto.CategoryDto;
import com.subscriptionmonitor.dto.PaymentDto;
import com.subscriptionmonitor.dto.SubscriptionDto;
import com.subscriptionmonitor.gui.exception.ApiException;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;
import com.subscriptionmonitor.gui.util.ValidationUtils;
import com.subscriptionmonitor.model.enums.Currency;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SubscriptionDialog extends JDialog {
    private RestClient restClient;
    private SubscriptionDto subscriptionDto;
    private List<CategoryDto> categories;
    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<CategoryItem> categoryComboBox;
    private JTextField costField;
    private JComboBox<Currency> currencyComboBox;
    private JSpinner billingPeriodSpinner;
    private JSpinner nextBillingDateSpinner;
    private JCheckBox activeCheckBox;
    private JButton saveButton;
    private JButton cancelButton;

    private JCheckBox advancedSettingsCheckBox;
    private JPanel advancedSettingsPanel;
    private JSpinner startDateSpinner;

    private boolean isUpdating = false;
    private LocalDate originalNextBillingDate;

    public SubscriptionDialog(Frame parent, RestClient restClient, List<CategoryDto> categories, SubscriptionDto subscription) {
        super(parent, subscription == null ? "Новая подписка" : "Редактирование подписки", true);
        this.restClient = restClient;
        this.categories = categories;
        this.subscriptionDto = subscription;
        initComponents();
        if (subscription != null) {
            loadSubscriptionData();
        }
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setSize(540, 520);
        setResizable(false);
        getContentPane().setBackground(StyleUtils.BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(subscriptionDto == null ? "Создать подписку" : "Изменить подписку", SwingConstants.CENTER);
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
        JLabel categoryLabel = new JLabel("Категория:");
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(categoryLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        categoryComboBox.setPreferredSize(new Dimension(250, 32));
        for (CategoryDto category : categories) {
            categoryComboBox.addItem(new CategoryItem(category));
        }
        mainPanel.add(categoryComboBox, gbc);

        JLabel paymentLabel = new JLabel("Информация о платеже:");
        paymentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        paymentLabel.setForeground(StyleUtils.TEXT_COLOR);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        mainPanel.add(paymentLabel, gbc);

        gbc.gridwidth = 1;

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel costLabel = new JLabel("Стоимость:");
        costLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(costLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel costPanel = new JPanel(new GridBagLayout());
        costPanel.setBackground(Color.WHITE);
        GridBagConstraints costGbc = new GridBagConstraints();
        costGbc.fill = GridBagConstraints.HORIZONTAL;
        costGbc.insets = new Insets(0, 0, 0, 5);

        costField = new JTextField();
        costField.setFont(new Font("Arial", Font.PLAIN, 13));
        costField.setPreferredSize(new Dimension(200, 32));
        costGbc.gridx = 0;
        costGbc.weightx = 1.0;
        costPanel.add(costField, costGbc);

        currencyComboBox = new JComboBox<>(Currency.values());
        currencyComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        currencyComboBox.setPreferredSize(new Dimension(90, 32));
        costGbc.gridx = 1;
        costGbc.weightx = 0.0;
        costGbc.insets = new Insets(0, 0, 0, 0);
        costPanel.add(currencyComboBox, costGbc);

        mainPanel.add(costPanel, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel periodLabel = new JLabel("Период (дни):");
        periodLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(periodLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        SpinnerNumberModel periodModel = new SpinnerNumberModel(30, 1, 365, 1);
        billingPeriodSpinner = new JSpinner(periodModel);
        billingPeriodSpinner.setFont(new Font("Arial", Font.PLAIN, 13));
        billingPeriodSpinner.setPreferredSize(new Dimension(250, 32));
        billingPeriodSpinner.addChangeListener(e -> onPeriodChanged());
        mainPanel.add(billingPeriodSpinner, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel nextDateLabel = new JLabel("След. платёж:");
        nextDateLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(nextDateLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        Date initialNextDate = Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel nextDateModel = new SpinnerDateModel(initialNextDate, null, null, Calendar.DAY_OF_MONTH);
        nextBillingDateSpinner = new JSpinner(nextDateModel);
        nextBillingDateSpinner.setFont(new Font("Arial", Font.PLAIN, 13));
        nextBillingDateSpinner.setPreferredSize(new Dimension(250, 32));
        JSpinner.DateEditor nextDateEditor = new JSpinner.DateEditor(nextBillingDateSpinner, "yyyy-MM-dd");
        nextBillingDateSpinner.setEditor(nextDateEditor);
        nextBillingDateSpinner.addChangeListener(e -> onNextDateChanged());
        mainPanel.add(nextBillingDateSpinner, gbc);

        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel activeLabel = new JLabel("Активна:");
        activeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(activeLabel, gbc);

        gbc.gridx = 1;
        activeCheckBox = new JCheckBox();
        activeCheckBox.setSelected(true);
        activeCheckBox.setBackground(Color.WHITE);
        mainPanel.add(activeCheckBox, gbc);

        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        advancedSettingsCheckBox = new JCheckBox("Дополнительные настройки");
        advancedSettingsCheckBox.setFont(new Font("Arial", Font.PLAIN, 13));
        advancedSettingsCheckBox.setBackground(Color.WHITE);
        advancedSettingsCheckBox.addItemListener(e -> {
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            advancedSettingsPanel.setVisible(selected);
            int width = getWidth();
            int newHeight = selected ? 620 : 520;
            setSize(width, newHeight);
        });
        mainPanel.add(advancedSettingsCheckBox, gbc);

        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        advancedSettingsPanel = createAdvancedSettingsPanel();
        advancedSettingsPanel.setVisible(false);
        mainPanel.add(advancedSettingsPanel, gbc);

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

    private JPanel createAdvancedSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel startLabel = new JLabel("Дата начала:");
        startLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(startLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        Date initialStartDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel startDateModel = new SpinnerDateModel(initialStartDate, null, null, Calendar.DAY_OF_MONTH);
        startDateSpinner = new JSpinner(startDateModel);
        startDateSpinner.setFont(new Font("Arial", Font.PLAIN, 13));
        startDateSpinner.setPreferredSize(new Dimension(250, 32));
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.addChangeListener(e -> onStartDateChanged());
        panel.add(startDateSpinner, gbc);

        return panel;
    }

    private void onPeriodChanged() {
        if (isUpdating) return;
        isUpdating = true;

        try {
            Integer period = (Integer) billingPeriodSpinner.getValue();
            LocalDate startDate = getStartDate();
            LocalDate newNextDate = startDate.plusDays(period);

            Date nextDate = Date.from(newNextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            nextBillingDateSpinner.setValue(nextDate);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка расчета даты: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            isUpdating = false;
        }
    }

    private void onNextDateChanged() {
        if (isUpdating) return;
        isUpdating = true;

        try {
            LocalDate startDate = getStartDate();
            LocalDate nextDate = getNextBillingDate();

            long daysBetween = ChronoUnit.DAYS.between(startDate, nextDate);

            if (daysBetween >= 1 && daysBetween <= 365) {
                billingPeriodSpinner.setValue((int) daysBetween);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка расчета периода: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            isUpdating = false;
        }
    }

    private void onStartDateChanged() {
        if (isUpdating) return;
        isUpdating = true;

        try {
            Integer period = (Integer) billingPeriodSpinner.getValue();
            LocalDate startDate = getStartDate();
            LocalDate newNextDate = startDate.plusDays(period);

            Date nextDate = Date.from(newNextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            nextBillingDateSpinner.setValue(nextDate);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка расчета даты следующего платежа: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            isUpdating = false;
        }
    }

    private LocalDate getStartDate() {
        if (advancedSettingsCheckBox.isSelected() && startDateSpinner != null) {
            Date date = (Date) startDateSpinner.getValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return LocalDate.now();
    }

    private LocalDate getNextBillingDate() {
        Date date = (Date) nextBillingDateSpinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void loadSubscriptionData() {
        try {
            nameField.setText(subscriptionDto.getName());
            activeCheckBox.setSelected(subscriptionDto.getIsActive());

            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                CategoryItem item = categoryComboBox.getItemAt(i);
                if (item.getCategory().getId().equals(subscriptionDto.getCategoryId())) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }

            PaymentDto payment = restClient.getPaymentById(subscriptionDto.getPaymentId());
            costField.setText(payment.getCost().toString());
            currencyComboBox.setSelectedItem(payment.getCurrency());
            billingPeriodSpinner.setValue(payment.getBillingPeriodDays());

            this.originalNextBillingDate = payment.getNextBillingDate();
            Date nextBillingDate = Date.from(payment.getNextBillingDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            nextBillingDateSpinner.setValue(nextBillingDate);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки данных подписки: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        CategoryItem selectedCategory = (CategoryItem) categoryComboBox.getSelectedItem();
        String costStr = costField.getText().trim();
        Currency currency = (Currency) currencyComboBox.getSelectedItem();
        Integer billingPeriod = (Integer) billingPeriodSpinner.getValue();
        Date nextBillingDateValue = (Date) nextBillingDateSpinner.getValue();
        Boolean isActive = activeCheckBox.isSelected();

        ValidationUtils.ValidationResult nameValidation = ValidationUtils.validateName(name);
        if (!nameValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    nameValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult categoryValidation = ValidationUtils.validateNotNull(selectedCategory, "Категория");
        if (!categoryValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    categoryValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ValidationUtils.ValidationResult costValidation = ValidationUtils.validateCost(costStr);
        if (!costValidation.isValid()) {
            JOptionPane.showMessageDialog(this,
                    costValidation.getErrorMessage(),
                    "Ошибка валидации",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal cost = new BigDecimal(costStr);

        LocalDate nextBillingDate = nextBillingDateValue.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        saveButton.setEnabled(false);
        saveButton.setText("Сохранение...");

        SwingWorker<SubscriptionDto, Void> worker = new SwingWorker<>() {
            @Override
            protected SubscriptionDto doInBackground() throws Exception {
                if (subscriptionDto == null) {
                    PaymentDto paymentDto = new PaymentDto();
                    paymentDto.setCost(cost);
                    paymentDto.setCurrency(currency);
                    paymentDto.setBillingPeriodDays(billingPeriod);
                    paymentDto.setNextBillingDate(nextBillingDate);

                    PaymentDto createdPayment = restClient.post("/api/payments", paymentDto, PaymentDto.class);

                    SubscriptionDto newSubscription = new SubscriptionDto();
                    newSubscription.setName(name);
                    newSubscription.setCategoryId(selectedCategory.getCategory().getId());
                    newSubscription.setPaymentId(createdPayment.getId());
                    newSubscription.setIsActive(isActive);

                    return restClient.createSubscription(newSubscription);
                } else {
                    com.subscriptionmonitor.dto.UpdateSubscriptionRequest request =
                            new com.subscriptionmonitor.dto.UpdateSubscriptionRequest();
                    request.setName(name);
                    request.setCategoryId(selectedCategory.getCategory().getId());
                    request.setIsActive(isActive);
                    request.setCost(cost);
                    request.setCurrency(currency);
                    request.setBillingPeriodDays(billingPeriod);
                    request.setNextBillingDate(nextBillingDate);
                    request.setOldNextBillingDate(originalNextBillingDate);

                    return restClient.updateSubscriptionWithPayment(subscriptionDto.getId(), request);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    saved = true;
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String message;
                    if (cause instanceof ApiException) {
                        ApiException apiEx = (ApiException) cause;
                        message = apiEx.getUserFriendlyMessage();
                    } else {
                        message = ex.getMessage();
                    }
                    JOptionPane.showMessageDialog(SubscriptionDialog.this,
                            "Ошибка сохранения: " + message,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
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

    private static class CategoryItem {
        private final CategoryDto category;

        public CategoryItem(CategoryDto category) {
            this.category = category;
        }

        public CategoryDto getCategory() {
            return category;
        }

        @Override
        public String toString() {
            return category.getName();
        }
    }
}
