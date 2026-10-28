package com.studentresult.ui;

import com.studentresult.model.MarkEntry;
import com.studentresult.model.Student;
import com.studentresult.service.CalculationService;
import com.studentresult.service.ResultService;
import com.studentresult.service.StudentService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Marks Entry Panel: Select student and semester, dynamically load subjects,
 * enter marks with client-side validation, and auto-calculate evaluation results.
 */
public class MarksEntryPanel extends JPanel {

    private final StudentService studentService;
    private final ResultService resultService;
    private final MainFrame mainFrame;

    private JComboBox<Integer> semesterCombo;
    private JComboBox<StudentItem> studentCombo;
    private JTable marksTable;
    private DefaultTableModel tableModel;

    // Live calculation summary widgets
    private JLabel totalObtainedLabel;
    private JLabel percentageLabel;
    private JLabel statusBadgeLabel;
    private JLabel gradeBadgeLabel;
    private JButton openReportCardBtn;

    private List<MarkEntry> currentEntries = new ArrayList<>();
    private Student currentSelectedStudent = null;

    public static class StudentItem {
        private final Student student;

        public StudentItem(Student student) {
            this.student = student;
        }

        public Student getStudent() {
            return student;
        }

        @Override
        public String toString() {
            if (student == null) return "-- Select Student --";
            return student.getRollNumber() + " - " + student.getFullName() + " (Sem " + student.getSemester() + ")";
        }
    }

    public MarksEntryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.studentService = new StudentService();
        this.resultService = new ResultService();

