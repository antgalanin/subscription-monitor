package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.UserDto;
import com.subscriptionmonitor.exception.validation.UserValidationException;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации и регистрации (публичные endpoints)")
public class AuthController {

    private final UserService userService;

    @Operation(
        summary = "Регистрация нового пользователя",
        description = "Публичный endpoint для регистрации. Не требует аутентификации. Автоматически создает пользователя с ролью USER."
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
