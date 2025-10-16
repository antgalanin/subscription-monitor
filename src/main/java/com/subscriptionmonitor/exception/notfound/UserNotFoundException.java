package com.subscriptionmonitor.exception.notfound;

import com.subscriptionmonitor.exception.base.EntityNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends EntityNotFoundException {

    private static final String ERROR_CODE = "USER_NOT_FOUND";

    public UserNotFoundException(UUID id) {
        super(String.format("User with id %s not found", id), ERROR_CODE, id);
    }

    public UserNotFoundException(String email) {
        super(String.format("User with email %s not found", email), ERROR_CODE, email);
    }

    public UserNotFoundException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }
}
