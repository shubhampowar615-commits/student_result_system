package com.studentresult.ui;

import com.studentresult.model.GradeScale;
import com.studentresult.model.MarkEntry;
import com.studentresult.model.ResultSummary;
import com.studentresult.model.Student;
import com.studentresult.service.ResultService;
import com.studentresult.service.StudentService;
import com.studentresult.util.PrintUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Printable Report Card Panel: Displays an institutional grade card
 * and provides standard desktop printing capability directly from Java Swing.
 */
public class ReportCardPanel extends JPanel {

    private final StudentService studentService;
    private final ResultService resultService;
    private final MainFrame mainFrame;

    private JComboBox<Integer> semesterCombo;
    private JComboBox<MarksEntryPanel.StudentItem> studentCombo;
    private JPanel reportCardCanvas;

    // Report card dynamic labels
    private JLabel collegeTitle;
    private JLabel examSessionLabel;
    private JLabel rollNumberLabel;
    private JLabel studentNameLabel;
    private JLabel departmentLabel;
    private JLabel batchLabel;
    private JLabel semesterDisplayLabel;
    private JLabel dateIssuedLabel;

    private JTable reportMarksTable;
    private DefaultTableModel marksTableModel;

    private JLabel totalObtainedDisplay;
    private JLabel totalMaxDisplay;
    private JLabel percentageDisplay;
    private JLabel gradeDisplay;
    private JLabel statusBadgeDisplay;

    private JButton printButton;

    public ReportCardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.studentService = new StudentService();
        this.resultService = new ResultService();

