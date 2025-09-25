package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.CategoryType;

public class Category extends BaseEntity {
    private String name;
    private CategoryType type;
    private Long createdByUserId;

    public Category() {
        super();
        this.type = CategoryType.CUSTOM;
    }

    public Category(String name) {
        super();
        this.name = name;
        this.type = CategoryType.CUSTOM;
    }

    public Category(String name, CategoryType type, Long createdByUserId) {
        super();
        this.name = name;
        this.type = type;
        this.createdByUserId = createdByUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + getId() +
                ", uuid=" + getUuid() +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", createdByUserId=" + createdByUserId +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}