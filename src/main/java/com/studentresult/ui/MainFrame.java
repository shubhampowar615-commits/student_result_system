package com.studentresult.ui;

import com.studentresult.model.User;
import com.studentresult.service.AuthService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Main Application Window providing top header banner, navy sidebar navigation,
 * and centralized CardLayout container for all subpanels.
 */
public class MainFrame extends JFrame {

    private final User currentUser;

    private CardLayout cardLayout;
    private JPanel centerCardPanel;

    private DashboardPanel dashboardPanel;
    private StudentPanel studentPanel;
    private SubjectPanel subjectPanel;
    private MarksEntryPanel marksEntryPanel;
    private ResultsPanel resultsPanel;
    private ReportCardPanel reportCardPanel;

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentCard = "DASHBOARD";

    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Student Result Management System - College Mini-Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 780);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());

        // 1. Top Header Bar (Deep Navy)
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.NAVY_DARK);
        topHeader.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        topHeader.setPreferredSize(new Dimension(getWidth(), 60));

        // Title and Institution branding
        JPanel brandingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandingPanel.setOpaque(false);

        JLabel appTitle = new JLabel("STUDENT RESULT MANAGEMENT SYSTEM");
        appTitle.setFont(UITheme.FONT_TITLE);
        appTitle.setForeground(Color.WHITE);

        JLabel tagBadge = new JLabel(" College Mini-Project ");
        tagBadge.setFont(UITheme.FONT_SMALL);
        tagBadge.setOpaque(true);
        tagBadge.setBackground(UITheme.ACCENT_BLUE);
        tagBadge.setForeground(Color.WHITE);
        tagBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        brandingPanel.add(appTitle);
        brandingPanel.add(tagBadge);
        topHeader.add(brandingPanel, BorderLayout.WEST);

        // User info & Logout button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        String displayName = (currentUser != null) ? currentUser.getFullName() : "Administrator";
        JLabel userLabel = new JLabel("Logged in as: " + displayName);
        userLabel.setFont(UITheme.FONT_REGULAR);
        userLabel.setForeground(new Color(224, 231, 255));

        JButton logoutBtn = UITheme.createStyledButton("Logout", false);
        logoutBtn.setPreferredSize(new Dimension(85, 30));
        logoutBtn.addActionListener(e -> performLogout());

        userPanel.add(userLabel);
        userPanel.add(logoutBtn);
        topHeader.add(userPanel, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // 2. Left Sidebar Navigation (Navy Primary)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.NAVY_PRIMARY);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JLabel menuHeading = new JLabel("NAVIGATION");
        menuHeading.setFont(UITheme.FONT_SMALL);
        menuHeading.setForeground(new Color(148, 163, 184));
        menuHeading.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 0));
        sidebar.add(menuHeading);

        // Navigation Buttons
        addNavButton(sidebar, "DASHBOARD", "1. Dashboard");
        addNavButton(sidebar, "STUDENTS", "2. Students");
        addNavButton(sidebar, "SUBJECTS", "3. Subjects");
        addNavButton(sidebar, "MARKS", "4. Marks Entry");
        addNavButton(sidebar, "RESULTS", "5. Results");
        addNavButton(sidebar, "REPORT", "6. Report Card");

        sidebar.add(Box.createVerticalGlue());

        // Sidebar Footer
        JPanel sidebarFooter = new JPanel(new GridLayout(2, 1, 2, 2));
        sidebarFooter.setOpaque(false);
        sidebarFooter.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel techLabel = new JLabel("Java 21 Swing & SQLite");
        techLabel.setFont(UITheme.FONT_SMALL);
        techLabel.setForeground(new Color(148, 163, 184));

        JLabel statusDot = new JLabel("System Online");
        statusDot.setFont(UITheme.FONT_SMALL);
        statusDot.setForeground(new Color(134, 239, 172)); // soft green

        sidebarFooter.add(techLabel);
        sidebarFooter.add(statusDot);
        sidebar.add(sidebarFooter);

        add(sidebar, BorderLayout.WEST);

        // 3. Center CardLayout Container
        cardLayout = new CardLayout();
        centerCardPanel = new JPanel(cardLayout);
        centerCardPanel.setBackground(UITheme.BG_LIGHT);

        dashboardPanel = new DashboardPanel(this);
        studentPanel = new StudentPanel(this);
        subjectPanel = new SubjectPanel(this);
        marksEntryPanel = new MarksEntryPanel(this);
        resultsPanel = new ResultsPanel(this);
        reportCardPanel = new ReportCardPanel(this);

        centerCardPanel.add(dashboardPanel, "DASHBOARD");
        centerCardPanel.add(studentPanel, "STUDENTS");
        centerCardPanel.add(subjectPanel, "SUBJECTS");
        centerCardPanel.add(marksEntryPanel, "MARKS");
        centerCardPanel.add(resultsPanel, "RESULTS");
        centerCardPanel.add(reportCardPanel, "REPORT");

        add(centerCardPanel, BorderLayout.CENTER);

        // 4. Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_GREY),
            BorderFactory.createEmptyBorder(4, 16, 4, 16)
        ));
        statusBar.setPreferredSize(new Dimension(getWidth(), 26));

        JLabel dbStatus = new JLabel("Connected to Database: student_results.db (SQLite) | Prepared Statements Enforced");
        dbStatus.setFont(UITheme.FONT_SMALL);
        dbStatus.setForeground(UITheme.TEXT_MUTED);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        JLabel dateStatus = new JLabel(today);
        dateStatus.setFont(UITheme.FONT_SMALL);
        dateStatus.setForeground(UITheme.TEXT_MUTED);

        statusBar.add(dbStatus, BorderLayout.WEST);
        statusBar.add(dateStatus, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // Highlight initial navigation
        highlightNavButton("DASHBOARD");
    }

    private void addNavButton(JPanel sidebar, String cardKey, String labelText) {
        JButton btn = new JButton(labelText);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(new Color(224, 231, 255));
        btn.setBackground(UITheme.NAVY_PRIMARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(196, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        btn.addActionListener(e -> navigateTo(cardKey));

        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
        navButtons.put(cardKey, btn);
    }

    public void navigateTo(String cardKey) {
        currentCard = cardKey;
        cardLayout.show(centerCardPanel, cardKey);
        highlightNavButton(cardKey);

        // Auto refresh relevant panels on visit
        switch (cardKey) {
            case "DASHBOARD" -> dashboardPanel.refreshData();
            case "STUDENTS"  -> studentPanel.loadStudents();
            case "SUBJECTS"  -> subjectPanel.loadSubjects();
            case "MARKS"     -> marksEntryPanel.loadStudentsForSemester();
            case "RESULTS"   -> resultsPanel.loadResults();
            case "REPORT"    -> reportCardPanel.loadStudentList();
        }
    }

    private void highlightNavButton(String activeKey) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equalsIgnoreCase(activeKey)) {
                btn.setBackground(UITheme.NAVY_HOVER);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.ACCENT_BLUE),
                    BorderFactory.createEmptyBorder(8, 12, 8, 16)
                ));
            } else {
                btn.setBackground(UITheme.NAVY_PRIMARY);
                btn.setForeground(new Color(224, 231, 255));
                btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            }
        }
    }

    public void openReportCardForStudent(int studentId, int semester) {
        navigateTo("REPORT");
        reportCardPanel.selectStudentAndSemester(studentId, semester);
    }

    public void openMarksEntryForStudent(int studentId, int semester) {
        navigateTo("MARKS");
        marksEntryPanel.selectStudentAndSemester(studentId, semester);
    }

    public void refreshDashboardMetrics() {
        dashboardPanel.refreshData();
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out of the system?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                this.dispose();
            });
        }
    }
}

