package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = "payment")
public class Subscription extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
}
