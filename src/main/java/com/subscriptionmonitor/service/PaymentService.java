package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.storage.DataStorage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaymentService implements CrudService<Payment, Long> {
    private final DataStorage storage;

    public PaymentService() {
        this.storage = DataStorage.getInstance();
    }

    @Override
    public Payment create(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        validatePayment(payment);

        Long id = storage.generatePaymentId();
        payment.setId(id);
        storage.getPayments().put(id, payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.getPayments().get(id));
    }

    @Override
    public List<Payment> findAll() {
        return storage.getPayments().values().stream().collect(Collectors.toList());
    }

    @Override
    public Payment update(Payment payment) {
        if (payment == null || payment.getId() == null) {
            throw new IllegalArgumentException("Payment and payment ID cannot be null");
        }

        if (!storage.getPayments().containsKey(payment.getId())) {
            throw new IllegalArgumentException("Payment not found with ID: " + payment.getId());
        }

        validatePayment(payment);
        storage.getPayments().put(payment.getId(), payment);
        return payment;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.getPayments().remove(id) != null;
    }

    public List<Payment> findByCurrency(Currency currency) {
        if (currency == null) {
            return Collections.emptyList();
        }

        return storage.getPayments().values().stream()
                .filter(payment -> currency.equals(payment.getCurrency()))
                .collect(Collectors.toList());
    }

    public List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }

        return storage.getPayments().values().stream()
                .filter(payment -> payment.getNextBillingDate() != null
                                && !payment.getNextBillingDate().isBefore(startDate)
                                && !payment.getNextBillingDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public BigDecimal calculateTotalByCurrency(Currency currency) {
        if (currency == null) {
            return BigDecimal.ZERO;
        }

        return storage.getPayments().values().stream()
                .filter(payment -> currency.equals(payment.getCurrency()))
                .map(Payment::getCost)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalCount() {
        return storage.getTotalPaymentsCount();
    }

    public Optional<Payment> findByUuid(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }

        return storage.getPayments().values().stream()
                .filter(payment -> uuid.equals(payment.getUuid()))
                .findFirst();
    }

    public boolean deleteByUuid(UUID uuid) {
        Optional<Payment> payment = findByUuid(uuid);
        if (payment.isPresent()) {
            storage.getPayments().remove(payment.get().getId());
            return true;
        }
        return false;
    }

    private void validatePayment(Payment payment) {
        if (payment.getCost() == null || payment.getCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost cannot be null or negative");
        }

        if (payment.getBillingPeriodDays() == null || payment.getBillingPeriodDays() <= 0) {
            throw new IllegalArgumentException("Billing period must be positive");
        }

        if (payment.getCurrency() == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
    }
}