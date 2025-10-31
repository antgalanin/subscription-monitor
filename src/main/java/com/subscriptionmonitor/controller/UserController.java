package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.UserDto;
import com.subscriptionmonitor.exception.notfound.UserNotFoundException;
import com.subscriptionmonitor.exception.validation.UserValidationException;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.UserRole;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями системы")
@SecurityRequirement(name = "basicAuth")
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;

    @Operation(
            summary = "Создать нового пользователя",
            description = "Создание нового пользователя. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User successfully created",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Username is required\",\"code\":\"USER_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users\"}"))),
        @ApiResponse(responseCode = "404", description = "Current user not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users\"}")))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> create(@RequestBody UserDto userDto) throws UserValidationException, UserNotFoundException {
        User currentUser = userService.findById(securityService.getCurrentUserId());
        User user = toEntity(userDto);
        User created = userService.create(user, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе по его ID. Доступ: ADMIN - любой пользователь, USER - только свой профиль."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\",\"code\":\"ACCESS_FORBIDDEN\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<UserDto> getById(@Parameter(description = "User ID") @PathVariable UUID id) throws UserNotFoundException {
        User user = userService.findById(id);
        return ResponseEntity.ok(toDto(user));
    }

    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\",\"code\":\"ACCESS_FORBIDDEN\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users\"}")))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAll() {
        List<UserDto> users = userService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Найти пользователя по username",
            description = "Возвращает пользователя с указанным именем. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/username/{username}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/username/{username}\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User with username john_doe not found\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/username/{username}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/username/{username}\"}")))
    })
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getByUsername(@Parameter(description = "Username") @PathVariable String username) throws UserNotFoundException {
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(toDto(user));
    }

    @Operation(
            summary = "Найти пользователя по email",
            description = "Возвращает пользователя с указанным email. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/email/{email}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - admin role required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/email/{email}\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User with email john@example.com not found\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/email/{email}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/email/{email}\"}")))
    })
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getByEmail(@Parameter(description = "Email") @PathVariable String email) throws UserNotFoundException {
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(toDto(user));
    }

    @Operation(
            summary = "Получить пользователей по роли",
            description = "Возвращает список пользователей с указанной ролью. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/role/{role}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\",\"code\":\"ACCESS_FORBIDDEN\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/role/{role}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/role/{role}\"}")))
    })
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getByRole(@Parameter(description = "User role (USER or ADMIN)") @PathVariable UserRole role) {
        List<UserDto> users = userService.findByRole(role).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Обновить данные пользователя",
            description = "Обновляет информацию о пользователе. Доступ: ADMIN - любой пользователь, USER - только свой профиль."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Email is required\",\"code\":\"USER_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\",\"code\":\"ACCESS_FORBIDDEN\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}")))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<UserDto> update(@Parameter(description = "User ID") @PathVariable UUID id, @RequestBody UserDto userDto) throws UserNotFoundException, UserValidationException {
        User currentUser = userService.findById(securityService.getCurrentUserId());
        userDto.setId(id);
        User user = toEntity(userDto);
        User updated = userService.update(user, currentUser);
        return ResponseEntity.ok(toDto(updated));
    }

    @Operation(
            summary = "Удалить пользователя по ID",
            description = "Удаляет пользователя из системы по его ID. Доступ: только ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\",\"code\":\"ACCESS_FORBIDDEN\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"User with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"USER_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/users/{id}\"}")))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@Parameter(description = "User ID") @PathVariable UUID id) throws UserNotFoundException {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // Never return password in DTO
                user.getRole(),
                user.getNotificationDays(),
                user.getCreatedAt()
        );
    }

    private User toEntity(UserDto dto) {
        User user = new User();
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getNotificationDays() != null) {
            user.setNotificationDays(dto.getNotificationDays());
        }
        if (dto.getCreatedAt() != null) {
            user.setCreatedAt(dto.getCreatedAt());
        }
        return user;
    }
}
