package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Предстоящий платеж по подписке")
public class UpcomingPaymentDto {
    @Schema(description = "ID пользователя", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;

    @Schema(description = "Email пользователя", example = "john@example.com")
    private String email;

    @Schema(description = "ID подписки", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID subscriptionId;

    @Schema(description = "Название подписки", example = "Netflix Premium")
    private String subscriptionName;

    @Schema(description = "Название категории", example = "Развлечения")
    private String categoryName;

    @Schema(description = "Стоимость подписки", example = "999.00")
    private BigDecimal cost;

    @Schema(description = "Валюта платежа", example = "RUB")
    private Currency currency;

    @Schema(description = "Дата следующего списания", example = "2025-11-15")
    private LocalDate nextBillingDate;

    @Schema(description = "Период оплаты в днях", example = "30")
    private Integer billingPeriodDays;

    @Schema(description = "Срочность платежа", example = "In 3 Days",
            allowableValues = {"Overdue", "Today", "Tomorrow", "In 2 Days", "In 3 Days", "This Week", "This Month", "Later"})
    private String paymentUrgency;

    @Schema(description = "Количество дней просрочки (отрицательное для будущих платежей)", example = "0")
    private Integer daysOverdue;

    @Schema(description = "Количество дней до платежа (положительное для будущих платежей)", example = "3")
    private Integer daysUntilPayment;
}
