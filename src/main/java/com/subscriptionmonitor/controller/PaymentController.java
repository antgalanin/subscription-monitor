package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.CreatePaymentRequest;
import com.subscriptionmonitor.dto.PaymentResponse;
import com.subscriptionmonitor.dto.UpdatePaymentRequest;
import com.subscriptionmonitor.exception.notfound.PaymentNotFoundException;
import com.subscriptionmonitor.exception.validation.PaymentValidationException;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.security.SecurityService;
import com.subscriptionmonitor.service.PaymentService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Payments", description = "API для управления платежной информацией")
@SecurityRequirement(name = "basicAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityService securityService;

    @Operation(
            summary = "Создать новую платежную информацию",
            description = "Создание новой платежной информации для подписки. Доступ: ADMIN и USER - для своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payment created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Cost is required\",\"code\":\"PAYMENT_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments\"}")))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) throws PaymentValidationException {
        Payment payment = toEntityFromCreateRequest(request);
        Payment created = paymentService.create(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Operation(
            summary = "Получить платежную информацию по ID",
            description = "Возвращает платежную информацию по ее ID. Доступ: ADMIN - любые платежи, USER - только платежи своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment found"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not payment owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Payment with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"PAYMENT_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPaymentOwner(#id)")
    public ResponseEntity<PaymentResponse> getById(@Parameter(description = "Payment ID") @PathVariable UUID id) throws PaymentNotFoundException {
        Payment payment = paymentService.findById(id);
        return ResponseEntity.ok(toResponse(payment));
    }

    @Operation(
            summary = "Получить всю платежную информацию",
            description = "Возвращает список всех платежей. Доступ: ADMIN - все платежи, USER - только платежи своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments\"}")))
    })
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAll() {
        List<com.subscriptionmonitor.model.entity.Payment> allPayments = paymentService.findAll();
        List<com.subscriptionmonitor.model.entity.Payment> accessiblePayments =
            securityService.filterAccessiblePayments(allPayments, com.subscriptionmonitor.model.entity.Payment::getId);

        List<PaymentResponse> payments = accessiblePayments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(payments);
    }

    @Operation(
            summary = "Получить платежную информацию по валюте",
            description = "Возвращает список платежей с указанной валютой. Доступ: ADMIN - все платежи, USER - только платежи своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/currency/{currency}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/currency/{currency}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/currency/{currency}\"}")))
    })
    @GetMapping("/currency/{currency}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getByCurrency(@Parameter(description = "Currency") @PathVariable Currency currency) {
        List<com.subscriptionmonitor.model.entity.Payment> paymentsByCurrency = paymentService.findByCurrency(currency);
        List<com.subscriptionmonitor.model.entity.Payment> accessiblePayments =
            securityService.filterAccessiblePayments(paymentsByCurrency, com.subscriptionmonitor.model.entity.Payment::getId);

        List<PaymentResponse> payments = accessiblePayments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(payments);
    }

    @Operation(
            summary = "Обновить платежную информацию",
            description = "Обновляет платежную информацию по ее ID. Доступ: ADMIN - любые платежи, USER - только платежи своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Cost is required\",\"code\":\"PAYMENT_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not payment owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Payment with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"PAYMENT_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}")))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPaymentOwner(#id)")
    public ResponseEntity<PaymentResponse> update(@Parameter(description = "Payment ID") @PathVariable UUID id, @Valid @RequestBody UpdatePaymentRequest request) throws PaymentNotFoundException, PaymentValidationException {
        Payment existing = paymentService.findById(id);
        Payment payment = toEntityFromUpdateRequest(id, request, existing);
        Payment updated = paymentService.update(payment);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(
            summary = "Удалить платежную информацию",
            description = "Удаляет платежную информацию по ее ID. Доступ: ADMIN - любые платежи, USER - только платежи своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Payment deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - not payment owner",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Payment with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"PAYMENT_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/{id}\"}")))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPaymentOwner(#id)")
    public ResponseEntity<Void> delete(@Parameter(description = "Payment ID") @PathVariable UUID id) throws PaymentNotFoundException {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getCost(),
                payment.getCurrency(),
                payment.getBillingPeriodDays(),
                payment.getNextBillingDate(),
                payment.getCreatedAt()
        );
    }

    private Payment toEntityFromCreateRequest(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setCost(request.getCost());
        payment.setCurrency(request.getCurrency());
        payment.setBillingPeriodDays(request.getBillingPeriodDays());
        payment.setNextBillingDate(request.getNextBillingDate());
        return payment;
    }

    private Payment toEntityFromUpdateRequest(UUID id, UpdatePaymentRequest request, Payment existing) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setCost(request.getCost() != null ? request.getCost() : existing.getCost());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : existing.getCurrency());
        payment.setBillingPeriodDays(request.getBillingPeriodDays() != null ? request.getBillingPeriodDays() : existing.getBillingPeriodDays());
        payment.setNextBillingDate(request.getNextBillingDate() != null ? request.getNextBillingDate() : existing.getNextBillingDate());
        payment.setCreatedAt(existing.getCreatedAt());
        return payment;
    }
}
