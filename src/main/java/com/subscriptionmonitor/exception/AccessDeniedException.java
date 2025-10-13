package com.subscriptionmonitor.exception;

import java.util.UUID;

public class AccessDeniedException extends BaseException {

    private static final String ERROR_CODE = "ACCESS_DENIED";

    public AccessDeniedException(UUID userId, String resource) {
        super(
            String.format("User %s does not have access to resource: %s", userId, resource),
            ERROR_CODE,
            userId, resource
        );
    }

    public AccessDeniedException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }
}
