package com.subscriptionmonitor.gui.model;

import com.subscriptionmonitor.dto.UserDto;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "ID", "Логин", "Email", "Роль", "Дней до уведомления", "Создан"
    };

    private final Class<?>[] columnClasses = {
            String.class, String.class, String.class, String.class, Integer.class, String.class
    };

    private List<UserDto> users = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public int getRowCount() {
        return users.size();
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
        UserDto user = users.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> user.getId() != null ? user.getId().toString() : "";
            case 1 -> user.getUsername();
            case 2 -> user.getEmail();
            case 3 -> user.getRole() != null ? user.getRole().toString() : "";
            case 4 -> user.getNotificationDays();
            case 5 -> user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "";
            default -> null;
        };
    }

    public void setData(List<UserDto> users) {
        this.users = users != null ? users : new ArrayList<>();
        fireTableDataChanged();
    }

    public UserDto getUserAt(int row) {
        if (row >= 0 && row < users.size()) {
            return users.get(row);
        }
        return null;
    }

    public void addUser(UserDto user) {
        users.add(user);
        fireTableRowsInserted(users.size() - 1, users.size() - 1);
    }

    public void updateUser(int row, UserDto user) {
        if (row >= 0 && row < users.size()) {
            users.set(row, user);
            fireTableRowsUpdated(row, row);
        }
    }

    public void removeUser(int row) {
        if (row >= 0 && row < users.size()) {
            users.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public void clear() {
        users.clear();
        fireTableDataChanged();
    }
}
