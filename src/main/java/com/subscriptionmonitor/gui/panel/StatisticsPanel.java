package com.subscriptionmonitor.gui.panel;

import com.subscriptionmonitor.dto.CategoryStatisticsDto;
import com.subscriptionmonitor.dto.UpcomingPaymentDto;
import com.subscriptionmonitor.dto.UserStatisticsDto;
import com.subscriptionmonitor.gui.util.RestClient;
import com.subscriptionmonitor.gui.util.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatisticsPanel extends JPanel {
    private RestClient restClient;
    private JProgressBar progressBar;
    private JComboBox<UserItem> userComboBox;

    private JLabel totalSubscriptionsLabel;
    private JLabel activeSubscriptionsLabel;
    private JLabel inactiveSubscriptionsLabel;
    private JLabel totalCostRubLabel;
    private JLabel totalCostUsdLabel;
    private JLabel totalCostEurLabel;

    private JTable upcomingPaymentsTable;
    private DefaultTableModel upcomingPaymentsModel;

    private JTable categoryDistributionTable;
    private DefaultTableModel categoryDistributionModel;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static class UserItem {
        private final com.subscriptionmonitor.dto.UserDto user;

        public UserItem(com.subscriptionmonitor.dto.UserDto user) {
            this.user = user;
        }

        public com.subscriptionmonitor.dto.UserDto getUser() {
            return user;
        }

        @Override
        public String toString() {
            return user.getUsername() + " (" + user.getEmail() + ")";
        }
    }

    public StatisticsPanel(RestClient restClient) {
        this.restClient = restClient;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTopPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Статистика");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(StyleUtils.TEXT_COLOR);
        leftTopPanel.add(titleLabel);

        if (restClient.isAdmin()) {
            JLabel userLabel = new JLabel("Пользователь:");
            userLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            userLabel.setForeground(StyleUtils.TEXT_SECONDARY);
            leftTopPanel.add(Box.createHorizontalStrut(20));
            leftTopPanel.add(userLabel);

            userComboBox = new JComboBox<>();
            userComboBox.setPreferredSize(new Dimension(280, 32));
            userComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
            userComboBox.addActionListener(e -> loadData());
            leftTopPanel.add(userComboBox);
            loadUsers();
        }

        topPanel.add(leftTopPanel, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));
        topPanel.add(progressBar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        JPanel summaryPanel = createSummaryPanel();
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        centerPanel.add(summaryPanel);

        JPanel paymentsPanel = createUpcomingPaymentsPanel();
        centerPanel.add(paymentsPanel);

        JPanel categoryPanel = createCategoryDistributionPanel();
        centerPanel.add(categoryPanel);

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 10, 15),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(StyleUtils.BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                )
        ));

        totalSubscriptionsLabel = new JLabel("0", SwingConstants.RIGHT);
        activeSubscriptionsLabel = new JLabel("0", SwingConstants.RIGHT);
        inactiveSubscriptionsLabel = new JLabel("0", SwingConstants.RIGHT);
        totalCostRubLabel = new JLabel("0.00", SwingConstants.RIGHT);
        totalCostUsdLabel = new JLabel("0.00", SwingConstants.RIGHT);
        totalCostEurLabel = new JLabel("0.00", SwingConstants.RIGHT);

        panel.add(createLabelPanel("Всего подписок:", totalSubscriptionsLabel, StyleUtils.PRIMARY_COLOR));
        panel.add(createLabelPanel("Активных:", activeSubscriptionsLabel, StyleUtils.SUCCESS_COLOR));
        panel.add(createLabelPanel("Неактивных:", inactiveSubscriptionsLabel, StyleUtils.SECONDARY_COLOR));
        panel.add(createLabelPanel("Расходы (RUB):", totalCostRubLabel, StyleUtils.WARNING_COLOR));
        panel.add(createLabelPanel("Расходы (USD):", totalCostUsdLabel, StyleUtils.WARNING_COLOR));
        panel.add(createLabelPanel("Расходы (EUR):", totalCostEurLabel, StyleUtils.WARNING_COLOR));

        return panel;
    }

    private JPanel createLabelPanel(String title, JLabel valueLabel, Color valueColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setBackground(StyleUtils.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(StyleUtils.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valueLabel.setForeground(valueColor);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUpcomingPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));

        JLabel sectionLabel = new JLabel("Предстоящие платежи");
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        sectionLabel.setForeground(StyleUtils.TEXT_COLOR);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(sectionLabel, BorderLayout.NORTH);

        String[] columnNames = {"Подписка", "Категория", "Стоимость", "Дата", "Срочность"};
        upcomingPaymentsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        upcomingPaymentsTable = new JTable(upcomingPaymentsModel);
        upcomingPaymentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        upcomingPaymentsTable.getTableHeader().setReorderingAllowed(false);
        StyleUtils.styleTable(upcomingPaymentsTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        upcomingPaymentsTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        upcomingPaymentsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        upcomingPaymentsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setOpaque(true);
                setFont(getFont().deriveFont(Font.BOLD));

                if (!isSelected && value != null) {
                    String urgency = value.toString();
                    switch (urgency) {
                        case "Просрочено":
                            setBackground(StyleUtils.URGENCY_OVERDUE);
                            setForeground(Color.WHITE);
                            break;
                        case "Сегодня":
                            setBackground(StyleUtils.URGENCY_TODAY);
                            setForeground(Color.BLACK);
                            break;
                        case "Завтра":
                            setBackground(StyleUtils.URGENCY_TOMORROW);
                            setForeground(Color.BLACK);
                            break;
                        case "Через 2 дня":
                            setBackground(StyleUtils.URGENCY_TWO_DAYS);
                            setForeground(Color.BLACK);
                            break;
                        case "Через 3 дня":
                            setBackground(StyleUtils.URGENCY_THREE_DAYS);
                            setForeground(Color.WHITE);
                            break;
                        case "На этой неделе":
                            setBackground(StyleUtils.URGENCY_WEEK);
                            setForeground(Color.WHITE);
                            break;
                        case "В этом месяце":
                            setBackground(StyleUtils.URGENCY_MONTH);
                            setForeground(Color.WHITE);
                            break;
                        case "Позже":
                            setBackground(StyleUtils.URGENCY_LATER);
                            setForeground(StyleUtils.TEXT_COLOR);
                            break;
                        default:
                            setBackground(Color.WHITE);
                            setForeground(Color.BLACK);
                            break;
                    }
                } else if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                }
                return this;
            }
        });

        upcomingPaymentsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        upcomingPaymentsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        upcomingPaymentsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        upcomingPaymentsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        upcomingPaymentsTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        upcomingPaymentsTable.setPreferredScrollableViewportSize(new Dimension(0, 100));

        JScrollPane scrollPane = StyleUtils.createStyledScrollPane(upcomingPaymentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCategoryDistributionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel sectionLabel = new JLabel("Распределение по категориям");
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        sectionLabel.setForeground(StyleUtils.TEXT_COLOR);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(sectionLabel, BorderLayout.NORTH);

        String[] columnNames = {"Категория", "Активных", "Расходы (RUB)", "Расходы (USD)", "Расходы (EUR)"};
        categoryDistributionModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        categoryDistributionTable = new JTable(categoryDistributionModel);
        categoryDistributionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryDistributionTable.getTableHeader().setReorderingAllowed(false);
        StyleUtils.styleTable(categoryDistributionTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        categoryDistributionTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        categoryDistributionTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        categoryDistributionTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        categoryDistributionTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        categoryDistributionTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        categoryDistributionTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        categoryDistributionTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        categoryDistributionTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        categoryDistributionTable.getColumnModel().getColumn(4).setPreferredWidth(150);

        categoryDistributionTable.setPreferredScrollableViewportSize(new Dimension(0, 100));

        JScrollPane scrollPane = StyleUtils.createStyledScrollPane(categoryDistributionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadUsers() {
        SwingWorker<List<com.subscriptionmonitor.dto.UserDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<com.subscriptionmonitor.dto.UserDto> doInBackground() throws Exception {
                return restClient.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<com.subscriptionmonitor.dto.UserDto> users = get();
                    userComboBox.removeAllItems();

                    java.util.UUID currentUserId = restClient.getCurrentUserId();
                    UserItem currentUserItem = null;

                    for (com.subscriptionmonitor.dto.UserDto user : users) {
                        UserItem item = new UserItem(user);
                        userComboBox.addItem(item);
                        if (user.getId().equals(currentUserId)) {
                            currentUserItem = item;
                        }
                    }

                    if (currentUserItem != null) {
                        userComboBox.setSelectedItem(currentUserItem);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StatisticsPanel.this,
                            "Ошибка загрузки пользователей: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    public void loadData() {
        progressBar.setVisible(true);
        if (userComboBox != null) {
            userComboBox.setEnabled(false);
        }

        SwingWorker<DataHolder, Void> worker = new SwingWorker<>() {
            @Override
            protected DataHolder doInBackground() throws Exception {
                java.util.UUID userId;
                if (restClient.isAdmin() && userComboBox != null && userComboBox.getSelectedItem() != null) {
                    UserItem selectedUser = (UserItem) userComboBox.getSelectedItem();
                    userId = selectedUser.getUser().getId();
                } else {
                    userId = restClient.getCurrentUserId();
                }

                UserStatisticsDto statistics = restClient.getUserStatistics(userId);
                List<UpcomingPaymentDto> payments = restClient.getUpcomingPayments(userId);
                List<CategoryStatisticsDto> categories = restClient.getCategoryStatistics(userId);
                return new DataHolder(statistics, payments, categories);
            }

            @Override
            protected void done() {
                try {
                    DataHolder data = get();
                    updateStatistics(data.statistics);
                    updateUpcomingPayments(data.payments);
                    updateCategoryDistribution(data.categories);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StatisticsPanel.this,
                            "Ошибка загрузки данных: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    if (userComboBox != null) {
                        userComboBox.setEnabled(true);
                    }
                }
            }
        };

        worker.execute();
    }

    private void updateStatistics(UserStatisticsDto statistics) {
        totalSubscriptionsLabel.setText(String.valueOf(statistics.getTotalSubscriptions()));
        activeSubscriptionsLabel.setText(String.valueOf(statistics.getActiveSubscriptions()));
        inactiveSubscriptionsLabel.setText(String.valueOf(statistics.getInactiveSubscriptions()));
        totalCostRubLabel.setText(formatMoney(statistics.getTotalCostRub()));
        totalCostUsdLabel.setText(formatMoney(statistics.getTotalCostUsd()));
        totalCostEurLabel.setText(formatMoney(statistics.getTotalCostEur()));
    }

    private void updateUpcomingPayments(List<UpcomingPaymentDto> payments) {
        upcomingPaymentsModel.setRowCount(0);
        for (UpcomingPaymentDto payment : payments) {
            upcomingPaymentsModel.addRow(new Object[]{
                    payment.getSubscriptionName(),
                    payment.getCategoryName(),
                    formatMoney(payment.getCost()) + " " + payment.getCurrency(),
                    payment.getNextBillingDate().format(dateFormatter),
                    translateUrgency(payment.getPaymentUrgency())
            });
        }
    }

    private void updateCategoryDistribution(List<CategoryStatisticsDto> categories) {
        categoryDistributionModel.setRowCount(0);
        for (CategoryStatisticsDto category : categories) {
            categoryDistributionModel.addRow(new Object[]{
                    category.getCategoryName(),
                    category.getActiveSubscriptions(),
                    formatMoney(category.getTotalCostRub()),
                    formatMoney(category.getTotalCostUsd()),
                    formatMoney(category.getTotalCostEur())
            });
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%.2f", amount);
    }

    private String translateUrgency(String urgency) {
        return switch (urgency) {
            case "Overdue" -> "Просрочено";
            case "Today" -> "Сегодня";
            case "Tomorrow" -> "Завтра";
            case "In 2 Days" -> "Через 2 дня";
            case "In 3 Days" -> "Через 3 дня";
            case "This Week" -> "На этой неделе";
            case "This Month" -> "В этом месяце";
            case "Later" -> "Позже";
            default -> urgency;
        };
    }

    private static class DataHolder {
        UserStatisticsDto statistics;
        List<UpcomingPaymentDto> payments;
        List<CategoryStatisticsDto> categories;

        DataHolder(UserStatisticsDto statistics, List<UpcomingPaymentDto> payments, List<CategoryStatisticsDto> categories) {
            this.statistics = statistics;
            this.payments = payments;
            this.categories = categories;
        }
    }
}
