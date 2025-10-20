package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO платежной информации подписки")
public class PaymentDto {
    @Schema(description = "Уникальный идентификатор платежа", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Стоимость подписки", example = "999.99", required = true)
    private BigDecimal cost;

    @Schema(description = "Валюта платежа", example = "RUB", allowableValues = {"RUB", "USD", "EUR"}, required = true)
    private Currency currency;

    @Schema(description = "Период списания в днях", example = "30")
    private Integer billingPeriodDays;

    @Schema(description = "Дата следующего списания", example = "2025-11-20")
    private LocalDate nextBillingDate;

    @Schema(description = "Дата и время создания платежа", example = "2025-10-20T15:30:00")
    private LocalDateTime createdAt;
}
