package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.CategoryStatisticsDto;
import com.subscriptionmonitor.dto.UpcomingPaymentDto;
import com.subscriptionmonitor.dto.UserStatisticsDto;
import com.subscriptionmonitor.exception.notfound.UserNotFoundException;
import com.subscriptionmonitor.security.SecurityService;
import com.subscriptionmonitor.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/api/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "API для получения аналитических данных и статистики")
@SecurityRequirement(name = "cookieAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityService securityService;

    @Operation(
            summary = "Получить статистику текущего пользователя",
            description = "Возвращает сводную статистику подписок текущего пользователя. Доступ: ADMIN и USER - только свои данные."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserStatisticsDto.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-statistics\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-statistics\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-statistics\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-statistics\"}")))
    })
    @GetMapping("/my-statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserStatisticsDto> getMyStatistics() throws UserNotFoundException {
        log.debug("Getting statistics for current user");
        UserStatisticsDto statistics = analyticsService.getUserStatistics(securityService.getCurrentUserId());
        return ResponseEntity.ok(statistics);
    }

    @Operation(
            summary = "Получить предстоящие платежи текущего пользователя",
            description = "Возвращает список предстоящих платежей с индикаторами срочности. Доступ: ADMIN и USER - только свои данные."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Upcoming payments retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UpcomingPaymentDto.class)))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-upcoming-payments\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-upcoming-payments\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-upcoming-payments\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-upcoming-payments\"}")))
    })
    @GetMapping("/my-upcoming-payments")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<UpcomingPaymentDto>> getMyUpcomingPayments() {
        log.debug("Getting upcoming payments for current user");
        List<UpcomingPaymentDto> payments = analyticsService.getUpcomingPayments(securityService.getCurrentUserId());
        return ResponseEntity.ok(payments);
    }

    @Operation(
            summary = "Получить статистику по категориям текущего пользователя",
            description = "Возвращает статистику по категориям для текущего пользователя. Доступ: ADMIN и USER - только свои данные."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category statistics retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryStatisticsDto.class)))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-category-statistics\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-category-statistics\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/my-category-statistics\"}")))
    })
    @GetMapping("/my-category-statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<CategoryStatisticsDto>> getMyCategoryStatistics() {
        log.debug("Getting category statistics for current user");
        List<CategoryStatisticsDto> statistics = analyticsService.getCategoryStatistics(securityService.getCurrentUserId());
        return ResponseEntity.ok(statistics);
    }

    @Operation(
            summary = "Получить статистику пользователя",
            description = "Возвращает сводную статистику подписок указанного пользователя. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserStatisticsDto.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/statistics\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/statistics\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/statistics\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/statistics\"}")))
    })
    @GetMapping("/users/{userId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatisticsDto> getUserStatistics(@PathVariable UUID userId) throws UserNotFoundException {
        log.debug("Getting statistics for user: {}", userId);
        UserStatisticsDto statistics = analyticsService.getUserStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(
        summary = "Получить предстоящие платежи пользователя",
        description = "Возвращает список предстоящих платежей указанного пользователя. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Upcoming payments retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UpcomingPaymentDto.class)))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/upcoming-payments\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/upcoming-payments\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/upcoming-payments\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/upcoming-payments\"}")))
    })
    @GetMapping("/users/{userId}/upcoming-payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UpcomingPaymentDto>> getUpcomingPayments(@PathVariable UUID userId) {
        log.debug("Getting upcoming payments for user: {}", userId);
        List<UpcomingPaymentDto> payments = analyticsService.getUpcomingPayments(userId);
        return ResponseEntity.ok(payments);
    }

    @Operation(
            summary = "Получить статистику по категориям пользователя",
            description = "Возвращает статистику по категориям указанного пользователя. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category statistics retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryStatisticsDto.class)))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/category-statistics\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/category-statistics\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/category-statistics\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/analytics/users/{userId}/category-statistics\"}")))
    })
    @GetMapping("/users/{userId}/category-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryStatisticsDto>> getCategoryStatistics(@PathVariable UUID userId) {
        log.debug("Getting category statistics for user: {}", userId);
        List<CategoryStatisticsDto> statistics = analyticsService.getCategoryStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
}
