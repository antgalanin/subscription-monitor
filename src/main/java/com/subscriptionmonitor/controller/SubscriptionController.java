package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.SubscriptionDto;
import com.subscriptionmonitor.exception.notfound.PaymentNotFoundException;
import com.subscriptionmonitor.exception.notfound.SubscriptionNotFoundException;
import com.subscriptionmonitor.exception.validation.SubscriptionValidationException;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.service.PaymentService;
import com.subscriptionmonitor.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<SubscriptionDto> create(@RequestBody SubscriptionDto subscriptionDto) throws SubscriptionValidationException, PaymentNotFoundException {
        Subscription subscription = toEntity(subscriptionDto);
        Subscription created = subscriptionService.create(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDto> getById(@PathVariable UUID id) throws SubscriptionNotFoundException {
        Subscription subscription = subscriptionService.findById(id);
        return ResponseEntity.ok(toDto(subscription));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAll() {
        List<SubscriptionDto> subscriptions = subscriptionService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionDto>> getByUserId(@PathVariable UUID userId) {
        List<SubscriptionDto> subscriptions = subscriptionService.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/user/{userId}/active/{isActive}")
    public ResponseEntity<List<SubscriptionDto>> getByUserIdAndIsActive(
            @PathVariable UUID userId,
            @PathVariable Boolean isActive) {
        List<SubscriptionDto> subscriptions = subscriptionService
                .findByUserIdAndIsActive(userId, isActive).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<SubscriptionDto>> getActiveByUserId(@PathVariable UUID userId) {
        List<SubscriptionDto> subscriptions = subscriptionService
                .findActiveSubscriptionsByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubscriptionDto>> getByCategoryId(@PathVariable UUID categoryId) {
        List<SubscriptionDto> subscriptions = subscriptionService.findByCategoryId(categoryId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDto> update(@PathVariable UUID id, @RequestBody SubscriptionDto subscriptionDto) throws SubscriptionNotFoundException, SubscriptionValidationException, PaymentNotFoundException {
        Subscription existing = subscriptionService.findById(id);
        subscriptionDto.setId(id);
        Subscription subscription = toEntity(subscriptionDto);
        Subscription updated = subscriptionService.update(subscription);
        return ResponseEntity.ok(toDto(updated));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SubscriptionDto> deactivate(@PathVariable UUID id) throws SubscriptionNotFoundException {
        Subscription subscription = subscriptionService.deactivate(id);
        return ResponseEntity.ok(toDto(subscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws SubscriptionNotFoundException {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SubscriptionDto toDto(Subscription subscription) {
        return new SubscriptionDto(
                subscription.getId(),
                subscription.getName(),
                subscription.getUserId(),
                subscription.getCategoryId(),
                subscription.getPayment() != null ? subscription.getPayment().getId() : null,
                subscription.getIsActive(),
                subscription.getCreatedAt()
        );
    }

    private Subscription toEntity(SubscriptionDto dto) throws PaymentNotFoundException {
        Subscription subscription = new Subscription();
        subscription.setId(dto.getId());
        subscription.setName(dto.getName());
        subscription.setUserId(dto.getUserId());
        subscription.setCategoryId(dto.getCategoryId());
        subscription.setIsActive(dto.getIsActive());
        subscription.setCreatedAt(dto.getCreatedAt());

        if (dto.getPaymentId() != null) {
            Payment payment = paymentService.findById(dto.getPaymentId());
            subscription.setPayment(payment);
        }

        return subscription;
    }
}
