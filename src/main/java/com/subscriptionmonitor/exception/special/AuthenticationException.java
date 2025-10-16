package com.subscriptionmonitor.exception.special;

import com.subscriptionmonitor.exception.base.BaseException;

public class AuthenticationException extends BaseException {

    private static final String ERROR_CODE = "AUTHENTICATION_FAILED";

    public AuthenticationException(String username) {
        super(
            String.format("Authentication failed for user: %s", username),
            ERROR_CODE,
            username
        );
    }

    public AuthenticationException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }
}