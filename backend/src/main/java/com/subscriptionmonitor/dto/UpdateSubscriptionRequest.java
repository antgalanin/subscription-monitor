package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление данных подписки")
public class UpdateSubscriptionRequest {
    @Schema(description = "Новое название подписки", example = "Spotify Premium")
    private String name;

    @Schema(description = "ID новой категории подписки", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID categoryId;

    @Schema(description = "Новый статус активности подписки", example = "true")
    private Boolean isActive;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be greater than or equal to 0")
    @Schema(description = "Новая стоимость подписки", example = "499.00")
    private BigDecimal cost;

    @Schema(description = "Новая валюта платежа", example = "RUB", allowableValues = {"RUB", "USD", "EUR"})
    private Currency currency;

    @Schema(description = "Новый период оплаты в днях", example = "30")
    private Integer billingPeriodDays;

    @Schema(description = "Новая дата следующего списания", example = "2025-12-01")
    private LocalDate nextBillingDate;

    @Schema(description = "Старая дата следующего списания (для отслеживания изменений)", example = "2025-11-15")
    private LocalDate oldNextBillingDate;
}
