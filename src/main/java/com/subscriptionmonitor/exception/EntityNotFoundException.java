package com.subscriptionmonitor.exception;

public abstract class EntityNotFoundException extends BaseException {

    public EntityNotFoundException(String message, String code, Object... args) {
        super(message, code, args);
    }

    public EntityNotFoundException(String message, Throwable cause, String code, Object... args) {
        super(message, cause, code, args);
    }
}
