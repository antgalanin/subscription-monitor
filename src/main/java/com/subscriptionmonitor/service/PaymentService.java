package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment create(Payment payment) {
        log.debug("Creating payment with cost: {}", payment.getCost());
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findById(UUID id) {
        log.debug("Finding payment by id: {}", id);
        return paymentRepository.findById(id);
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

    public Payment update(Payment payment) {
        log.debug("Updating payment: {}", payment.getId());
        return paymentRepository.save(payment);
    }

    public void delete(UUID id) {
        log.debug("Deleting payment: {}", id);
        paymentRepository.deleteById(id);
    }

    public void deleteAll() {
        log.debug("Deleting all payments");
        paymentRepository.deleteAll();
    }
}
