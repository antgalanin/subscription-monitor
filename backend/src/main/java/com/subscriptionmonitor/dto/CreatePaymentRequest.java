package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание новой платежной информации")
public class CreatePaymentRequest {

    @NotNull(message = "Cost is required")
    @Positive(message = "Cost must be positive")
    @Schema(description = "Стоимость подписки", example = "999.99", required = true)
    private BigDecimal cost;

    @NotNull(message = "Currency is required")
    @Schema(description = "Валюта платежа", example = "RUB", allowableValues = {"RUB", "USD", "EUR"}, required = true)
    private Currency currency;

    @Schema(description = "Период списания в днях", example = "30")
    private Integer billingPeriodDays;

    @Schema(description = "Дата следующего списания", example = "2025-11-20")
    private LocalDate nextBillingDate;
}
