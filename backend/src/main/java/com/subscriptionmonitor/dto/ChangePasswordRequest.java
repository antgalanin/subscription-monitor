package com.subscriptionmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на смену пароля пользователя")
public class ChangePasswordRequest {
    @Schema(description = "Текущий пароль пользователя", example = "oldPassword123", required = true)
    private String currentPassword;

    @Schema(description = "Новый пароль пользователя", example = "newSecurePassword456", required = true)
    private String newPassword;
}
