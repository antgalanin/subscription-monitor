package com.subscriptionmonitor.exception.notfound;

import com.subscriptionmonitor.exception.base.EntityNotFoundException;

import java.util.UUID;

public class NotificationNotFoundException extends EntityNotFoundException {

    private static final String ERROR_CODE = "NOTIFICATION_NOT_FOUND";

    public NotificationNotFoundException(UUID id) {
        super(String.format("Notification with id %s not found", id), ERROR_CODE, id);
    }

    public NotificationNotFoundException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }
}
