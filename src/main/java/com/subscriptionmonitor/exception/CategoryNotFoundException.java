package com.subscriptionmonitor.exception;

import java.util.UUID;

public class CategoryNotFoundException extends EntityNotFoundException {

    private static final String ERROR_CODE = "CATEGORY_NOT_FOUND";

    public CategoryNotFoundException(UUID id) {
        super(String.format("Category with id %s not found", id), ERROR_CODE, id);
    }

    public CategoryNotFoundException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }
}
