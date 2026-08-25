package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.notfound.PaymentNotFoundException;
import com.subscriptionmonitor.exception.validation.PaymentValidationException;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment create(Payment payment) throws PaymentValidationException {
        log.debug("Creating payment with cost: {}", payment.getCost());
        validatePayment(payment);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment findById(UUID id) throws PaymentNotFoundException {
        log.debug("Finding payment by id: {}", id);
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        log.debug("Finding all payments");
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> findByCurrency(Currency currency) {
        log.debug("Finding payments by currency: {}", currency);
        return paymentRepository.findByCurrency(currency);
    }

    @Transactional(readOnly = true)
    public List<Payment> findByNextBillingDateBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Finding payments between {} and {}", startDate, endDate);
        return paymentRepository.findByNextBillingDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Payment> findByNextBillingDateBefore(LocalDate date) {
        log.debug("Finding payments before date: {}", date);
        return paymentRepository.findByNextBillingDateBefore(date);
    }

    public Payment update(Payment payment) throws PaymentNotFoundException, PaymentValidationException {
        log.debug("Updating payment: {}", payment.getId());
        if (payment.getId() == null) {
            throw new PaymentValidationException("Payment ID cannot be null for update operation");
        }
        if (!paymentRepository.existsById(payment.getId())) {
            throw new PaymentNotFoundException(payment.getId());
        }
        validatePayment(payment);
        return paymentRepository.save(payment);
    }

    public void delete(UUID id) throws PaymentNotFoundException {
        log.debug("Deleting payment: {}", id);
        if (!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException(id);
        }
        paymentRepository.deleteById(id);
    }

    public void deleteAll() {
        log.debug("Deleting all payments");
        paymentRepository.deleteAll();
    }

    private void validatePayment(Payment payment) throws PaymentValidationException {
        if (payment == null) {
            throw new PaymentValidationException("Payment cannot be null");
        }
        if (payment.getCost() == null) {
            throw new PaymentValidationException("Payment cost cannot be null");
        }
        if (payment.getCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentValidationException("Payment cost must be greater than zero");
        }
        if (payment.getCurrency() == null) {
            throw new PaymentValidationException("Payment currency cannot be null");
        }
        if (payment.getBillingPeriodDays() == null || payment.getBillingPeriodDays() <= 0) {
            throw new PaymentValidationException("Billing period must be a positive number");
        }
        if (payment.getNextBillingDate() == null) {
            throw new PaymentValidationException("Next billing date cannot be null");
        }
    }
}
