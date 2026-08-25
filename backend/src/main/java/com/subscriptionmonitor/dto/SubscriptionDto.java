package com.subscriptionmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO подписки пользователя")
public class SubscriptionDto {
    @Schema(description = "Уникальный идентификатор подписки", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Название подписки", example = "Netflix Premium", required = true)
    private String name;

    @Schema(description = "ID пользователя-владельца подписки", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
    private UUID userId;

    @Schema(description = "ID категории подписки", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID categoryId;

    @Schema(description = "ID платежной информации", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID paymentId;

    @Schema(description = "Статус активности подписки", example = "true")
    private Boolean isActive;

    @Schema(description = "Дата и время создания подписки", example = "2025-10-20T15:30:00")
    private LocalDateTime createdAt;
}
