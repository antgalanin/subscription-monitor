package com.subscriptionmonitor.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.subscriptionmonitor.gui.dialog.AboutDialog;
import com.subscriptionmonitor.gui.dialog.LoginDialog;
import com.subscriptionmonitor.gui.panel.CategoryPanel;
import com.subscriptionmonitor.gui.panel.NotificationPanel;
import com.subscriptionmonitor.gui.panel.ProfilePanel;
import com.subscriptionmonitor.gui.panel.StatisticsPanel;
import com.subscriptionmonitor.gui.panel.SubscriptionPanel;
import com.subscriptionmonitor.gui.panel.UserPanel;
import com.subscriptionmonitor.gui.util.RestClient;

import javax.swing.*;
import java.awt.*;

public class SubscriptionMonitorGUI extends JFrame {
    private RestClient restClient;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private SubscriptionPanel subscriptionPanel;
    private StatisticsPanel statisticsPanel;
    private CategoryPanel categoryPanel;
    private NotificationPanel notificationPanel;
    private ProfilePanel profilePanel;
    private UserPanel userPanel;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            SubscriptionMonitorGUI app = new SubscriptionMonitorGUI();
            app.setVisible(true);
        });
    }

    public SubscriptionMonitorGUI() {
        restClient = new RestClient("http://localhost:8080");

        setTitle("Subscription Monitor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        if (!showLoginDialog()) {
            System.exit(0);
        }

        initComponents();
        updateTitle();
    }

    private boolean showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(this, restClient);
        loginDialog.setVisible(true);
        return loginDialog.isAuthenticated();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        createMenuBar();

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        subscriptionPanel = new SubscriptionPanel(restClient, this);
        tabbedPane.addTab("Подписки", subscriptionPanel);

        statisticsPanel = new StatisticsPanel(restClient);
        tabbedPane.addTab("Статистика", statisticsPanel);

        categoryPanel = new CategoryPanel(restClient, this);
        tabbedPane.addTab("Категории", categoryPanel);

        notificationPanel = new NotificationPanel(restClient);
        tabbedPane.addTab("Уведомления", notificationPanel);

        profilePanel = new ProfilePanel(restClient);
        tabbedPane.addTab("Профиль", profilePanel);

        if (restClient.isAdmin()) {
            userPanel = new UserPanel(restClient, this);
            tabbedPane.addTab("Пользователи", userPanel);
        }

        add(tabbedPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        statusPanel.setBackground(new Color(250, 250, 250));

        String roleText = restClient.isAdmin() ? "Администратор" : "Пользователь";
        statusLabel = new JLabel(restClient.getCurrentUsername() + " • " + roleText);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(80, 80, 80));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Выход");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 13));
        logoutButton.setPreferredSize(new Dimension(80, 30));
        logoutButton.setBackground(new Color(244, 67, 54));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.addActionListener(e -> handleLogout());
        statusPanel.add(logoutButton, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите выйти из системы?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            restClient.logout();
            dispose();

            SwingUtilities.invokeLater(() -> {
                SubscriptionMonitorGUI newApp = new SubscriptionMonitorGUI();
                newApp.setVisible(true);
            });
        }
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu aboutMenu = new JMenu("О программе");
        aboutMenu.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent e) {
                showAboutDialog();
            }

            @Override
            public void menuDeselected(javax.swing.event.MenuEvent e) {}

            @Override
            public void menuCanceled(javax.swing.event.MenuEvent e) {}
        });

        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);
    }

    public void refreshSubscriptionRelatedData() {
        subscriptionPanel.loadData();
        statisticsPanel.loadData();
        notificationPanel.loadData();
    }

    public void refreshCategoryRelatedData() {
        categoryPanel.loadData();
        subscriptionPanel.loadData();
        statisticsPanel.loadData();
    }

    public void refreshUserRelatedData() {
        if (userPanel != null) {
            userPanel.loadData();
        }
        statisticsPanel.loadData();
    }

    public void refreshStatistics() {
        statisticsPanel.loadData();
    }

    public void refreshNotifications() {
        notificationPanel.loadData();
    }

    private void showAboutDialog() {
        AboutDialog dialog = new AboutDialog(this);
        dialog.setVisible(true);
    }

    private void updateTitle() {
        String role = restClient.isAdmin() ? "ADMIN" : "USER";
        setTitle("Subscription Monitor - " + restClient.getCurrentUsername() + " [" + role + "]");
    }
}
