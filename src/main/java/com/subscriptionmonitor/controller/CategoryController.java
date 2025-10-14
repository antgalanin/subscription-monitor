package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.CategoryDto;
import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody CategoryDto categoryDto) {
        Category category = toEntity(categoryDto);
        Category created = categoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable UUID id) {
        return categoryService.findById(id)
                .map(category -> ResponseEntity.ok(toDto(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll() {
        List<CategoryDto> categories = categoryService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CategoryDto>> getByType(@PathVariable CategoryType type) {
        List<CategoryDto> categories = categoryService.findByType(type).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CategoryDto>> getByUserId(@PathVariable UUID userId) {
        List<CategoryDto> categories = categoryService.findByCreatedByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/type/{type}/user/{userId}")
    public ResponseEntity<List<CategoryDto>> getByTypeAndUserId(
            @PathVariable CategoryType type,
            @PathVariable UUID userId) {
        List<CategoryDto> categories = categoryService.findByTypeAndCreatedByUserId(type, userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable UUID id, @RequestBody CategoryDto categoryDto) {
        return categoryService.findById(id)
                .map(existing -> {
                    categoryDto.setId(id);
                    Category category = toEntity(categoryDto);
                    Category updated = categoryService.update(category);
                    return ResponseEntity.ok(toDto(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getCreatedByUserId(),
                category.getCreatedAt()
        );
    }

    private Category toEntity(CategoryDto dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setCreatedByUserId(dto.getCreatedByUserId());
        category.setCreatedAt(dto.getCreatedAt());
        return category;
    }
}
