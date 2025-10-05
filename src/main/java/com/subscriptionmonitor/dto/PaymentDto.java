package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.Currency;
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
public class PaymentDto {
    private UUID id;
    private BigDecimal cost;
    private Currency currency;
    private Integer billingPeriodDays;
    private LocalDate nextBillingDate;
    private LocalDateTime createdAt;
}
