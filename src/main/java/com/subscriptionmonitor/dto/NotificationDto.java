package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO уведомления о предстоящем списании")
public class NotificationDto {
    @Schema(description = "Уникальный идентификатор уведомления", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "ID пользователя-получателя уведомления", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    private UUID userId;

    @Schema(description = "ID подписки, по которой отправляется уведомление", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    private UUID subscriptionId;

    @Schema(description = "Дата и время отправки уведомления", example = "2025-10-17T10:00:00", required = true)
    private LocalDateTime notificationDate;

    @Schema(description = "Тип уведомления", example = "EMAIL", allowableValues = {"EMAIL", "SMS", "PUSH"}, required = true)
    private NotificationType type;

    @Schema(description = "Статус отправки уведомления (true - отправлено, false - ожидает отправки)", example = "false")
    private Boolean isSent;

    @Schema(description = "Текст уведомления", example = "Через 3 дня спишется платеж за подписку Netflix Premium на сумму 999.99 RUB", required = true)
    private String message;

    @Schema(description = "Дата и время создания уведомления", example = "2025-10-20T15:30:00")
    private LocalDateTime createdAt;
}
