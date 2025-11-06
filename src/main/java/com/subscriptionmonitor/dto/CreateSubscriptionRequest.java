package com.subscriptionmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание новой подписки")
public class CreateSubscriptionRequest {

    @NotBlank(message = "Subscription name is required")
    @Size(min = 1, max = 200, message = "Subscription name must be between 1 and 200 characters")
    @Schema(description = "Название подписки", example = "Netflix Premium", required = true)
    private String name;

    @Schema(description = "ID категории подписки", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID categoryId;

    @Schema(description = "ID платежной информации", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID paymentId;

    @Schema(description = "Статус активности подписки (по умолчанию true)", example = "true", defaultValue = "true")
    private Boolean isActive;
}
