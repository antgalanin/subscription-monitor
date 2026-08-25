package com.subscriptionmonitor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Структура ответа при ошибке")
public class ErrorResponse {

    @Schema(description = "HTTP статус код ошибки", example = "400")
    private int status;

    @Schema(description = "Название HTTP статуса", example = "Bad Request")
    private String error;

    @Schema(description = "Описание ошибки", example = "Некорректные данные")
    private String message;

    @Schema(description = "Код ошибки приложения", example = "VALIDATION_ERROR")
    private String code;

    @Schema(description = "Время возникновения ошибки", example = "2025-10-19T18:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Путь к endpoint, где произошла ошибка", example = "/api/users/123")
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
