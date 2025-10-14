package com.subscriptionmonitor.exception.validation;

import com.subscriptionmonitor.exception.base.ValidationException;

public class SubscriptionValidationException extends ValidationException {

    private static final String ERROR_CODE = "SUBSCRIPTION_VALIDATION_ERROR";

    public SubscriptionValidationException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }

    public SubscriptionValidationException(String message, Throwable cause, Object... args) {
        super(message, cause, ERROR_CODE, args);
    }
}
