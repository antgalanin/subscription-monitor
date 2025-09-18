package com.subscriptionmonitor.model.entity;

public class Category extends BaseEntity {
    private String name;
    private Boolean isDefault;
    private Long createdByUserId;

    public Category() {
        super();
        this.isDefault = false;
    }

    public Category(String name) {
        super();
        this.name = name;
        this.isDefault = false;
    }

    public Category(String name, Boolean isDefault, Long createdByUserId) {
        super();
        this.name = name;
        this.isDefault = isDefault;
        this.createdByUserId = createdByUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
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
                ", name='" + name + '\'' +
                ", isDefault=" + isDefault +
                ", createdByUserId=" + createdByUserId +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}