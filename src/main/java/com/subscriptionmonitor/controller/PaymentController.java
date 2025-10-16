package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.PaymentDto;
import com.subscriptionmonitor.exception.notfound.PaymentNotFoundException;
import com.subscriptionmonitor.exception.validation.PaymentValidationException;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDto> create(@RequestBody PaymentDto paymentDto) throws PaymentValidationException {
        Payment payment = toEntity(paymentDto);
        Payment created = paymentService.create(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getById(@PathVariable UUID id) throws PaymentNotFoundException {
        Payment payment = paymentService.findById(id);
        return ResponseEntity.ok(toDto(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentDto>> getAll() {
        List<PaymentDto> payments = paymentService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<PaymentDto>> getByCurrency(@PathVariable Currency currency) {
        List<PaymentDto> payments = paymentService.findByCurrency(currency).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/billing-date-range")
    public ResponseEntity<List<PaymentDto>> getByBillingDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PaymentDto> payments = paymentService.findByNextBillingDateBetween(startDate, endDate).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/billing-date-before")
    public ResponseEntity<List<PaymentDto>> getByBillingDateBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PaymentDto> payments = paymentService.findByNextBillingDateBefore(date).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentDto> update(@PathVariable UUID id, @RequestBody PaymentDto paymentDto) throws PaymentNotFoundException, PaymentValidationException {
        Payment existing = paymentService.findById(id);
        paymentDto.setId(id);
        Payment payment = toEntity(paymentDto);
        Payment updated = paymentService.update(payment);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws PaymentNotFoundException {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PaymentDto toDto(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                payment.getCost(),
                payment.getCurrency(),
                payment.getBillingPeriodDays(),
                payment.getNextBillingDate(),
                payment.getCreatedAt()
        );
    }

    private Payment toEntity(PaymentDto dto) {
        Payment payment = new Payment();
        payment.setId(dto.getId());
        payment.setCost(dto.getCost());
        payment.setCurrency(dto.getCurrency());
        payment.setBillingPeriodDays(dto.getBillingPeriodDays());
        payment.setNextBillingDate(dto.getNextBillingDate());
        payment.setCreatedAt(dto.getCreatedAt());
        return payment;
    }
}
