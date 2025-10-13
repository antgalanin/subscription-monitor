package com.subscriptionmonitor.exception;

public class NotificationValidationException extends ValidationException {

    private static final String ERROR_CODE = "NOTIFICATION_VALIDATION_ERROR";

    public NotificationValidationException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }

    public NotificationValidationException(String message, Throwable cause, Object... args) {
        super(message, cause, ERROR_CODE, args);
    }
}
