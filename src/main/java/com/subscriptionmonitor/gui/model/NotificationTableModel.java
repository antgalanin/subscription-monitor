package com.subscriptionmonitor.gui.model;

import com.subscriptionmonitor.dto.NotificationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "ID", "Subscription ID", "Дата уведомления", "Тип", "Отправлено", "Сообщение", "Создано", "Владелец"
    };

    private final Class<?>[] columnClasses = {
            String.class, String.class, String.class, String.class, Boolean.class, String.class, String.class, String.class
    };

    private List<NotificationWithUsername> notifications = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Getter
    @AllArgsConstructor
    public static class NotificationWithUsername {
        private final NotificationDto notification;
        private final String username;
    }

    @Override
    public int getRowCount() {
        return notifications.size();
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
        NotificationWithUsername item = notifications.get(rowIndex);
        NotificationDto notification = item.getNotification();

        return switch (columnIndex) {
            case 0 -> notification.getId() != null ? notification.getId().toString() : "";
            case 1 -> notification.getSubscriptionId() != null ? notification.getSubscriptionId().toString() : "";
            case 2 -> notification.getNotificationDate() != null ? notification.getNotificationDate().format(formatter) : "";
            case 3 -> notification.getType() != null ? notification.getType().toString() : "";
            case 4 -> notification.getIsSent();
            case 5 -> notification.getMessage();
            case 6 -> notification.getCreatedAt() != null ? notification.getCreatedAt().format(formatter) : "";
            case 7 -> item.getUsername() != null ? item.getUsername() : "";
            default -> null;
        };
    }

    public void setData(List<NotificationWithUsername> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        fireTableDataChanged();
    }

    public NotificationDto getNotificationAt(int row) {
        if (row >= 0 && row < notifications.size()) {
            return notifications.get(row).getNotification();
        }
        return null;
    }

    public void removeNotification(int row) {
        if (row >= 0 && row < notifications.size()) {
            notifications.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public void clear() {
        notifications.clear();
        fireTableDataChanged();
    }
}
