package com.subscriptionmonitor.exception.validation;

import com.subscriptionmonitor.exception.base.ValidationException;

public class UserValidationException extends ValidationException {

    private static final String ERROR_CODE = "USER_VALIDATION_ERROR";

    public UserValidationException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }

    public UserValidationException(String message, Throwable cause, Object... args) {
        super(message, cause, ERROR_CODE, args);
    }
}