        initComponents();
        loadStudentList();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Control Bar
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout(10, 10));

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterControls.setOpaque(false);

        JLabel semLbl = new JLabel("Semester:");
        semLbl.setFont(UITheme.FONT_BOLD);
        semLbl.setForeground(UITheme.TEXT_PRIMARY);

        semesterCombo = UITheme.createComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        semesterCombo.setPreferredSize(new Dimension(80, 32));
        semesterCombo.addActionListener(e -> {
            loadStudentList();
            generateReportCard();
        });

        JLabel stuLbl = new JLabel("Student:");
        stuLbl.setFont(UITheme.FONT_BOLD);
        stuLbl.setForeground(UITheme.TEXT_PRIMARY);

        studentCombo = new JComboBox<>();
        studentCombo.setFont(UITheme.FONT_REGULAR);
        studentCombo.setBackground(Color.WHITE);
        studentCombo.setPreferredSize(new Dimension(320, 32));
        studentCombo.addActionListener(e -> generateReportCard());

        JButton generateBtn = UITheme.createStyledButton("Generate Report Card", true);
        generateBtn.addActionListener(e -> generateReportCard());

        filterControls.add(semLbl);
        filterControls.add(semesterCombo);
        filterControls.add(stuLbl);
        filterControls.add(studentCombo);
        filterControls.add(generateBtn);

        JPanel printControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        printControls.setOpaque(false);

        printButton = UITheme.createStyledButton("Print Grade Sheet", true);
        printButton.setBackground(new Color(15, 118, 110)); // Deep Teal
        printButton.addActionListener(e -> printCurrentReportCard());

        printControls.add(printButton);

        topBar.add(filterControls, BorderLayout.WEST);
        topBar.add(printControls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center: Printable Report Card in a ScrollPane
        JPanel canvasWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        canvasWrapper.setBackground(UITheme.BG_LIGHT);

        reportCardCanvas = createReportCardCanvas();
        canvasWrapper.add(reportCardCanvas);

        JScrollPane scrollPane = new JScrollPane(canvasWrapper);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GREY));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createReportCardCanvas() {
        // High quality printable sheet sized approximately for standard A4 aspect
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Outer ornamental academic border
                g2.setColor(UITheme.NAVY_PRIMARY);
                g2.drawRect(8, 8, getWidth() - 17, getHeight() - 17);
                g2.drawRect(12, 12, getWidth() - 25, getHeight() - 25);
            }
        };
        card.setLayout(new BorderLayout(14, 14));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(740, 840));
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // 1. Institution Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        collegeTitle = new JLabel("COLLEGE OF ENGINEERING & TECHNOLOGY", SwingConstants.CENTER);
        collegeTitle.setAlignmentX(CENTER_ALIGNMENT);
        collegeTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        collegeTitle.setForeground(UITheme.NAVY_PRIMARY);

        JLabel subCollege = new JLabel("Approved by AICTE & Affiliated to University", SwingConstants.CENTER);
        subCollege.setAlignmentX(CENTER_ALIGNMENT);
        subCollege.setFont(UITheme.FONT_SMALL);
        subCollege.setForeground(UITheme.TEXT_MUTED);

        JLabel reportHeading = new JLabel("OFFICIAL STATEMENT OF MARKS & GRADE CARD", SwingConstants.CENTER);
        reportHeading.setAlignmentX(CENTER_ALIGNMENT);
        reportHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        reportHeading.setForeground(UITheme.TEXT_PRIMARY);

        examSessionLabel = new JLabel("Academic Session Examination 2024-2025", SwingConstants.CENTER);
        examSessionLabel.setAlignmentX(CENTER_ALIGNMENT);
        examSessionLabel.setFont(UITheme.FONT_SMALL);
        examSessionLabel.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(collegeTitle);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(subCollege);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(reportHeading);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(examSessionLabel);
        headerPanel.add(Box.createVerticalStrut(10));

        // 2. Student Info Grid
        JPanel studentInfoBox = new JPanel(new GridLayout(3, 2, 8, 4));
        studentInfoBox.setBackground(new Color(248, 250, 252));
        studentInfoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_GREY, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        rollNumberLabel = new JLabel("Roll Number: --");
        rollNumberLabel.setFont(UITheme.FONT_BOLD);

        studentNameLabel = new JLabel("Student Name: --");
        studentNameLabel.setFont(UITheme.FONT_BOLD);

        departmentLabel = new JLabel("Department: --");
        departmentLabel.setFont(UITheme.FONT_REGULAR);

        batchLabel = new JLabel("Batch: --");
        batchLabel.setFont(UITheme.FONT_REGULAR);

        semesterDisplayLabel = new JLabel("Semester: --");
        semesterDisplayLabel.setFont(UITheme.FONT_BOLD);

        dateIssuedLabel = new JLabel("Date Issued: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        dateIssuedLabel.setFont(UITheme.FONT_REGULAR);

        studentInfoBox.add(rollNumberLabel);
        studentInfoBox.add(studentNameLabel);
        studentInfoBox.add(departmentLabel);
        studentInfoBox.add(batchLabel);
        studentInfoBox.add(semesterDisplayLabel);
        studentInfoBox.add(dateIssuedLabel);

        headerPanel.add(studentInfoBox);
        card.add(headerPanel, BorderLayout.NORTH);

        // 3. Subject-wise Marks Table
        String[] columns = {"Course Code", "Subject / Course Title", "Credits", "Max Marks", "Pass Marks", "Marks Scored", "Status", "Grade"};
        marksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportMarksTable = new JTable(marksTableModel);
        UITheme.styleTable(reportMarksTable);
        reportMarksTable.setRowHeight(26);

        JScrollPane tableScroll = new JScrollPane(reportMarksTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GREY));
        tableScroll.getViewport().setBackground(Color.WHITE);
        card.add(tableScroll, BorderLayout.CENTER);

        // 4. Summary & Signatures Footer
        JPanel footerContainer = new JPanel();
        footerContainer.setLayout(new BoxLayout(footerContainer, BoxLayout.Y_AXIS));
        footerContainer.setOpaque(false);

        // Summary Bar
        JPanel summaryBox = new JPanel(new GridLayout(1, 5, 8, 4));
        summaryBox.setBackground(new Color(241, 245, 249));
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_GREY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        totalObtainedDisplay = new JLabel("--", SwingConstants.CENTER);
        totalObtainedDisplay.setFont(UITheme.FONT_BOLD);

        totalMaxDisplay = new JLabel("--", SwingConstants.CENTER);
        totalMaxDisplay.setFont(UITheme.FONT_BOLD);

        percentageDisplay = new JLabel("--", SwingConstants.CENTER);
        percentageDisplay.setFont(UITheme.FONT_BOLD);

        gradeDisplay = new JLabel("--", SwingConstants.CENTER);
        gradeDisplay.setFont(UITheme.FONT_BOLD);

        statusBadgeDisplay = new JLabel("--", SwingConstants.CENTER);
        statusBadgeDisplay.setFont(UITheme.FONT_HEADER);

        summaryBox.add(createReportSummaryCell("Total Scored", totalObtainedDisplay));
        summaryBox.add(createReportSummaryCell("Max Marks", totalMaxDisplay));
        summaryBox.add(createReportSummaryCell("Percentage", percentageDisplay));
        summaryBox.add(createReportSummaryCell("Final Grade", gradeDisplay));
        summaryBox.add(createReportSummaryCell("Result Status", statusBadgeDisplay));

        footerContainer.add(summaryBox);
        footerContainer.add(Box.createVerticalStrut(30));

        // Signature section
        JPanel signRow = new JPanel(new GridLayout(1, 3, 20, 0));
        signRow.setOpaque(false);

        signRow.add(createSignBox("Prepared By / Class Incharge"));
        signRow.add(createSignBox("Controller of Examinations"));
        signRow.add(createSignBox("Principal / Dean"));

        footerContainer.add(signRow);
        footerContainer.add(Box.createVerticalStrut(10));

        JLabel watermark = new JLabel("* This is a computer generated certified college report card *", SwingConstants.CENTER);
        watermark.setAlignmentX(CENTER_ALIGNMENT);
        watermark.setFont(UITheme.FONT_SMALL);
        watermark.setForeground(UITheme.TEXT_MUTED);
        footerContainer.add(watermark);

        card.add(footerContainer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createReportSummaryCell(String label, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JPanel createSignBox(String title) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setOpaque(false);

        JLabel line = new JLabel("____________________________", SwingConstants.CENTER);
        line.setForeground(UITheme.BORDER_GREY);

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(UITheme.FONT_SMALL);
        t.setForeground(UITheme.TEXT_PRIMARY);

        p.add(line, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    public void loadStudentList() {
        int semester = (int) semesterCombo.getSelectedItem();
        studentCombo.removeAllItems();
        studentCombo.addItem(new MarksEntryPanel.StudentItem(null));

        try {
            List<Student> list = studentService.getStudentsBySemester(semester);
            if (list.isEmpty()) {
                list = studentService.getAllStudents();
            }
            for (Student s : list) {
                studentCombo.addItem(new MarksEntryPanel.StudentItem(s));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void selectStudentAndSemester(int studentId, int semester) {
        semesterCombo.setSelectedItem(semester);
        loadStudentList();
        for (int i = 0; i < studentCombo.getItemCount(); i++) {
            MarksEntryPanel.StudentItem item = studentCombo.getItemAt(i);
            if (item != null && item.getStudent() != null && item.getStudent().getId() == studentId) {
                studentCombo.setSelectedIndex(i);
                break;
            }
        }
        generateReportCard();
    }

    public void generateReportCard() {
        MarksEntryPanel.StudentItem item = (MarksEntryPanel.StudentItem) studentCombo.getSelectedItem();
        if (item == null || item.getStudent() == null) {
            resetReportCardView();
            return;
        }

        Student student = item.getStudent();
        int semester = (int) semesterCombo.getSelectedItem();

        rollNumberLabel.setText("Roll Number: " + student.getRollNumber());
        studentNameLabel.setText("Student Name: " + student.getFullName());
        departmentLabel.setText("Department: " + student.getDepartment());
        batchLabel.setText("Batch: " + student.getBatchYear());
        semesterDisplayLabel.setText("Semester: " + semester);

        marksTableModel.setRowCount(0);

        try {
            List<MarkEntry> marks = resultService.getReportCardMarks(student.getId(), semester);
            ResultSummary summary = resultService.getReportCardSummary(student.getId(), semester);

            if (marks.isEmpty()) {
                resetSummaryDisplay();
                JOptionPane.showMessageDialog(this, 
                    "No marks have been recorded for " + student.getRollNumber() + " in Semester " + semester + ".\n" +
                    "Please enter marks in the Marks Entry panel first.", 
                    "No Examination Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (MarkEntry m : marks) {
                String scoredStr = m.isAbsent() ? "ABSENT" : (m.getMarksObtained() != null ? String.valueOf(m.getMarksObtained()) : "--");
                String statusStr = m.getSubjectStatus();
                String subjectGrade = calculateSubjectGrade(m);

                marksTableModel.addRow(new Object[]{
                    m.getSubjectCode(),
                    m.getSubjectName(),
                    m.getCredits(),
                    m.getMaxMarks(),
                    m.getPassMarks(),
                    scoredStr,
                    statusStr,
                    subjectGrade
                });
            }

            if (summary != null) {
                totalObtainedDisplay.setText(String.valueOf(summary.getTotalObtained()));
                totalMaxDisplay.setText(String.valueOf(summary.getTotalMax()));
                percentageDisplay.setText(String.format("%.2f%%", summary.getPercentage()));
                gradeDisplay.setText(summary.getGrade());
                statusBadgeDisplay.setText(summary.getStatus());

                if ("PASS".equalsIgnoreCase(summary.getStatus())) {
                    statusBadgeDisplay.setForeground(UITheme.STATUS_PASS_TXT);
                } else if ("FAIL".equalsIgnoreCase(summary.getStatus())) {
                    statusBadgeDisplay.setForeground(UITheme.STATUS_FAIL_TXT);
                } else {
                    statusBadgeDisplay.setForeground(UITheme.STATUS_INC_TXT);
                }
            } else {
                resetSummaryDisplay();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load report card data: " + e.getMessage(), 
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String calculateSubjectGrade(MarkEntry m) {
        if (m.isAbsent() || m.getMarksObtained() == null) {
            return GradeScale.GRADE_INCOMPLETE;
        }
        if (m.getMarksObtained() < m.getPassMarks()) {
            return GradeScale.GRADE_FAIL;
        }
        double percentage = (m.getMarksObtained() / m.getMaxMarks()) * 100.0;
        return GradeScale.calculateGrade(percentage, GradeScale.STATUS_PASS);
    }

    private void printCurrentReportCard() {
        MarksEntryPanel.StudentItem item = (MarksEntryPanel.StudentItem) studentCombo.getSelectedItem();
        if (item == null || item.getStudent() == null) {
            JOptionPane.showMessageDialog(this, "Please select a student and generate a report card before printing.", 
                                          "Nothing to Print", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roll = item.getStudent().getRollNumber();
        int sem = (int) semesterCombo.getSelectedItem();
        boolean success = PrintUtil.printComponent(reportCardCanvas, "ReportCard_" + roll + "_Sem" + sem);
        if (success) {
            JOptionPane.showMessageDialog(this, "Report card dispatched to printer successfully.", 
                                          "Print Job Sent", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetReportCardView() {
        rollNumberLabel.setText("Roll Number: --");
        studentNameLabel.setText("Student Name: --");
        departmentLabel.setText("Department: --");
        batchLabel.setText("Batch: --");
        semesterDisplayLabel.setText("Semester: --");
        marksTableModel.setRowCount(0);
        resetSummaryDisplay();
    }

    private void resetSummaryDisplay() {
        totalObtainedDisplay.setText("--");
        totalMaxDisplay.setText("--");
        percentageDisplay.setText("--");
        gradeDisplay.setText("--");
        statusBadgeDisplay.setText("--");
        statusBadgeDisplay.setForeground(UITheme.TEXT_MUTED);
    }
}

