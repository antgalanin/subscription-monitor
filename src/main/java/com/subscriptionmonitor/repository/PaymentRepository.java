package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByCurrency(Currency currency);

    List<Payment> findByNextBillingDateBetween(LocalDate startDate, LocalDate endDate);

    List<Payment> findByNextBillingDateBefore(LocalDate date);
}
