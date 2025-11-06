package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание нового пользователя")
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Имя пользователя для входа в систему", example = "john_doe", required = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email адрес пользователя", example = "john@example.com", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Пароль пользователя", example = "securePassword123", required = true)
    private String password;

    @NotNull(message = "Role is required")
    @Schema(description = "Роль пользователя в системе", example = "USER", allowableValues = {"USER", "ADMIN"}, required = true)
    private UserRole role;

    @Schema(description = "За сколько дней до списания отправлять уведомления", example = "3", defaultValue = "3")
    private Integer notificationDays;
}