package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление данных пользователя")
public class UpdateUserRequest {

    @Email(message = "Email must be valid")
    @Schema(description = "Email адрес пользователя", example = "john.new@example.com")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Новый пароль пользователя (если нужно изменить)", example = "newSecurePassword123")
    private String password;

    @Schema(description = "Роль пользователя в системе (только для ADMIN)", example = "USER", allowableValues = {"USER", "ADMIN"})
    private UserRole role;

    @Schema(description = "За сколько дней до списания отправлять уведомления", example = "5")
    private Integer notificationDays;
}