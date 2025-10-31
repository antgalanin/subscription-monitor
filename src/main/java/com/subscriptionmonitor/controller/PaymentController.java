package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.PaymentDto;
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
    public ResponseEntity<PaymentDto> create(@RequestBody PaymentDto paymentDto) throws PaymentValidationException {
        Payment payment = toEntity(paymentDto);
        Payment created = paymentService.create(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
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
    public ResponseEntity<PaymentDto> getById(@Parameter(description = "Payment ID") @PathVariable UUID id) throws PaymentNotFoundException {
        Payment payment = paymentService.findById(id);
        return ResponseEntity.ok(toDto(payment));
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
    public ResponseEntity<List<PaymentDto>> getAll() {
        List<PaymentDto> payments;

        if (securityService.isAdmin()) {
            payments = paymentService.findAll().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            java.util.Set<UUID> allowedPaymentIds = securityService.getPaymentIdsForCurrentUser();
            payments = paymentService.findAll().stream()
                    .filter(payment -> allowedPaymentIds.contains(payment.getId()))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

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
    public ResponseEntity<List<PaymentDto>> getByCurrency(@Parameter(description = "Currency") @PathVariable Currency currency) {
        List<PaymentDto> payments;

        if (securityService.isAdmin()) {
            payments = paymentService.findByCurrency(currency).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            java.util.Set<UUID> allowedPaymentIds = securityService.getPaymentIdsForCurrentUser();
            payments = paymentService.findByCurrency(currency).stream()
                    .filter(payment -> allowedPaymentIds.contains(payment.getId()))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(payments);
    }

    @Operation(
            summary = "Получить платежную информацию по диапазону дат",
            description = "Возвращает платежи с датой следующего списания в указанном диапазоне. Доступ: ADMIN - все платежи, USER - только платежи своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/billing-date-range\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/billing-date-range\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/billing-date-range\"}")))
    })
    @GetMapping("/billing-date-range")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDto>> getByBillingDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PaymentDto> payments;

        if (securityService.isAdmin()) {
            payments = paymentService.findByNextBillingDateBetween(startDate, endDate).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            java.util.Set<UUID> allowedPaymentIds = securityService.getPaymentIdsForCurrentUser();
            payments = paymentService.findByNextBillingDateBetween(startDate, endDate).stream()
                    .filter(payment -> allowedPaymentIds.contains(payment.getId()))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(payments);
    }

    @Operation(
            summary = "Получить платежную информацию до указанной даты",
            description = "Возвращает платежи с датой следующего списания до указанной даты. Доступ: ADMIN - все платежи, USER - только платежи своих подписок."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/billing-date-before\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/billing-date-before\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/payments/billing-date-before\"}")))
    })
    @GetMapping("/billing-date-before")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDto>> getByBillingDateBefore(
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PaymentDto> payments;

        if (securityService.isAdmin()) {
            payments = paymentService.findByNextBillingDateBefore(date).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            java.util.Set<UUID> allowedPaymentIds = securityService.getPaymentIdsForCurrentUser();
            payments = paymentService.findByNextBillingDateBefore(date).stream()
                    .filter(payment -> allowedPaymentIds.contains(payment.getId()))
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

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
    public ResponseEntity<PaymentDto> update(@Parameter(description = "Payment ID") @PathVariable UUID id, @RequestBody PaymentDto paymentDto) throws PaymentNotFoundException, PaymentValidationException {
        Payment existing = paymentService.findById(id);
        paymentDto.setId(id);
        Payment payment = toEntity(paymentDto);
        Payment updated = paymentService.update(payment);
        return ResponseEntity.ok(toDto(updated));
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
