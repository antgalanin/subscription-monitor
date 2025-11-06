package com.subscriptionmonitor.gui.util;

import java.math.BigDecimal;

public class ValidationUtils {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 100;

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.invalid("Email не может быть пустым");
        }

        if (!email.matches(EMAIL_REGEX)) {
            return ValidationResult.invalid("Некорректный формат email");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.invalid("Логин не может быть пустым");
        }

        if (username.length() < MIN_USERNAME_LENGTH) {
            return ValidationResult.invalid("Логин должен содержать минимум " + MIN_USERNAME_LENGTH + " символа");
        }

        if (username.length() > MAX_USERNAME_LENGTH) {
            return ValidationResult.invalid("Логин должен содержать максимум " + MAX_USERNAME_LENGTH + " символов");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.invalid("Пароль не может быть пустым");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.invalid("Пароль должен содержать минимум " + MIN_PASSWORD_LENGTH + " символов");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validatePasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return ValidationResult.invalid("Пароли не совпадают");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.invalid("Название не может быть пустым");
        }

        if (name.length() < MIN_NAME_LENGTH) {
            return ValidationResult.invalid("Название не может быть пустым");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            return ValidationResult.invalid("Название не может превышать " + MAX_NAME_LENGTH + " символов");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateCost(String costStr) {
        if (costStr == null || costStr.trim().isEmpty()) {
            return ValidationResult.invalid("Укажите стоимость");
        }

        try {
            BigDecimal cost = new BigDecimal(costStr);
            if (cost.compareTo(BigDecimal.ZERO) <= 0) {
                return ValidationResult.invalid("Стоимость должна быть положительным числом");
            }
        } catch (NumberFormatException ex) {
            return ValidationResult.invalid("Стоимость должна быть числом");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validatePositiveInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.invalid(fieldName + " не может быть пустым");
        }

        try {
            int intValue = Integer.parseInt(value);
            if (intValue <= 0) {
                return ValidationResult.invalid(fieldName + " должно быть положительным числом");
            }
        } catch (NumberFormatException ex) {
            return ValidationResult.invalid(fieldName + " должно быть целым числом");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateNotificationDays(Integer days) {
        if (days == null) {
            return ValidationResult.invalid("Укажите количество дней для уведомлений");
        }

        if (days < 1 || days > 30) {
            return ValidationResult.invalid("Количество дней должно быть от 1 до 30");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateBillingPeriod(Integer days) {
        if (days == null) {
            return ValidationResult.invalid("Укажите период оплаты");
        }

        if (days < 1 || days > 365) {
            return ValidationResult.invalid("Период оплаты должен быть от 1 до 365 дней");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.invalid(fieldName + " не может быть пустым");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateNotNull(Object value, String fieldName) {
        if (value == null) {
            return ValidationResult.invalid(fieldName + " не может быть пустым");
        }

        return ValidationResult.valid();
    }
}
