package com.subscriptionmonitor.gui.dialog;

import com.subscriptionmonitor.gui.util.StyleUtils;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {

    public AboutDialog(JFrame parent) {
        super(parent, "О программе", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(600, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(35, 40, 20, 40));

        JLabel titleLabel = new JLabel("Subscription Monitor");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(StyleUtils.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createVerticalStrut(8));

        JLabel versionLabel = new JLabel("Версия 1.0");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        versionLabel.setForeground(StyleUtils.TEXT_SECONDARY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(versionLabel);

        mainPanel.add(Box.createVerticalStrut(25));

        JLabel descriptionLabel = new JLabel("Система мониторинга персональных подписок");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        descriptionLabel.setForeground(StyleUtils.TEXT_COLOR);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(descriptionLabel);

        mainPanel.add(Box.createVerticalStrut(25));

        JPanel techPanel = createInfoPanel();
        techPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(techPanel);

        mainPanel.add(Box.createVerticalStrut(20));

        JLabel projectLabel = new JLabel("Курсовая работа ПКП 2025");
        projectLabel.setFont(new Font("Arial", Font.BOLD, 13));
        projectLabel.setForeground(StyleUtils.TEXT_COLOR);
        projectLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(projectLabel);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 0, 15, 0)
        ));

        JButton okButton = StyleUtils.createPrimaryButton("OK");
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.addActionListener(e -> dispose());
        buttonPanel.add(okButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);
        outerPanel.setMaximumSize(new Dimension(520, 220));
        outerPanel.setPreferredSize(new Dimension(520, 220));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtils.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel techTitle = new JLabel("Технологии:");
        techTitle.setFont(new Font("Arial", Font.BOLD, 14));
        techTitle.setForeground(StyleUtils.TEXT_COLOR);
        techTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(techTitle);

        panel.add(Box.createVerticalStrut(12));

        String[] technologies = {
                "• Java 24 + Swing",
                "• FlatLaf Look and Feel",
                "• Spring Boot 3.4.5 + REST API",
                "• PostgreSQL 17",
                "• Spring Security",
                "• Swagger/OpenAPI"
        };

        for (String tech : technologies) {
            JLabel techLabel = new JLabel(tech);
            techLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            techLabel.setForeground(StyleUtils.TEXT_SECONDARY);
            techLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(techLabel);
            panel.add(Box.createVerticalStrut(6));
        }

        outerPanel.add(panel, BorderLayout.CENTER);
        return outerPanel;
    }
}
