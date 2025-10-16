package com.subscriptionmonitor.exception.base;

import lombok.Getter;

@Getter
public abstract class BaseException extends Exception {

    private final String code;
    private final Object[] args;

    public BaseException(String message, String code, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public BaseException(String message, Throwable cause, String code, Object... args) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }
}
