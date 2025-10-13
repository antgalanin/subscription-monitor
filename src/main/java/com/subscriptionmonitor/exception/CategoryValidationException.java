package com.subscriptionmonitor.exception;

public class CategoryValidationException extends ValidationException {

    private static final String ERROR_CODE = "CATEGORY_VALIDATION_ERROR";

    public CategoryValidationException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }

    public CategoryValidationException(String message, Throwable cause, Object... args) {
        super(message, cause, ERROR_CODE, args);
    }
}
