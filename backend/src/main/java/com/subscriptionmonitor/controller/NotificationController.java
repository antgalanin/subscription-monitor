package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.CreateNotificationRequest;
import com.subscriptionmonitor.dto.NotificationResponse;
import com.subscriptionmonitor.dto.UpdateNotificationRequest;
import com.subscriptionmonitor.exception.notfound.NotificationNotFoundException;
import com.subscriptionmonitor.exception.validation.NotificationValidationException;
import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.security.SecurityService;
import com.subscriptionmonitor.service.NotificationService;
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
import jakarta.validation.Valid;
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
@SecurityRequirement(name = "cookieAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityService securityService;

    @Operation(
            summary = "Создать новое уведомление",
            description = "Создание нового уведомления для текущего пользователя. Обычно уведомления создаются автоматически. Доступ: ADMIN и USER - для своих уведомлений."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notification created successfully",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Notification message is required\",\"code\":\"NOTIFICATION_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) throws NotificationValidationException {
        UUID currentUserId = securityService.getCurrentUserId();
        Notification notification = toEntityFromCreateRequest(request, currentUserId);
        Notification created = notificationService.create(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Operation(
            summary = "Получить уведомление по ID",
            description = "Возвращает уведомление по его ID. Доступ: ADMIN - любые уведомления, USER - только свои уведомления."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification found",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
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
    public ResponseEntity<NotificationResponse> getById(
        @Parameter(description = "UUID уведомления") @PathVariable UUID id
    ) throws NotificationNotFoundException {
        Notification notification = notificationService.findById(id);
        return ResponseEntity.ok(toResponse(notification));
    }

    @Operation(
            summary = "Получить все уведомления",
            description = "Возвращает список всех уведомлений. Доступ: ADMIN - все уведомления, USER - только свои уведомления."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getAll() {
        UUID currentUserId = securityService.getCurrentUserId();
        List<NotificationResponse> notifications;

        if (securityService.isAdmin()) {
            notifications = notificationService.findAll().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            notifications = notificationService.findAll().stream()
                    .filter(notification -> notification.getUserId().equals(currentUserId))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Получить уведомления пользователя",
            description = "Возвращает все уведомления указанного пользователя. Доступ: ADMIN - уведомления любого пользователя, USER - только свои уведомления."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/user/{userId}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not notification owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You can only access your own notifications\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/user/{userId}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/user/{userId}\"}")))
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#userId)")
    public ResponseEntity<List<NotificationResponse>> getByUserId(
        @Parameter(description = "UUID пользователя") @PathVariable UUID userId
    ) {
        List<NotificationResponse> notifications = notificationService.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Получить уведомления по подписке",
            description = "Возвращает уведомления связанные с указанной подпиской. Доступ: ADMIN - любые подписки, USER - только свои подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/subscription/{subscriptionId}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not subscription owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You can only access notifications for your own subscriptions\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/subscription/{subscriptionId}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/subscription/{subscriptionId}\"}")))
    })
    @GetMapping("/subscription/{subscriptionId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isSubscriptionOwner(#subscriptionId)")
    public ResponseEntity<List<NotificationResponse>> getBySubscriptionId(
        @Parameter(description = "UUID подписки") @PathVariable UUID subscriptionId
    ) {
        List<NotificationResponse> notifications = notificationService.findBySubscriptionId(subscriptionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Получить уведомления по статусу отправки",
            description = "Возвращает уведомления по статусу отправки (true/false). Доступ: ADMIN - все уведомления, USER - только свои уведомления."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications\"}")))
    })
    @GetMapping("/sent/{isSent}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getBySentStatus(
        @Parameter(description = "Is sent status (true/false)", example = "true") @PathVariable Boolean isSent
    ) {
        UUID currentUserId = securityService.getCurrentUserId();
        List<NotificationResponse> notifications;

        if (securityService.isAdmin()) {
            notifications = notificationService.findByIsSent(isSent).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            notifications = notificationService.findByIsSent(isSent).stream()
                    .filter(notification -> notification.getUserId().equals(currentUserId))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Получить полученные уведомления текущего пользователя",
            description = "Возвращает уведомления текущего пользователя с наступившей датой. Доступ: ADMIN и USER - только свои уведомления."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Received notifications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/my/received\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/my/received\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/my/received\"}")))
    })
    @GetMapping("/my/received")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getMyReceived() {
        UUID currentUserId = securityService.getCurrentUserId();
        List<NotificationResponse> notifications = notificationService.findReceivedByUserId(currentUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Получить ожидающие уведомления",
            description = "Возвращает неотправленные уведомления (isSent = false). Доступ: ADMIN - все ожидающие, USER - только свои."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending notifications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/pending\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/pending\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/pending\"}")))
    })
    @GetMapping("/pending")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getPending() {
        UUID currentUserId = securityService.getCurrentUserId();
        List<NotificationResponse> notifications;

        if (securityService.isAdmin()) {
            notifications = notificationService.getPendingNotifications().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            notifications = notificationService.getPendingNotifications().stream()
                    .filter(notification -> notification.getUserId().equals(currentUserId))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Пометить просроченные уведомления как отправленные",
            description = "Помечает просроченные уведомления как отправленные (isSent = true). Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending notifications marked as sent"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/mark-pending-sent\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/mark-pending-sent\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/notifications/mark-pending-sent\"}")))
    })
    @PostMapping("/mark-pending-sent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> markPendingAsSent() {
        int marked = notificationService.markPendingAsSent();
        return ResponseEntity.ok(marked);
    }

    @Operation(
            summary = "Обновить уведомление",
            description = "Обновляет данные уведомления по его ID. Доступ: ADMIN - любые уведомления, USER - только свои уведомления."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification updated successfully",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
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
    public ResponseEntity<NotificationResponse> update(
        @Parameter(description = "UUID уведомления") @PathVariable UUID id,
        @Valid @RequestBody UpdateNotificationRequest request
    ) throws NotificationNotFoundException, NotificationValidationException {
        Notification existing = notificationService.findById(id);
        Notification notification = toEntityFromUpdateRequest(id, request, existing);
        Notification updated = notificationService.update(notification);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(
            summary = "Удалить уведомление",
            description = "Удаляет уведомление по его ID. Доступ: ADMIN - любые уведомления, USER - только свои уведомления."
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

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
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

    private Notification toEntityFromCreateRequest(CreateNotificationRequest request, UUID currentUserId) {
        Notification notification = new Notification();
        notification.setUserId(currentUserId);
        notification.setSubscriptionId(request.getSubscriptionId());
        notification.setNotificationDate(request.getNotificationDate());
        notification.setType(request.getType());
        notification.setIsSent(request.getIsSent() != null ? request.getIsSent() : false);
        notification.setMessage(request.getMessage());
        return notification;
    }

    private Notification toEntityFromUpdateRequest(UUID id, UpdateNotificationRequest request, Notification existing) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(existing.getUserId());
        notification.setSubscriptionId(existing.getSubscriptionId());
        notification.setNotificationDate(request.getNotificationDate() != null ? request.getNotificationDate() : existing.getNotificationDate());
        notification.setType(request.getType() != null ? request.getType() : existing.getType());
        notification.setIsSent(request.getIsSent() != null ? request.getIsSent() : existing.getIsSent());
        notification.setMessage(request.getMessage() != null ? request.getMessage() : existing.getMessage());
        notification.setCreatedAt(existing.getCreatedAt());
        return notification;
    }
}
