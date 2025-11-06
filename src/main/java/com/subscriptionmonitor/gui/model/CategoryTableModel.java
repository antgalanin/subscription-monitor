package com.subscriptionmonitor.gui.model;

import com.subscriptionmonitor.dto.CategoryDto;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CategoryTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "ID", "Название", "Тип", "Создатель (User ID)", "Владелец", "Подписки", "Создана"
    };

    private final Class<?>[] columnClasses = {
            String.class, String.class, String.class, String.class, String.class, Integer.class, String.class
    };

    private List<CategoryWithOwner> categories = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static class CategoryWithOwner {
        private final CategoryDto category;
        private final String ownerUsername;
        private final int subscriptionCount;

        public CategoryWithOwner(CategoryDto category, String ownerUsername) {
            this(category, ownerUsername, 0);
        }

        public CategoryWithOwner(CategoryDto category, String ownerUsername, int subscriptionCount) {
            this.category = category;
            this.ownerUsername = ownerUsername;
            this.subscriptionCount = subscriptionCount;
        }

        public CategoryDto getCategory() {
            return category;
        }

        public String getOwnerUsername() {
            return ownerUsername;
        }

        public int getSubscriptionCount() {
            return subscriptionCount;
        }
    }

    @Override
    public int getRowCount() {
        return categories.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CategoryWithOwner item = categories.get(rowIndex);
        CategoryDto category = item.getCategory();

        return switch (columnIndex) {
            case 0 -> category.getId() != null ? category.getId().toString() : "";
            case 1 -> category.getName();
            case 2 -> category.getType() != null ? category.getType().toString() : "";
            case 3 -> category.getCreatedByUserId() != null ? category.getCreatedByUserId().toString() : "Системная";
            case 4 -> item.getOwnerUsername() != null ? item.getOwnerUsername() : "";
            case 5 -> item.getSubscriptionCount();
            case 6 -> category.getCreatedAt() != null ? category.getCreatedAt().format(formatter) : "";
            default -> null;
        };
    }

    public void setData(List<CategoryWithOwner> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        fireTableDataChanged();
    }

    public CategoryDto getCategoryAt(int row) {
        if (row >= 0 && row < categories.size()) {
            return categories.get(row).getCategory();
        }
        return null;
    }

    public void addCategory(CategoryWithOwner category) {
        categories.add(category);
        fireTableRowsInserted(categories.size() - 1, categories.size() - 1);
    }

    public void updateCategory(int row, CategoryWithOwner category) {
        if (row >= 0 && row < categories.size()) {
            categories.set(row, category);
            fireTableRowsUpdated(row, row);
        }
    }

    public void removeCategory(int row) {
        if (row >= 0 && row < categories.size()) {
            categories.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public void clear() {
        categories.clear();
        fireTableDataChanged();
    }
}
