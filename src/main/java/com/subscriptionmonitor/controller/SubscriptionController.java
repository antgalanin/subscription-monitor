package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.SubscriptionDto;
import com.subscriptionmonitor.exception.notfound.PaymentNotFoundException;
import com.subscriptionmonitor.exception.notfound.SubscriptionNotFoundException;
import com.subscriptionmonitor.exception.validation.SubscriptionValidationException;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.entity.Subscription;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Создать новую подписку")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Subscription name is required\",\"code\":\"SUBSCRIPTION_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Payment with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"PAYMENT_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}")))
    })
    @PostMapping
    public ResponseEntity<SubscriptionDto> create(@RequestBody SubscriptionDto subscriptionDto) throws SubscriptionValidationException, PaymentNotFoundException {
        Subscription subscription = toEntity(subscriptionDto);
        Subscription created = subscriptionService.create(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @Operation(summary = "Получить подписку по ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription found"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Subscription with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"SUBSCRIPTION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDto> getById(@Parameter(description = "Subscription ID") @PathVariable UUID id) throws SubscriptionNotFoundException {
        Subscription subscription = subscriptionService.findById(id);
        return ResponseEntity.ok(toDto(subscription));
    }

    @Operation(summary = "Получить все подписки")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions\"}")))
    })
    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAll() {
        List<SubscriptionDto> subscriptions = subscriptionService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Получить подписки пользователя")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}\"}")))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionDto>> getByUserId(@Parameter(description = "User ID") @PathVariable UUID userId) {
        List<SubscriptionDto> subscriptions = subscriptionService.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Получить подписки пользователя по статусу активности")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}/active/{isActive}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}/active/{isActive}\"}")))
    })
    @GetMapping("/user/{userId}/active/{isActive}")
    public ResponseEntity<List<SubscriptionDto>> getByUserIdAndIsActive(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Is active") @PathVariable Boolean isActive) {
        List<SubscriptionDto> subscriptions = subscriptionService
                .findByUserIdAndIsActive(userId, isActive).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Получить активные подписки пользователя")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}/active\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/user/{userId}/active\"}")))
    })
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<SubscriptionDto>> getActiveByUserId(@Parameter(description = "User ID") @PathVariable UUID userId) {
        List<SubscriptionDto> subscriptions = subscriptionService
                .findActiveSubscriptionsByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Получить подписки по категории")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/category/{categoryId}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/category/{categoryId}\"}")))
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubscriptionDto>> getByCategoryId(@Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        List<SubscriptionDto> subscriptions = subscriptionService.findByCategoryId(categoryId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Обновить подписку")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Subscription name is required\",\"code\":\"SUBSCRIPTION_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Subscription with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"SUBSCRIPTION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDto> update(@Parameter(description = "Subscription ID") @PathVariable UUID id, @RequestBody SubscriptionDto subscriptionDto) throws SubscriptionNotFoundException, SubscriptionValidationException, PaymentNotFoundException {
        Subscription existing = subscriptionService.findById(id);
        subscriptionDto.setId(id);
        Subscription subscription = toEntity(subscriptionDto);
        Subscription updated = subscriptionService.update(subscription);
        return ResponseEntity.ok(toDto(updated));
    }

    @Operation(summary = "Деактивировать подписку")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subscription deactivated successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}/deactivate\"}"))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Subscription with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"SUBSCRIPTION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}/deactivate\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}/deactivate\"}")))
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SubscriptionDto> deactivate(@Parameter(description = "Subscription ID") @PathVariable UUID id) throws SubscriptionNotFoundException {
        Subscription subscription = subscriptionService.deactivate(id);
        return ResponseEntity.ok(toDto(subscription));
    }

    @Operation(summary = "Удалить подписку")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subscription deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Subscription with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"SUBSCRIPTION_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/subscriptions/{id}\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Subscription ID") @PathVariable UUID id) throws SubscriptionNotFoundException {
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
