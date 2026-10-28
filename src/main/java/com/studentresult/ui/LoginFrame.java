package com.studentresult.ui;

import com.studentresult.model.User;
import com.studentresult.service.AuthService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Administrator Login window with secure credential verification and
 * professional Navy Blue and White presentation.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame() {
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Administrator Login - Student Result Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());

        // Top Navy Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.NAVY_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel titleLabel = new JLabel("College Mini-Project", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_SUBTITLE);
        titleLabel.setForeground(new Color(200, 220, 245));

        JLabel mainTitle = new JLabel("Student Result System", SwingConstants.CENTER);
        mainTitle.setFont(UITheme.FONT_TITLE);
        mainTitle.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(mainTitle, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 25));
        centerWrapper.setBackground(UITheme.BG_LIGHT);

        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setPreferredSize(new Dimension(400, 320));
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel loginHeader = new JLabel("Administrator Sign In", SwingConstants.CENTER);
        loginHeader.setFont(UITheme.FONT_HEADER);
        loginHeader.setForeground(UITheme.TEXT_PRIMARY);
        cardPanel.add(loginHeader, gbc);

        // Error message label
        gbc.gridy++;
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.STATUS_FAIL_TXT);
        cardPanel.add(errorLabel, gbc);

        // Username Field
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(UITheme.FONT_BOLD);
        userLabel.setForeground(UITheme.TEXT_PRIMARY);
        cardPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = UITheme.createTextField(15);
        usernameField.setText("admin"); // pre-filled for convenience
        cardPanel.add(usernameField, gbc);

        // Password Field
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UITheme.FONT_BOLD);
        passLabel.setForeground(UITheme.TEXT_PRIMARY);
        cardPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(UITheme.FONT_REGULAR);
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 32));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_GREY, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        passwordField.setText("admin123"); // pre-filled for quick testing
        passwordField.addActionListener(e -> performLogin());
        cardPanel.add(passwordField, gbc);

        // Buttons Panel
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btnPanel.setOpaque(false);

        JButton loginBtn = UITheme.createStyledButton("Login to System", true);
        loginBtn.addActionListener(e -> performLogin());

        JButton exitBtn = UITheme.createStyledButton("Exit", false);
        exitBtn.addActionListener(e -> System.exit(0));

        btnPanel.add(loginBtn);
        btnPanel.add(exitBtn);
        cardPanel.add(btnPanel, gbc);

        // Default Credentials Info Box
        gbc.gridy++;
        JPanel hintBox = new JPanel(new BorderLayout());
        hintBox.setBackground(new Color(238, 242, 255));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(199, 210, 254), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JLabel hintLabel = new JLabel("<html><center><b>Default Admin:</b> username <i>admin</i> &bull; password <i>admin123</i></center></html>", SwingConstants.CENTER);
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(new Color(67, 56, 202));
        hintBox.add(hintLabel, BorderLayout.CENTER);
        cardPanel.add(hintBox, gbc);

        centerWrapper.add(cardPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // Footer
        JLabel footerLabel = new JLabel("College Mini-Project - Java Swing & SQLite Desktop Application", SwingConstants.CENTER);
        footerLabel.setFont(UITheme.FONT_SMALL);
        footerLabel.setForeground(UITheme.TEXT_MUTED);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 14, 0));
        add(footerLabel, BorderLayout.SOUTH);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user != null) {
                errorLabel.setText(" ");
                // Launch Main Application Window
                SwingUtilities.invokeLater(() -> {
                    MainFrame mainFrame = new MainFrame(user);
                    mainFrame.setVisible(true);
                    this.dispose();
                });
            } else {
                errorLabel.setText("Invalid username or password. Please try again.");
                passwordField.setText("");
            }
        } catch (SQLException ex) {
            errorLabel.setText("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

