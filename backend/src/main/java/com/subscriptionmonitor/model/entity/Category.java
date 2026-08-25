package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type = CategoryType.CUSTOM;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    public Category(String name) {
        super();
        this.name = name;
        this.type = CategoryType.CUSTOM;
    }

    public Category(String name, CategoryType type, UUID createdByUserId) {
        super();
        this.name = name;
        this.type = type;
        this.createdByUserId = createdByUserId;
    }
}
