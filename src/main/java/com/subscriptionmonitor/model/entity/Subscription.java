package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Subscription extends BaseEntity {
    private Long userId;
    private Long categoryId;
    private String name;
    private BigDecimal cost;
    private Currency currency;
    private Integer billingPeriodDays;
    private LocalDate nextBillingDate;
    private Boolean isActive;

    public Subscription() {
        super();
        this.currency = Currency.RUB;
        this.billingPeriodDays = 30;
        this.isActive = true;
    }

    public Subscription(Long userId, Long categoryId, String name, BigDecimal cost) {
        super();
        this.userId = userId;
        this.categoryId = categoryId;
        this.name = name;
        this.cost = cost;
        this.currency = Currency.RUB;
        this.billingPeriodDays = 30;
        this.isActive = true;
        this.nextBillingDate = LocalDate.now().plusDays(billingPeriodDays);
    }

    public Subscription(Long userId, Long categoryId, String name, BigDecimal cost,
                       Currency currency, Integer billingPeriodDays, LocalDate nextBillingDate) {
        super();
        this.userId = userId;
        this.categoryId = categoryId;
        this.name = name;
        this.cost = cost;
        this.currency = currency;
        this.billingPeriodDays = billingPeriodDays;
        this.nextBillingDate = nextBillingDate;
        this.isActive = true;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + getId() +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", cost=" + cost +
                ", currency=" + currency +
                ", billingPeriodDays=" + billingPeriodDays +
                ", nextBillingDate=" + nextBillingDate +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}