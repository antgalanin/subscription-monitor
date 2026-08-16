package com.subscriptionmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на вход в систему")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Имя пользователя", example = "john_doe", required = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Пароль пользователя", example = "securePassword123", required = true)
    private String password;
}
