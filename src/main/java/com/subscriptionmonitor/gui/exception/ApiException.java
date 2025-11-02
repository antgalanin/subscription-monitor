package com.subscriptionmonitor.gui.exception;

import com.subscriptionmonitor.dto.ErrorResponse;
import lombok.Getter;

@Getter
public class ApiException extends Exception {
    private final int statusCode;
    private final String errorCode;
    private final ErrorResponse errorResponse;

    public ApiException(int statusCode, ErrorResponse errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage() : "Unknown error");
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
        this.errorCode = errorResponse != null ? errorResponse.getCode() : "UNKNOWN_ERROR";
    }

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorResponse = null;
        this.errorCode = "UNKNOWN_ERROR";
    }

    public boolean hasErrorCode(String code) {
        return errorCode != null && errorCode.equals(code);
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }

    public String getUserFriendlyMessage() {
        if (errorResponse != null && errorResponse.getMessage() != null) {
            return errorResponse.getMessage();
        }
        return getMessage();
    }
}