        initComponents();
        loadStudentsForSemester();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleGrp = new JPanel();
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));
        titleGrp.setOpaque(false);

        JLabel title = new JLabel("Semester Marks Entry & Evaluation");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Enter subject-wise scores, record student attendance, and trigger automatic result calculation");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        titleGrp.add(title);
        titleGrp.add(subtitle);
        headerPanel.add(titleGrp, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content Container
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        // Selection Toolbar Card
        JPanel toolbarCard = UITheme.createCardPanel();
        toolbarCard.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));

        JLabel semLbl = new JLabel("Select Semester:");
        semLbl.setFont(UITheme.FONT_BOLD);
        semLbl.setForeground(UITheme.TEXT_PRIMARY);

        semesterCombo = UITheme.createComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        semesterCombo.setPreferredSize(new Dimension(80, 32));
        semesterCombo.addActionListener(e -> {
            loadStudentsForSemester();
            loadSubjectMarksForSelectedStudent();
        });

        JLabel stuLbl = new JLabel("Select Student:");
        stuLbl.setFont(UITheme.FONT_BOLD);
        stuLbl.setForeground(UITheme.TEXT_PRIMARY);

        studentCombo = new JComboBox<>();
        studentCombo.setFont(UITheme.FONT_REGULAR);
        studentCombo.setBackground(Color.WHITE);
        studentCombo.setPreferredSize(new Dimension(320, 32));
        studentCombo.addActionListener(e -> loadSubjectMarksForSelectedStudent());

        JButton reloadBtn = UITheme.createStyledButton("Reload Subjects", false);
        reloadBtn.addActionListener(e -> loadSubjectMarksForSelectedStudent());

        toolbarCard.add(semLbl);
        toolbarCard.add(semesterCombo);
        toolbarCard.add(stuLbl);
        toolbarCard.add(studentCombo);
        toolbarCard.add(reloadBtn);

        centerPanel.add(toolbarCard, BorderLayout.NORTH);

        // Marks Entry Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] columns = {"Subject Code", "Subject Name", "Max Marks", "Passing Marks", "Marks Obtained", "Absent?", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only Marks Obtained (4), Absent (5), and Remarks (6) are editable
                return column == 4 || column == 5 || column == 6;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) {
                    return Boolean.class; // Checkbox for Absent
                }
                return Object.class;
            }
        };

        marksTable = new JTable(tableModel);
        UITheme.styleTable(marksTable);

        // Custom editor for numeric marks obtained
        JTextField marksEditorField = UITheme.createTextField(6);
        marksTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(marksEditorField));

        JScrollPane scrollPane = new JScrollPane(marksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GREY));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);

        // Bottom Evaluation Preview & Save Action Card
        JPanel bottomCard = UITheme.createCardPanel();
        bottomCard.setLayout(new BorderLayout(14, 10));

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 6));
        statsRow.setOpaque(false);

        totalObtainedLabel = new JLabel("0.0 / 0.0");
        totalObtainedLabel.setFont(UITheme.FONT_HEADER);

        percentageLabel = new JLabel("0.00%");
        percentageLabel.setFont(UITheme.FONT_HEADER);

        statusBadgeLabel = new JLabel("PENDING");
        statusBadgeLabel.setFont(UITheme.FONT_HEADER);

        gradeBadgeLabel = new JLabel("--");
        gradeBadgeLabel.setFont(UITheme.FONT_HEADER);

        statsRow.add(createMiniStatBox("Total Marks", totalObtainedLabel));
        statsRow.add(createMiniStatBox("Percentage", percentageLabel));
        statsRow.add(createMiniStatBox("Overall Result", statusBadgeLabel));
        statsRow.add(createMiniStatBox("Letter Grade", gradeBadgeLabel));

        bottomCard.add(statsRow, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);

        JButton saveAndCalcBtn = UITheme.createStyledButton("Save Marks & Calculate Result", true);
        saveAndCalcBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveAndCalcBtn.addActionListener(e -> saveAndCalculate());

        openReportCardBtn = UITheme.createStyledButton("Open Printable Report Card", false);
        openReportCardBtn.setEnabled(false);
        openReportCardBtn.addActionListener(e -> {
            if (currentSelectedStudent != null) {
                mainFrame.openReportCardForStudent(currentSelectedStudent.getId(), (int) semesterCombo.getSelectedItem());
            }
        });

        actionRow.add(openReportCardBtn);
        actionRow.add(saveAndCalcBtn);

        bottomCard.add(actionRow, BorderLayout.SOUTH);

        centerPanel.add(bottomCard, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createMiniStatBox(String title, JLabel valueLabel) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(248, 250, 252));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_GREY, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setForeground(UITheme.NAVY_PRIMARY);

        box.add(titleLbl);
        box.add(valueLabel);
        return box;
    }

    public void loadStudentsForSemester() {
        int semester = (int) semesterCombo.getSelectedItem();
        studentCombo.removeAllItems();
        studentCombo.addItem(new StudentItem(null));

        try {
            List<Student> allStudents = studentService.getAllStudents();
            // Prioritize students enrolled in this semester
            for (Student s : allStudents) {
                if (s.getSemester() == semester) {
                    studentCombo.addItem(new StudentItem(s));
                }
            }
            // Also append students from other semesters so newly created students are always accessible
            for (Student s : allStudents) {
                if (s.getSemester() != semester) {
                    studentCombo.addItem(new StudentItem(s));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void selectStudentAndSemester(int studentId, int semester) {
        semesterCombo.setSelectedItem(semester);
        loadStudentsForSemester();
        for (int i = 0; i < studentCombo.getItemCount(); i++) {
            StudentItem item = studentCombo.getItemAt(i);
            if (item != null && item.getStudent() != null && item.getStudent().getId() == studentId) {
                studentCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void loadSubjectMarksForSelectedStudent() {
        StudentItem selectedItem = (StudentItem) studentCombo.getSelectedItem();
        tableModel.setRowCount(0);
        resetEvaluationDisplay();

        if (selectedItem == null || selectedItem.getStudent() == null) {
            currentSelectedStudent = null;
            currentEntries.clear();
            return;
        }

        currentSelectedStudent = selectedItem.getStudent();
        int semester = (int) semesterCombo.getSelectedItem();

        try {
            currentEntries = resultService.getMarksForEntry(currentSelectedStudent.getId(), semester);
            if (currentEntries.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No subjects have been configured for Semester " + semester + ".\n" +
                    "Please configure subjects in the Subject Management panel first.", 
                    "Curriculum Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (MarkEntry entry : currentEntries) {
                Object marksVal = entry.getMarksObtained() != null ? entry.getMarksObtained() : "";
                boolean absent = entry.isAbsent();
                String remarks = entry.getRemarks() != null ? entry.getRemarks() : "";

                tableModel.addRow(new Object[]{
                    entry.getSubjectCode(),
                    entry.getSubjectName(),
                    entry.getMaxMarks(),
                    entry.getPassMarks(),
                    absent ? "" : marksVal,
                    absent,
                    remarks
                });
            }

            // If existing marks are present, perform preview evaluation
            CalculationService.Evaluation eval = new CalculationService().evaluate(currentEntries);
            updateEvaluationDisplay(eval);
            openReportCardBtn.setEnabled(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load marks: " + e.getMessage(), 
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAndCalculate() {
        if (currentSelectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Please select a student first.", 
                                          "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Stop editing if cell is actively focused
        if (marksTable.isEditing()) {
            marksTable.getCellEditor().stopCellEditing();
        }

        int semester = (int) semesterCombo.getSelectedItem();
        int rowCount = tableModel.getRowCount();

        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "No subjects found to enter marks for Semester " + semester + ".", 
                                          "Empty Table", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<MarkEntry> entriesToSave = new ArrayList<>();
        String today = LocalDate.now().toString();

        for (int row = 0; row < rowCount; row++) {
            MarkEntry original = currentEntries.get(row);
            MarkEntry entry = new MarkEntry();
            entry.setStudentId(currentSelectedStudent.getId());
            entry.setSubjectId(original.getSubjectId());
            entry.setSemester(semester);
            entry.setSubjectCode(original.getSubjectCode());
            entry.setSubjectName(original.getSubjectName());
            entry.setMaxMarks(original.getMaxMarks());
            entry.setPassMarks(original.getPassMarks());
            entry.setCredits(original.getCredits());
            entry.setExamDate(today);

            boolean isAbsent = Boolean.TRUE.equals(tableModel.getValueAt(row, 5));
            entry.setAbsent(isAbsent);

            Object rawMarks = tableModel.getValueAt(row, 4);
            Object rawRemarks = tableModel.getValueAt(row, 6);
            entry.setRemarks(rawRemarks != null ? rawRemarks.toString().trim() : "");

            if (isAbsent) {
                entry.setMarksObtained(null);
            } else {
                if (rawMarks == null || rawMarks.toString().trim().isEmpty()) {
                    // Mark as null/incomplete
                    entry.setMarksObtained(null);
                } else {
                    try {
                        double marks = Double.parseDouble(rawMarks.toString().trim());
                        if (marks < 0.0 || marks > original.getMaxMarks()) {
                            JOptionPane.showMessageDialog(this, 
                                "Invalid marks for " + original.getSubjectCode() + ": " + marks + 
                                ".\nMust be between 0.0 and " + original.getMaxMarks() + ".", 
                                "Validation Error", JOptionPane.ERROR_MESSAGE);
                            marksTable.changeSelection(row, 4, false, false);
                            return;
                        }
                        entry.setMarksObtained(marks);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(this, 
                            "Please enter a valid numeric score for " + original.getSubjectCode() + ".", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                        marksTable.changeSelection(row, 4, false, false);
                        return;
                    }
                }
            }
            entriesToSave.add(entry);
        }

        try {
            CalculationService.Evaluation eval = resultService.saveMarksAndCalculate(
                currentSelectedStudent.getId(), semester, entriesToSave);

            updateEvaluationDisplay(eval);
            openReportCardBtn.setEnabled(true);
            mainFrame.refreshDashboardMetrics();

            String message = String.format(
                "Marks saved and result calculated successfully!\n\n" +
                "Total Scored: %.1f / %.1f\n" +
                "Percentage: %.2f%%\n" +
                "Overall Status: %s\n" +
                "Final Grade: %s\n" +
                "Passed Subjects: %d / %d\n" +
                (eval.getAbsentSubjects() > 0 ? "Absent Subjects: " + eval.getAbsentSubjects() + " (Incomplete)\n" : "") +
                (eval.getMissingSubjects() > 0 ? "Pending Subjects: " + eval.getMissingSubjects() + " (Incomplete)\n" : ""),
                eval.getTotalObtained(), eval.getTotalMax(), eval.getPercentage(), 
                eval.getStatus(), eval.getGrade(), eval.getPassedSubjects(), eval.getTotalSubjects()
            );

            JOptionPane.showMessageDialog(this, message, "Evaluation Completed", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Evaluation error: " + ex.getMessage(), 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateEvaluationDisplay(CalculationService.Evaluation eval) {
        totalObtainedLabel.setText(eval.getTotalObtained() + " / " + eval.getTotalMax());
        percentageLabel.setText(String.format("%.2f%%", eval.getPercentage()));
        statusBadgeLabel.setText(eval.getStatus());
        gradeBadgeLabel.setText(eval.getGrade());

        if (eval.isPass()) {
            statusBadgeLabel.setForeground(UITheme.STATUS_PASS_TXT);
        } else if (eval.isFail()) {
            statusBadgeLabel.setForeground(UITheme.STATUS_FAIL_TXT);
        } else {
            statusBadgeLabel.setForeground(UITheme.STATUS_INC_TXT);
        }
    }

    private void resetEvaluationDisplay() {
        totalObtainedLabel.setText("0.0 / 0.0");
        percentageLabel.setText("0.00%");
        statusBadgeLabel.setText("PENDING");
        statusBadgeLabel.setForeground(UITheme.TEXT_MUTED);
        gradeBadgeLabel.setText("--");
        openReportCardBtn.setEnabled(false);
    }
}

