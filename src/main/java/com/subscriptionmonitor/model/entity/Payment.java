package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment extends BaseEntity {
    private BigDecimal cost;
    private Currency currency;
    private Integer billingPeriodDays;
    private LocalDate nextBillingDate;

    public Payment() {
        super();
        this.currency = Currency.RUB;
        this.billingPeriodDays = 30;
    }

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

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Integer getBillingPeriodDays() {
        return billingPeriodDays;
    }

    public void setBillingPeriodDays(Integer billingPeriodDays) {
        this.billingPeriodDays = billingPeriodDays;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + getId() +
                ", uuid=" + getUuid() +
                ", cost=" + cost +
                ", currency=" + currency +
                ", billingPeriodDays=" + billingPeriodDays +
                ", nextBillingDate=" + nextBillingDate +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}