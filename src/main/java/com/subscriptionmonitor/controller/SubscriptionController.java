package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.CreateSubscriptionRequest;
import com.subscriptionmonitor.dto.SubscriptionResponse;
import com.subscriptionmonitor.dto.UpdateSubscriptionRequest;
import com.subscriptionmonitor.exception.notfound.PaymentNotFoundException;
import com.subscriptionmonitor.exception.notfound.SubscriptionNotFoundException;
import com.subscriptionmonitor.exception.validation.SubscriptionValidationException;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.security.SecurityService;
import com.subscriptionmonitor.service.PaymentService;
import com.subscriptionmonitor.service.SubscriptionService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "API для управления подписками")
@SecurityRequirement(name = "basicAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final SecurityService securityService;

    @Operation(
            summary = "Создать новую подписку",
            description = "Создание новой подписки для текущего пользователя. Доступ: ADMIN и USER - для своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Subscription name is required\",\"code\":\"SUBSCRIPTION_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Payment with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"PAYMENT_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}")))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest request) throws SubscriptionValidationException, PaymentNotFoundException {
        UUID currentUserId = securityService.getCurrentUserId();
        Subscription subscription = toEntityFromCreateRequest(request, currentUserId);
        Subscription created = subscriptionService.create(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Operation(
            summary = "Получить подписку по ID",
            description = "Возвращает подписку по ее ID. Доступ: ADMIN - любые подписки, USER - только свои подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription found"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not subscription owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Subscription with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"SUBSCRIPTION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isSubscriptionOwner(#id)")
    public ResponseEntity<SubscriptionResponse> getById(@Parameter(description = "Subscription ID") @PathVariable UUID id) throws SubscriptionNotFoundException {
        Subscription subscription = subscriptionService.findById(id);
        return ResponseEntity.ok(toResponse(subscription));
    }

    @Operation(
            summary = "Получить все подписки",
            description = "Возвращает список всех подписок. Доступ: ADMIN - все подписки, USER - только свои подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}")))
    })
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getAll() {
        UUID currentUserId = securityService.getCurrentUserId();
        List<SubscriptionResponse> subscriptions;

        if (securityService.isAdmin()) {
            subscriptions = subscriptionService.findAll().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            subscriptions = subscriptionService.findByUserId(currentUserId).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(subscriptions);
    }

    @Operation(
            summary = "Получить подписки пользователя",
            description = "Возвращает список подписок указанного пользователя. Доступ: ADMIN - подписки любого пользователя, USER - только свои подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not subscription owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You can only access your own subscriptions\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}\"}")))
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#userId)")
    public ResponseEntity<List<SubscriptionResponse>> getByUserId(@Parameter(description = "User ID") @PathVariable UUID userId) {
        List<SubscriptionResponse> subscriptions = subscriptionService.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(
            summary = "Получить активные подписки пользователя",
            description = "Возвращает только активные подписки указанного пользователя. Доступ: ADMIN - подписки любого пользователя, USER - только свои подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}/active\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not subscription owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You can only access your own subscriptions\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}/active\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}/active\"}")))
    })
    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#userId)")
    public ResponseEntity<List<SubscriptionResponse>> getActiveByUserId(@Parameter(description = "User ID") @PathVariable UUID userId) {
        List<SubscriptionResponse> subscriptions = subscriptionService
                .findActiveSubscriptionsByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(
            summary = "Получить подписки по категории",
            description = "Возвращает подписки, относящиеся к указанной категории. Доступ: ADMIN - все подписки в категории, USER - только свои подписки в этой категории."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/category/{categoryId}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/category/{categoryId}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/category/{categoryId}\"}")))
    })
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getByCategoryId(@Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        UUID currentUserId = securityService.getCurrentUserId();
        List<SubscriptionResponse> subscriptions;

        if (securityService.isAdmin()) {
            subscriptions = subscriptionService.findByCategoryId(categoryId).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            subscriptions = subscriptionService.findByCategoryId(categoryId).stream()
                    .filter(sub -> sub.getUserId().equals(currentUserId))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(subscriptions);
    }

    @Operation(
            summary = "Обновить подписку",
            description = "Обновляет данные подписки по ее ID. Доступ: ADMIN - любые подписки, USER - только свои подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Subscription name is required\",\"code\":\"SUBSCRIPTION_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not subscription owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Subscription with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"SUBSCRIPTION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}")))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isSubscriptionOwner(#id)")
    public ResponseEntity<SubscriptionResponse> update(@Parameter(description = "Subscription ID") @PathVariable UUID id, @Valid @RequestBody CreateSubscriptionRequest request) throws SubscriptionNotFoundException, SubscriptionValidationException, PaymentNotFoundException {
        Subscription existing = subscriptionService.findById(id);
        Subscription subscription = toEntityFromUpdateRequest(id, request, existing);
        Subscription updated = subscriptionService.update(subscription);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(
            summary = "Атомарное обновление подписки с данными платежа",
            description = "Обновляет подписку и связанные платежные данные в одной транзакции. Доступ: ADMIN - любые подписки, USER - только свои подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription updated successfully",
            content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Subscription name is required\",\"code\":\"SUBSCRIPTION_VALIDATION_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/subscriptions/{id}/with-payment\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/subscriptions/{id}/with-payment\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not subscription owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/subscriptions/{id}/with-payment\"}"))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Subscription with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"SUBSCRIPTION_NOT_FOUND\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/subscriptions/{id}/with-payment\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/subscriptions/{id}/with-payment\"}")))
    })
    @PutMapping("/{id}/with-payment")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isSubscriptionOwner(#id)")
    public ResponseEntity<SubscriptionResponse> updateWithPayment(
            @Parameter(description = "Subscription ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSubscriptionRequest request) throws SubscriptionNotFoundException, SubscriptionValidationException {
        Subscription updated = subscriptionService.updateWithPayment(
                id,
                request.getName(),
                request.getCategoryId(),
                request.getIsActive(),
                request.getCost(),
                request.getCurrency(),
                request.getBillingPeriodDays(),
                request.getNextBillingDate(),
                request.getOldNextBillingDate()
        );
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(
            summary = "Удалить подписку",
            description = "Удаляет подписку по ее ID. Доступ: ADMIN - любые подписки, USER - только свои подписки."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subscription deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not subscription owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Subscription with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"SUBSCRIPTION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}")))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isSubscriptionOwner(#id)")
    public ResponseEntity<Void> delete(@Parameter(description = "Subscription ID") @PathVariable UUID id) throws SubscriptionNotFoundException {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getName(),
                subscription.getUserId(),
                subscription.getCategoryId(),
                subscription.getPayment() != null ? subscription.getPayment().getId() : null,
                subscription.getIsActive(),
                subscription.getCreatedAt()
        );
    }

    private Subscription toEntityFromCreateRequest(CreateSubscriptionRequest request, UUID currentUserId) throws PaymentNotFoundException {
        Subscription subscription = new Subscription();
        subscription.setName(request.getName());
        subscription.setUserId(currentUserId);
        subscription.setCategoryId(request.getCategoryId());
        subscription.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getPaymentId() != null) {
            Payment payment = paymentService.findById(request.getPaymentId());
            subscription.setPayment(payment);
        }

        return subscription;
    }

    private Subscription toEntityFromUpdateRequest(UUID id, CreateSubscriptionRequest request, Subscription existing) throws PaymentNotFoundException {
        Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setName(request.getName());
        subscription.setUserId(existing.getUserId());
        subscription.setCategoryId(request.getCategoryId());
        subscription.setIsActive(request.getIsActive() != null ? request.getIsActive() : existing.getIsActive());
        subscription.setCreatedAt(existing.getCreatedAt());

        if (request.getPaymentId() != null) {
            Payment payment = paymentService.findById(request.getPaymentId());
            subscription.setPayment(payment);
        } else {
            subscription.setPayment(existing.getPayment());
        }

        return subscription;
    }
}
