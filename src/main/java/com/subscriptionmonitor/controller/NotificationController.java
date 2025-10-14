package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.NotificationDto;
import com.subscriptionmonitor.exception.notfound.NotificationNotFoundException;
import com.subscriptionmonitor.exception.validation.NotificationValidationException;
import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationDto> create(@RequestBody NotificationDto notificationDto) throws NotificationValidationException {
        Notification notification = toEntity(notificationDto);
        Notification created = notificationService.create(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getById(@PathVariable UUID id) throws NotificationNotFoundException {
        Notification notification = notificationService.findById(id);
        return ResponseEntity.ok(toDto(notification));
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAll() {
        List<NotificationDto> notifications = notificationService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getByUserId(@PathVariable UUID userId) {
        List<NotificationDto> notifications = notificationService.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<NotificationDto>> getBySubscriptionId(@PathVariable UUID subscriptionId) {
        List<NotificationDto> notifications = notificationService.findBySubscriptionId(subscriptionId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/sent/{isSent}")
    public ResponseEntity<List<NotificationDto>> getBySentStatus(@PathVariable Boolean isSent) {
        List<NotificationDto> notifications = notificationService.findByIsSent(isSent).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<NotificationDto>> getByUserIdAndType(
            @PathVariable UUID userId,
            @PathVariable NotificationType type) {
        List<NotificationDto> notifications = notificationService.findByUserIdAndType(userId, type).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<NotificationDto>> getPending() {
        List<NotificationDto> notifications = notificationService.getPendingNotifications().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/mark-sent")
    public ResponseEntity<Void> markAsSent(@PathVariable UUID id) throws NotificationNotFoundException {
        notificationService.markAsSent(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationDto> update(@PathVariable UUID id, @RequestBody NotificationDto notificationDto) throws NotificationNotFoundException, NotificationValidationException {
        Notification existing = notificationService.findById(id);
        notificationDto.setId(id);
        Notification notification = toEntity(notificationDto);
        Notification updated = notificationService.update(notification);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws NotificationNotFoundException {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getUserId(),
                notification.getSubscriptionId(),
                notification.getNotificationDate(),
                notification.getType(),
                notification.getIsSent(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }

    private Notification toEntity(NotificationDto dto) {
        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setUserId(dto.getUserId());
        notification.setSubscriptionId(dto.getSubscriptionId());
        notification.setNotificationDate(dto.getNotificationDate());
        notification.setType(dto.getType());
        notification.setIsSent(dto.getIsSent() != null ? dto.getIsSent() : false);
        notification.setMessage(dto.getMessage());
        notification.setCreatedAt(dto.getCreatedAt());
        return notification;
    }
}
