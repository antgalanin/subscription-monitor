package com.subscriptionmonitor.exception;

import java.util.UUID;

public class SubscriptionNotFoundException extends EntityNotFoundException {

    private static final String ERROR_CODE = "SUBSCRIPTION_NOT_FOUND";

    public SubscriptionNotFoundException(UUID id) {
        super(String.format("Subscription with id %s not found", id), ERROR_CODE, id);
    }

    public SubscriptionNotFoundException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }
}
