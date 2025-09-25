package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Subscription extends BaseEntity {
    private Long userId;
    private Long categoryId;
    private String name;
    private Payment payment;
    private Boolean isActive;

    public Subscription() {
        super();
        this.payment = new Payment();
        this.isActive = true;
    }

    public Subscription(Long userId, Long categoryId, String name, BigDecimal cost) {
        super();
        this.userId = userId;
        this.categoryId = categoryId;
        this.name = name;
        this.payment = new Payment(cost);
        this.isActive = true;
    }

    public Subscription(Long userId, Long categoryId, String name, BigDecimal cost,
                       Currency currency, Integer billingPeriodDays, LocalDate nextBillingDate) {
        super();
        this.userId = userId;
        this.categoryId = categoryId;
        this.name = name;
        this.payment = new Payment(cost, currency, billingPeriodDays, nextBillingDate);
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

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    // Convenience methods для обратной совместимости
    public BigDecimal getCost() {
        return payment != null ? payment.getCost() : null;
    }

    public void setCost(BigDecimal cost) {
        if (payment == null) {
            payment = new Payment();
        }
        payment.setCost(cost);
    }

    public Currency getCurrency() {
        return payment != null ? payment.getCurrency() : null;
    }

    public void setCurrency(Currency currency) {
        if (payment == null) {
            payment = new Payment();
        }
        payment.setCurrency(currency);
    }

    public Integer getBillingPeriodDays() {
        return payment != null ? payment.getBillingPeriodDays() : null;
    }

    public void setBillingPeriodDays(Integer billingPeriodDays) {
        if (payment == null) {
            payment = new Payment();
        }
        payment.setBillingPeriodDays(billingPeriodDays);
    }

    public LocalDate getNextBillingDate() {
        return payment != null ? payment.getNextBillingDate() : null;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        if (payment == null) {
            payment = new Payment();
        }
        payment.setNextBillingDate(nextBillingDate);
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
                ", uuid=" + getUuid() +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", payment=" + payment +
                ", isActive=" + isActive +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}