package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository для работы с платежами.
 *
 * @author Галанин А.Н.
 * @version 2.0 (ЛР2)
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Найти все платежи в указанной валюте.
     */
    List<Payment> findByCurrency(Currency currency);

    /**
     * Найти платежи с датой списания в заданном диапазоне.
     */
    List<Payment> findByNextBillingDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Найти платежи с датой списания до указанной.
     */
    List<Payment> findByNextBillingDateBefore(LocalDate date);
}
