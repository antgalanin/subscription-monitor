package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Статистика по категории")
public class CategoryStatisticsDto {
    @Schema(description = "ID категории", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID categoryId;

    @Schema(description = "Название категории", example = "Развлечения")
    private String categoryName;

    @Schema(description = "Тип категории", example = "SYSTEM")
    private CategoryType categoryType;

    @Schema(description = "Общее количество подписок", example = "25")
    private Long totalSubscriptions;

    @Schema(description = "Количество активных подписок", example = "20")
    private Long activeSubscriptions;

    @Schema(description = "Количество уникальных пользователей", example = "15")
    private Long uniqueUsers;

    @Schema(description = "Общая стоимость в рублях", example = "5000.00")
    private BigDecimal totalCostRub;

    @Schema(description = "Общая стоимость в долларах", example = "100.00")
    private BigDecimal totalCostUsd;

    @Schema(description = "Общая стоимость в евро", example = "80.00")
    private BigDecimal totalCostEur;

    @Schema(description = "Средняя стоимость подписки", example = "500.00")
    private BigDecimal avgCost;

    @Schema(description = "Средний период оплаты в днях", example = "30")
    private Long avgBillingDays;
}
