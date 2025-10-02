package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Payment extends BaseEntity {

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency = Currency.RUB;

    @Column(name = "billing_period_days", nullable = false)
    private Integer billingPeriodDays = 30;

    @Column(name = "next_billing_date", nullable = false)
    private LocalDate nextBillingDate;

    public Payment(BigDecimal cost) {
        super();
        this.cost = cost;
        this.currency = Currency.RUB;
        this.billingPeriodDays = 30;
        this.nextBillingDate = LocalDate.now().plusDays(billingPeriodDays);
    }

    public Payment(BigDecimal cost, Currency currency, Integer billingPeriodDays, LocalDate nextBillingDate) {
        super();
        this.cost = cost;
        this.currency = currency;
        this.billingPeriodDays = billingPeriodDays;
        this.nextBillingDate = nextBillingDate;
    }
}
