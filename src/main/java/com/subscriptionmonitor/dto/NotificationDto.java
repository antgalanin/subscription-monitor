package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private UUID userId;
    private UUID subscriptionId;
    private LocalDateTime notificationDate;
    private NotificationType type;
    private Boolean isSent;
    private String message;
    private LocalDateTime createdAt;
}
