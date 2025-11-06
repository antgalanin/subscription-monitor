package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление платежной информации")
public class UpdatePaymentRequest {

    @Positive(message = "Cost must be positive")
    @Schema(description = "Стоимость подписки", example = "1499.99")
    private BigDecimal cost;

    @Schema(description = "Валюта платежа", example = "USD", allowableValues = {"RUB", "USD", "EUR"})
    private Currency currency;

    @Schema(description = "Период списания в днях", example = "30")
    private Integer billingPeriodDays;

    @Schema(description = "Дата следующего списания", example = "2025-12-20")
    private LocalDate nextBillingDate;
}
