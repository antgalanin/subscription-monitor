package com.subscriptionmonitor.gui.model;

import com.subscriptionmonitor.dto.PaymentDto;
import com.subscriptionmonitor.dto.SubscriptionDto;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "ID", "Название", "User ID", "Category ID", "Payment ID",
            "След. списание", "Период (дн.)", "Сумма", "Валюта", "Активна", "Владелец", "Создана"
    };

    private final Class<?>[] columnClasses = {
            String.class, String.class, String.class, String.class, String.class,
            String.class, Integer.class, String.class, String.class, Boolean.class, String.class, String.class
    };

    private List<SubscriptionWithPayment> subscriptions = new ArrayList<>();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static class SubscriptionWithPayment {
        private final SubscriptionDto subscription;
        private final PaymentDto payment;
        private final String username;

        public SubscriptionWithPayment(SubscriptionDto subscription, PaymentDto payment, String username) {
            this.subscription = subscription;
            this.payment = payment;
            this.username = username;
        }

        public SubscriptionDto getSubscription() {
            return subscription;
        }

        public PaymentDto getPayment() {
            return payment;
        }

        public String getUsername() {
            return username;
        }
    }

    @Override
    public int getRowCount() {
        return subscriptions.size();
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
        SubscriptionWithPayment item = subscriptions.get(rowIndex);
        SubscriptionDto subscription = item.getSubscription();
        PaymentDto payment = item.getPayment();

        return switch (columnIndex) {
            case 0 -> subscription.getId() != null ? subscription.getId().toString() : "";
            case 1 -> subscription.getName();
            case 2 -> subscription.getUserId() != null ? subscription.getUserId().toString() : "";
            case 3 -> subscription.getCategoryId() != null ? subscription.getCategoryId().toString() : "";
            case 4 -> subscription.getPaymentId() != null ? subscription.getPaymentId().toString() : "";
            case 5 -> payment != null && payment.getNextBillingDate() != null
                ? payment.getNextBillingDate().format(dateFormatter) : "";
            case 6 -> payment != null ? payment.getBillingPeriodDays() : 0;
            case 7 -> payment != null && payment.getCost() != null ? payment.getCost().toString() : "";
            case 8 -> payment != null && payment.getCurrency() != null ? payment.getCurrency().toString() : "";
            case 9 -> subscription.getIsActive();
            case 10 -> item.getUsername() != null ? item.getUsername() : "";
            case 11 -> subscription.getCreatedAt() != null ? subscription.getCreatedAt().format(dateTimeFormatter) : "";
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 9;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 9 && value instanceof Boolean) {
            SubscriptionWithPayment item = subscriptions.get(rowIndex);
            item.getSubscription().setIsActive((Boolean) value);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public void setData(List<SubscriptionWithPayment> subscriptions) {
        this.subscriptions = subscriptions != null ? subscriptions : new ArrayList<>();
        fireTableDataChanged();
    }

    public SubscriptionDto getSubscriptionAt(int row) {
        if (row >= 0 && row < subscriptions.size()) {
            return subscriptions.get(row).getSubscription();
        }
        return null;
    }

    public SubscriptionWithPayment getSubscriptionWithPaymentAt(int row) {
        if (row >= 0 && row < subscriptions.size()) {
            return subscriptions.get(row);
        }
        return null;
    }

    public void addSubscription(SubscriptionWithPayment item) {
        subscriptions.add(item);
        fireTableRowsInserted(subscriptions.size() - 1, subscriptions.size() - 1);
    }

    public void updateSubscription(int row, SubscriptionWithPayment item) {
        if (row >= 0 && row < subscriptions.size()) {
            subscriptions.set(row, item);
            fireTableRowsUpdated(row, row);
        }
    }

    public void removeSubscription(int row) {
        if (row >= 0 && row < subscriptions.size()) {
            subscriptions.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public void clear() {
        subscriptions.clear();
        fireTableDataChanged();
    }
}
