package com.subscriptionmonitor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String code;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;

    public static ErrorResponse of(int status, String error, String message, String code, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .code(code)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}
