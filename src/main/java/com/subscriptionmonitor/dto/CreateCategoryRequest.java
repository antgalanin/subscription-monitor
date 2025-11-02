package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание новой категории")
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    @Schema(description = "Название категории", example = "Стриминговые сервисы", required = true)
    private String name;

    @NotNull(message = "Category type is required")
    @Schema(description = "Тип категории (SYSTEM - системная, CUSTOM - пользовательская)", example = "CUSTOM", allowableValues = {"SYSTEM", "CUSTOM"}, required = true)
    private CategoryType type;
}
