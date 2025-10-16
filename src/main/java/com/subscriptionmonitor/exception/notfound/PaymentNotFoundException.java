package com.subscriptionmonitor.exception.notfound;

import com.subscriptionmonitor.exception.base.EntityNotFoundException;

import java.util.UUID;

public class PaymentNotFoundException extends EntityNotFoundException {

    private static final String ERROR_CODE = "PAYMENT_NOT_FOUND";

    public PaymentNotFoundException(UUID id) {
        super(String.format("Payment with id %s not found", id), ERROR_CODE, id);
    }

    public PaymentNotFoundException(String message, Object... args) {
        super(message, ERROR_CODE, args);
    }
}
