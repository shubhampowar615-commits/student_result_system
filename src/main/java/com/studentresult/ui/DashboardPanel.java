package com.studentresult.ui;

import com.studentresult.model.ResultSummary;
import com.studentresult.service.ResultService;
import com.studentresult.service.StudentService;
import com.studentresult.service.SubjectService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Dashboard Panel presenting overall institution statistics, key metrics,
 * quick navigation actions, and recent semester results.
 */
public class DashboardPanel extends JPanel {

    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ResultService resultService;
    private final MainFrame mainFrame;

    private JLabel studentCountLabel;
    private JLabel subjectCountLabel;
    private JLabel resultCountLabel;
    private JLabel passCountLabel;
    private JLabel failCountLabel;
    private JLabel incompleteCountLabel;

    private JTable recentResultsTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.studentService = new StudentService();
        this.subjectService = new SubjectService();
        this.resultService = new ResultService();

        initComponents();
        refreshData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Section: Title and Refresh Button
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleGrp = new JPanel();
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));
        titleGrp.setOpaque(false);

        JLabel title = new JLabel("System Dashboard");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Overview of students, semester subjects, and examination evaluations");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        titleGrp.add(title);
        titleGrp.add(Box.createVerticalStrut(3));
        titleGrp.add(subtitle);

        JButton refreshBtn = UITheme.createStyledButton("Refresh Metrics", false);
        refreshBtn.addActionListener(e -> refreshData());

        topHeader.add(titleGrp, BorderLayout.WEST);
        topHeader.add(refreshBtn, BorderLayout.EAST);

        // Center Content Container
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);

        // 1. Metric Cards Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 6, 12, 12));
        metricsGrid.setOpaque(false);
        metricsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        studentCountLabel = new JLabel("0");
        subjectCountLabel = new JLabel("0");
        resultCountLabel = new JLabel("0");
        passCountLabel = new JLabel("0");
        failCountLabel = new JLabel("0");
        incompleteCountLabel = new JLabel("0");

        metricsGrid.add(createCustomMetricCard("Total Students", studentCountLabel, UITheme.NAVY_PRIMARY));
        metricsGrid.add(createCustomMetricCard("Subjects", subjectCountLabel, UITheme.ACCENT_BLUE));
        metricsGrid.add(createCustomMetricCard("Evaluations", resultCountLabel, new Color(79, 70, 229)));
        metricsGrid.add(createCustomMetricCard("Passed", passCountLabel, UITheme.STATUS_PASS_TXT));
        metricsGrid.add(createCustomMetricCard("Failed", failCountLabel, UITheme.STATUS_FAIL_TXT));
        metricsGrid.add(createCustomMetricCard("Incomplete", incompleteCountLabel, UITheme.STATUS_INC_TXT));

        contentContainer.add(metricsGrid);
        contentContainer.add(Box.createVerticalStrut(18));

        // 2. Quick Actions Card
        JPanel quickActionsCard = UITheme.createCardPanel();
        quickActionsCard.setLayout(new BorderLayout(8, 8));
        quickActionsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JLabel qaLabel = new JLabel("Quick Actions");
        qaLabel.setFont(UITheme.FONT_HEADER);
        qaLabel.setForeground(UITheme.TEXT_PRIMARY);
        quickActionsCard.add(qaLabel, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        btnRow.setOpaque(false);

        JButton addStudentBtn = UITheme.createStyledButton("+ Add Student", true);
        addStudentBtn.addActionListener(e -> mainFrame.navigateTo("STUDENTS"));

        JButton enterMarksBtn = UITheme.createStyledButton("Enter Marks", true);
        enterMarksBtn.addActionListener(e -> mainFrame.navigateTo("MARKS"));

        JButton viewResultsBtn = UITheme.createStyledButton("View All Results", false);
        viewResultsBtn.addActionListener(e -> mainFrame.navigateTo("RESULTS"));

        JButton reportCardBtn = UITheme.createStyledButton("Print Report Card", false);
        reportCardBtn.addActionListener(e -> mainFrame.navigateTo("REPORT"));

        btnRow.add(addStudentBtn);
        btnRow.add(enterMarksBtn);
        btnRow.add(viewResultsBtn);
        btnRow.add(reportCardBtn);
        quickActionsCard.add(btnRow, BorderLayout.CENTER);

        contentContainer.add(quickActionsCard);
        contentContainer.add(Box.createVerticalStrut(18));

        // 3. Recent Results Table Card
        JPanel recentCard = UITheme.createCardPanel();
        recentCard.setLayout(new BorderLayout(10, 10));

        JLabel tableTitle = new JLabel("Recent Evaluated Results");
        tableTitle.setFont(UITheme.FONT_HEADER);
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);
        recentCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Roll Number", "Student Name", "Semester", "Total Marks", "Max Marks", "Percentage", "Grade", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentResultsTable = new JTable(tableModel);
        UITheme.styleTable(recentResultsTable);

        JScrollPane scrollPane = new JScrollPane(recentResultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GREY));
        recentCard.add(scrollPane, BorderLayout.CENTER);

        contentContainer.add(recentCard);

        add(topHeader, BorderLayout.NORTH);
        add(contentContainer, BorderLayout.CENTER);
    }

    private JPanel createCustomMetricCard(String title, JLabel valLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                g.setColor(accentColor);
                g.fillRect(0, 0, getWidth(), 4);
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_GREY, 1),
            BorderFactory.createEmptyBorder(10, 14, 12, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_MUTED);

        valLabel.setFont(UITheme.FONT_METRIC_NUM);
        valLabel.setForeground(UITheme.TEXT_PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        try {
            int students = studentService.getTotalStudentCount();
            int subjects = subjectService.getTotalSubjectCount();
            int results = resultService.getTotalResultsCount();
            int passed = resultService.getPassedResultsCount();
            int failed = resultService.getFailedResultsCount();
            int incomplete = resultService.getIncompleteResultsCount();

            studentCountLabel.setText(String.valueOf(students));
            subjectCountLabel.setText(String.valueOf(subjects));
            resultCountLabel.setText(String.valueOf(results));
            passCountLabel.setText(String.valueOf(passed));
            failCountLabel.setText(String.valueOf(failed));
            incompleteCountLabel.setText(String.valueOf(incomplete));

            // Load Recent Results into table
            tableModel.setRowCount(0);
            List<ResultSummary> list = resultService.searchResults(null, null, null);
            for (ResultSummary r : list) {
                tableModel.addRow(new Object[]{
                    r.getRollNumber(),
                    r.getStudentName(),
                    "Semester " + r.getSemester(),
                    r.getTotalObtained(),
                    r.getTotalMax(),
                    String.format("%.2f%%", r.getPercentage()),
                    r.getGrade(),
                    r.getStatus()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

