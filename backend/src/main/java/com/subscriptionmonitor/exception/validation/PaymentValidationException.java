package com.subscriptionmonitor.exception.validation;

import com.subscriptionmonitor.exception.base.ValidationException;

public class PaymentValidationException extends ValidationException {

    private static final String ERROR_CODE = "PAYMENT_VALIDATION_ERROR";

    public PaymentValidationException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }

    public PaymentValidationException(String message, Throwable cause, Object... args) {
        super(message, cause, ERROR_CODE, args);
    }
}
