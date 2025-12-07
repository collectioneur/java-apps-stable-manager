// src/ui/LoginFrame.java
package pl.agh.lab.ui;

import pl.agh.lab.service.StableService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final StableService service;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton adminButton;
    private JButton clientButton;

    public LoginFrame(StableService service) {
        this.service = service;
        initUI();
    }

    private void initUI() {
        setTitle("Stable Manager – Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 280);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(245, 247, 250));
        setContentPane(background);

        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 230));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Stable Manager");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitle = new JLabel("Sign in as admin or client");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(new Color(110, 110, 110));

        usernameField = new JTextField();
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));

        passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        adminButton = new JButton("Login as admin");
        clientButton = new JButton("Login as client");

        adminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clientButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        adminButton.addActionListener(e -> openAdmin());
        clientButton.addActionListener(e -> openClient());

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(16));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(8));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(16));
        card.add(adminButton);
        card.add(Box.createVerticalStrut(6));
        card.add(clientButton);

        background.add(card);
    }

    private void openAdmin() {
        SwingUtilities.invokeLater(() -> {
            AdminFrame frame = new AdminFrame(service);
            frame.setVisible(true);
        });
        dispose();
    }

    private void openClient() {
        SwingUtilities.invokeLater(() -> {
            ClientFrame frame = new ClientFrame(service);
            frame.setVisible(true);
        });
        dispose();
    }
}
