package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.NotificationDto;
import com.subscriptionmonitor.exception.notfound.NotificationNotFoundException;
import com.subscriptionmonitor.exception.validation.NotificationValidationException;
import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.security.SecurityService;
import com.subscriptionmonitor.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API для управления уведомлениями о предстоящих списаниях. Автоматически создаются при добавлении подписки.")
@SecurityRequirement(name = "basicAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityService securityService;

    @Operation(
        summary = "Создать новое уведомление",
        description = "Создает новое уведомление о предстоящем платеже. Требуется указать ID пользователя, ID подписки, дату уведомления, тип и сообщение. В большинстве случаев уведомления создаются автоматически при создании подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notification created successfully",
            content = @Content(schema = @Schema(implementation = NotificationDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Notification message is required\",\"code\":\"NOTIFICATION_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @PostMapping
    public ResponseEntity<NotificationDto> create(@RequestBody NotificationDto notificationDto) throws NotificationValidationException {
        UUID currentUserId = securityService.getCurrentUserId();
        notificationDto.setUserId(currentUserId);
        Notification notification = toEntity(notificationDto);
        Notification created = notificationService.create(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @Operation(
        summary = "Получить уведомление по ID",
        description = "Возвращает информацию об уведомлении по его уникальному идентификатору."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification found",
            content = @Content(schema = @Schema(implementation = NotificationDto.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not notification owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Notification not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Notification with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"NOTIFICATION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isNotificationOwner(#id)")
    public ResponseEntity<NotificationDto> getById(
        @Parameter(description = "UUID уведомления") @PathVariable UUID id
    ) throws NotificationNotFoundException {
        Notification notification = notificationService.findById(id);
        return ResponseEntity.ok(toDto(notification));
    }

    @Operation(
        summary = "Получить все уведомления",
        description = "Возвращает список всех уведомлений в системе."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAll() {
        UUID currentUserId = securityService.getCurrentUserId();
        List<NotificationDto> notifications;

        if (securityService.isAdmin()) {
            notifications = notificationService.findAll().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            notifications = notificationService.findAll().stream()
                    .filter(notification -> notification.getUserId().equals(currentUserId))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(notifications);
    }

    @Operation(
        summary = "Получить уведомления пользователя",
        description = "Возвращает все уведомления (отправленные и неотправленные) указанного пользователя."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getByUserId(
        @Parameter(description = "UUID пользователя") @PathVariable UUID userId
    ) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (!securityService.isAdmin() && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("You can only access your own notifications");
        }

        List<NotificationDto> notifications = notificationService.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @Operation(
        summary = "Получить уведомления по подписке",
        description = "Возвращает все уведомления, связанные с указанной подпиской."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<NotificationDto>> getBySubscriptionId(
        @Parameter(description = "UUID подписки") @PathVariable UUID subscriptionId
    ) {
        if (!securityService.isAdmin() && !securityService.isSubscriptionOwner(subscriptionId)) {
            throw new AccessDeniedException("You can only access notifications for your own subscriptions");
        }

        List<NotificationDto> notifications = notificationService.findBySubscriptionId(subscriptionId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @Operation(
        summary = "Получить уведомления по статусу отправки",
        description = "Возвращает уведомления с указанным статусом отправки (true - отправленные, false - неотправленные)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @GetMapping("/sent/{isSent}")
    public ResponseEntity<List<NotificationDto>> getBySentStatus(
        @Parameter(description = "Is sent status (true/false)", example = "true") @PathVariable Boolean isSent
    ) {
        UUID currentUserId = securityService.getCurrentUserId();
        List<NotificationDto> notifications;

        if (securityService.isAdmin()) {
            notifications = notificationService.findByIsSent(isSent).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            notifications = notificationService.findByIsSent(isSent).stream()
                    .filter(notification -> notification.getUserId().equals(currentUserId))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(notifications);
    }

    @Operation(
        summary = "Получить уведомления пользователя по типу",
        description = "Возвращает уведомления указанного пользователя с указанным типом (EMAIL, SMS, PUSH и т.д.)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<NotificationDto>> getByUserIdAndType(
            @Parameter(description = "UUID пользователя") @PathVariable UUID userId,
            @Parameter(description = "Тип уведомления (EMAIL, SMS, PUSH и т.д.)") @PathVariable NotificationType type) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (!securityService.isAdmin() && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("You can only access your own notifications");
        }

        List<NotificationDto> notifications = notificationService.findByUserIdAndType(userId, type).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @Operation(
        summary = "Получить ожидающие уведомления",
        description = "Возвращает все неотправленные уведомления (isSent = false). Полезно для обработки очереди уведомлений."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending notifications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/pending\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/pending\"}")))
    })
    @GetMapping("/pending")
    public ResponseEntity<List<NotificationDto>> getPending() {
        UUID currentUserId = securityService.getCurrentUserId();
        List<NotificationDto> notifications;

        if (securityService.isAdmin()) {
            notifications = notificationService.getPendingNotifications().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            notifications = notificationService.getPendingNotifications().stream()
                    .filter(notification -> notification.getUserId().equals(currentUserId))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(notifications);
    }

    @Operation(
        summary = "Пометить уведомление как отправленное",
        description = "Изменяет статус уведомления на отправленное (isSent = true). Используется после успешной отправки уведомления пользователю."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification marked as sent successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not notification owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}/mark-sent\"}"))),
        @ApiResponse(responseCode = "404", description = "Notification not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Notification with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"NOTIFICATION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}")))
    })
    @PatchMapping("/{id}/mark-sent")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isNotificationOwner(#id)")
    public ResponseEntity<Void> markAsSent(
        @Parameter(description = "UUID уведомления") @PathVariable UUID id
    ) throws NotificationNotFoundException {
        notificationService.markAsSent(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Обновить уведомление",
        description = "Обновляет существующее уведомление. Можно изменить дату уведомления, тип, сообщение или статус отправки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification updated successfully",
            content = @Content(schema = @Schema(implementation = NotificationDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Notification message is required\",\"code\":\"NOTIFICATION_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not notification owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Notification not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Notification with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"NOTIFICATION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}")))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isNotificationOwner(#id)")
    public ResponseEntity<NotificationDto> update(
        @Parameter(description = "UUID уведомления") @PathVariable UUID id,
        @RequestBody NotificationDto notificationDto
    ) throws NotificationNotFoundException, NotificationValidationException {
        Notification existing = notificationService.findById(id);
        notificationDto.setId(id);
        notificationDto.setUserId(existing.getUserId());
        notificationDto.setSubscriptionId(existing.getSubscriptionId());
        Notification notification = toEntity(notificationDto);
        Notification updated = notificationService.update(notification);
        return ResponseEntity.ok(toDto(updated));
    }

    @Operation(
        summary = "Удалить уведомление",
        description = "Полностью удаляет уведомление из системы."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not notification owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Notification not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Notification with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"NOTIFICATION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/{id}\"}")))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isNotificationOwner(#id)")
    public ResponseEntity<Void> delete(
        @Parameter(description = "UUID уведомления") @PathVariable UUID id
    ) throws NotificationNotFoundException {
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
