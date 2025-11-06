package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO категории подписок")
public class CategoryDto {
    @Schema(description = "Уникальный идентификатор категории", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Название категории", example = "Стриминговые сервисы", required = true)
    private String name;

    @Schema(description = "Тип категории (SYSTEM - системная, CUSTOM - пользовательская, LEGACY - устаревшая)", example = "CUSTOM", allowableValues = {"SYSTEM", "CUSTOM", "LEGACY"})
    private CategoryType type;

    @Schema(description = "ID пользователя, создавшего категорию (null для системных)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID createdByUserId;

    @Schema(description = "Дата и время создания категории", example = "2025-10-20T15:30:00")
    private LocalDateTime createdAt;
}
