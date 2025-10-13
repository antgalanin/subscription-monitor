package com.subscriptionmonitor.exception;

public abstract class ValidationException extends BaseException {

    public ValidationException(String message, String code, Object... args) {
        super(message, code, args);
    }

    public ValidationException(String message, Throwable cause, String code, Object... args) {
        super(message, cause, code, args);
    }
}
