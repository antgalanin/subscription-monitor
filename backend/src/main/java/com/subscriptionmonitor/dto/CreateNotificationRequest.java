package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание нового уведомления")
public class CreateNotificationRequest {

    @NotNull(message = "Subscription ID is required")
    @Schema(description = "ID подписки, по которой отправляется уведомление", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    private UUID subscriptionId;

    @NotNull(message = "Notification date is required")
    @Schema(description = "Дата и время отправки уведомления", example = "2025-10-17T10:00:00", required = true)
    private LocalDateTime notificationDate;

    @NotNull(message = "Notification type is required")
    @Schema(description = "Тип уведомления", example = "EMAIL", allowableValues = {"EMAIL", "SMS", "PUSH"}, required = true)
    private NotificationType type;

    @NotBlank(message = "Message is required")
    @Schema(description = "Текст уведомления", example = "Через 3 дня спишется платеж за подписку Netflix Premium на сумму 999.99 RUB", required = true)
    private String message;

    @Schema(description = "Статус отправки уведомления (по умолчанию false)", example = "false", defaultValue = "false")
    private Boolean isSent;
}
