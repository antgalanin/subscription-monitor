package com.subscriptionmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление email адреса пользователя")
public class UpdateEmailRequest {
    @Schema(description = "Новый email адрес пользователя", example = "newemail@example.com", required = true)
    private String email;
}
