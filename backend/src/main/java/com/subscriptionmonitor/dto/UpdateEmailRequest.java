package com.subscriptionmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление email адреса пользователя")
public class UpdateEmailRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Новый email адрес пользователя", example = "newemail@example.com", required = true)
    private String email;
}
