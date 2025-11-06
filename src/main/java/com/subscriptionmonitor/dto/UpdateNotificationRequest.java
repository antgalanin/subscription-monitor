package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление уведомления")
public class UpdateNotificationRequest {

    @Schema(description = "Дата и время отправки уведомления", example = "2025-10-18T10:00:00")
    private LocalDateTime notificationDate;

    @Schema(description = "Тип уведомления", example = "SMS", allowableValues = {"EMAIL", "SMS", "PUSH"})
    private NotificationType type;

    @Schema(description = "Статус отправки уведомления", example = "true")
    private Boolean isSent;

    @Schema(description = "Текст уведомления", example = "Обновленное сообщение о списании")
    private String message;
}
