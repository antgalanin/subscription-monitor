package com.subscriptionmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Статистика подписок пользователя")
public class UserStatisticsDto {
    @Schema(description = "ID пользователя", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;

    @Schema(description = "Email пользователя", example = "john@example.com")
    private String email;

    @Schema(description = "Общее количество подписок", example = "15")
    private Long totalSubscriptions;

    @Schema(description = "Количество активных подписок", example = "12")
    private Long activeSubscriptions;

    @Schema(description = "Количество неактивных подписок", example = "3")
    private Long inactiveSubscriptions;

    @Schema(description = "Общие расходы в рублях", example = "2500.00")
    private BigDecimal totalCostRub;

    @Schema(description = "Общие расходы в долларах", example = "50.00")
    private BigDecimal totalCostUsd;

    @Schema(description = "Общие расходы в евро", example = "30.00")
    private BigDecimal totalCostEur;

    @Schema(description = "Средний период оплаты в днях", example = "30")
    private Long avgBillingPeriodDays;
}
