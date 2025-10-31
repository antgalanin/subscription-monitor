package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.ChangePasswordRequest;
import com.subscriptionmonitor.dto.UpdateEmailRequest;
import com.subscriptionmonitor.dto.UserDto;
import com.subscriptionmonitor.exception.notfound.UserNotFoundException;
import com.subscriptionmonitor.exception.validation.UserValidationException;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.security.SecurityService;
import com.subscriptionmonitor.service.UserService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации и регистрации")
public class AuthController {

    private final UserService userService;
    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "Получить информацию о текущем пользователе",
            description = "Возвращает информацию о текущем пользователе. Доступ: ADMIN и USER - информация о себе."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class),
                examples = @ExampleObject(value = "{\"id\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"username\":\"admin\",\"email\":\"admin@example.com\",\"role\":\"ADMIN\",\"notificationDays\":3,\"createdAt\":\"2025-10-25T18:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid credentials",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Full authentication is required to access this resource\",\"code\":\"AUTHENTICATION_ERROR\",\"timestamp\":\"2025-10-25T18:30:00\",\"path\":\"/api/auth/me\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-25T18:30:00\",\"path\":\"/api/auth/me\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-25T18:30:00\",\"path\":\"/api/auth/me\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-25T18:30:00\",\"path\":\"/api/auth/me\"}")))
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getCurrentUser() throws UserNotFoundException {
        log.info("Get current user request");
        java.util.UUID userId = securityService.getCurrentUserId();
        User user = userService.findById(userId);
        return ResponseEntity.ok(toDto(user));
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрация нового пользователя с ролью USER. Доступ: публичный endpoint, не требует аутентификации."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Username is required\",\"code\":\"USER_VALIDATION_ERROR\",\"timestamp\":\"2025-10-25T18:30:00\",\"path\":\"/api/auth/register\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-25T18:30:00\",\"path\":\"/api/auth/register\"}")))
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto userDto) throws UserValidationException {
        log.info("New user registration request: {}", userDto.getUsername());
        User user = toEntity(userDto);
        User registered = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(registered));
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                user.getRole(),
                user.getNotificationDays(),
                user.getCreatedAt()
        );
    }

    @Operation(
            summary = "Обновить email пользователя",
            description = "Обновляет email адрес пользователя. Доступ: ADMIN и USER - только свой email."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Email updated successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Email не может быть пустым\",\"code\":\"USER_VALIDATION_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/email\"}"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/email\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/email\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/email\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/email\"}")))
    })
    @SecurityRequirement(name = "basicAuth")
    @PutMapping("/{id}/email")
    @PreAuthorize("@securityService.isOwner(#id)")
    public ResponseEntity<UserDto> updateEmail(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @RequestBody UpdateEmailRequest request) throws UserNotFoundException, UserValidationException {
        log.info("Update email request for user: {}", id);

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new UserValidationException("Email не может быть пустым");
        }

        User user = userService.findById(id);
        user.setEmail(request.getEmail().trim());

        User currentUser = userService.findById(securityService.getCurrentUserId());
        User updated = userService.update(user, currentUser);

        return ResponseEntity.ok(toDto(updated));
    }

    @Operation(
            summary = "Изменить пароль пользователя",
            description = "Изменяет пароль пользователя. Доступ: ADMIN и USER - только свой пароль."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed - invalid current password or new password too short",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Новый пароль должен содержать минимум 6 символов\",\"code\":\"USER_VALIDATION_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/change-password\"}"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/change-password\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/change-password\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/change-password\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-30T18:30:00\",\"path\":\"/api/auth/{id}/change-password\"}")))
    })
    @SecurityRequirement(name = "basicAuth")
    @PostMapping("/{id}/change-password")
    @PreAuthorize("@securityService.isOwner(#id)")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @RequestBody ChangePasswordRequest request) throws UserNotFoundException, UserValidationException {
        log.info("Change password request for user: {}", id);

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
            throw new UserValidationException("Текущий пароль обязателен");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new UserValidationException("Новый пароль должен содержать минимум 6 символов");
        }

        User user = userService.findById(id);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UserValidationException("Неверный текущий пароль");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User currentUser = userService.findById(securityService.getCurrentUserId());
        userService.update(user, currentUser);

        return ResponseEntity.ok().build();
    }

    private User toEntity(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        if (dto.getNotificationDays() != null) {
            user.setNotificationDays(dto.getNotificationDays());
        } else {
            user.setNotificationDays(3);
        }
        return user;
    }
}
