package com.subscriptionmonitor.exception;

import java.util.UUID;

public class LegacyCategoryException extends BaseException {

    private static final String ERROR_CODE = "CATEGORY_LEGACY";

    public LegacyCategoryException(UUID categoryId) {
        super(
            String.format("Category with id %s is LEGACY and cannot be used", categoryId),
            ERROR_CODE,
            categoryId
        );
    }

    public LegacyCategoryException(String categoryName) {
        super(
            String.format("Category '%s' is LEGACY and cannot be used", categoryName),
            ERROR_CODE,
            categoryName
        );
    }
}
