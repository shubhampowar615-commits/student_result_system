package com.studentresult.ui;

import com.studentresult.model.ResultSummary;
import com.studentresult.service.ResultService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Results Panel: Search, filter, and inspect semester results across all students,
 * view grade distributions, and quickly jump to printable report cards.
 */
public class ResultsPanel extends JPanel {

    private final ResultService resultService;
    private final MainFrame mainFrame;

    private JTextField searchField;
    private JComboBox<String> semesterFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JTable resultsTable;
    private DefaultTableModel tableModel;

    public ResultsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.resultService = new ResultService();

        initComponents();
        loadResults();
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

        JLabel title = new JLabel("Semester Results & Evaluations");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Search, filter, and inspect calculated student performances and status");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        titleGrp.add(title);
        titleGrp.add(subtitle);

        // Header Action Buttons
        JPanel actionBtnGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnGrp.setOpaque(false);

        JButton openReportBtn = UITheme.createStyledButton("Open Report Card", true);
        openReportBtn.addActionListener(e -> openSelectedReportCard());

        JButton editMarksBtn = UITheme.createStyledButton("Edit Marks", false);
        editMarksBtn.addActionListener(e -> editSelectedMarks());

        JButton recalcBtn = UITheme.createStyledButton("Recalculate All", false);
        recalcBtn.addActionListener(e -> recalculateAll());

        JButton deleteBtn = UITheme.createStyledButton("Delete Result", false);
        deleteBtn.setForeground(UITheme.STATUS_FAIL_TXT);
        deleteBtn.addActionListener(e -> deleteSelectedResult());

        actionBtnGrp.add(openReportBtn);
        actionBtnGrp.add(editMarksBtn);
        actionBtnGrp.add(recalcBtn);
        actionBtnGrp.add(deleteBtn);

        headerPanel.add(titleGrp, BorderLayout.WEST);
        headerPanel.add(actionBtnGrp, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BorderLayout(12, 12));

        // Search and Filter Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search (Roll / Name):");
        searchLbl.setFont(UITheme.FONT_BOLD);
        searchLbl.setForeground(UITheme.TEXT_PRIMARY);

        searchField = UITheme.createTextField(16);
        searchField.addActionListener(e -> loadResults());

        JLabel semLbl = new JLabel("Semester:");
        semLbl.setFont(UITheme.FONT_BOLD);
        semLbl.setForeground(UITheme.TEXT_PRIMARY);

        String[] semesters = {"All Semesters", "Semester 1", "Semester 2", "Semester 3", 
                              "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8"};
        semesterFilterCombo = UITheme.createComboBox(semesters);
        semesterFilterCombo.addActionListener(e -> loadResults());

        JLabel statLbl = new JLabel("Status:");
        statLbl.setFont(UITheme.FONT_BOLD);
        statLbl.setForeground(UITheme.TEXT_PRIMARY);

        String[] statuses = {"All Statuses", "PASS", "FAIL", "INCOMPLETE"};
        statusFilterCombo = UITheme.createComboBox(statuses);
        statusFilterCombo.addActionListener(e -> loadResults());

        JButton searchBtn = UITheme.createStyledButton("Search", true);
        searchBtn.addActionListener(e -> loadResults());

        JButton resetBtn = UITheme.createStyledButton("Reset", false);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            semesterFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            loadResults();
        });

        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(semLbl);
        filterBar.add(semesterFilterCombo);
        filterBar.add(statLbl);
        filterBar.add(statusFilterCombo);
        filterBar.add(searchBtn);
        filterBar.add(resetBtn);

        cardPanel.add(filterBar, BorderLayout.NORTH);

        // Results Table
        String[] columns = {"Result ID", "Student ID", "Roll Number", "Student Name", "Semester", 
                            "Department", "Total Scored", "Max Marks", "Percentage", "Grade", "Status", "Evaluated At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        UITheme.styleTable(resultsTable);

        // Hide technical IDs from view
        resultsTable.getColumnModel().getColumn(0).setMinWidth(0);
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        resultsTable.getColumnModel().getColumn(1).setMinWidth(0);
        resultsTable.getColumnModel().getColumn(1).setMaxWidth(0);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GREY));
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        add(cardPanel, BorderLayout.CENTER);
    }

    public void loadResults() {
        String query = searchField.getText().trim();
        int semIdx = semesterFilterCombo.getSelectedIndex();
        Integer semester = semIdx > 0 ? semIdx : null;

        int statIdx = statusFilterCombo.getSelectedIndex();
        String status = statIdx > 0 ? (String) statusFilterCombo.getSelectedItem() : null;

        try {
            List<ResultSummary> list = resultService.searchResults(query, semester, status);
            tableModel.setRowCount(0);
            for (ResultSummary r : list) {
                tableModel.addRow(new Object[]{
                    r.getResultId(),
                    r.getStudentId(),
                    r.getRollNumber(),
                    r.getStudentName(),
                    "Semester " + r.getSemester(),
                    r.getDepartment(),
                    r.getTotalObtained(),
                    r.getTotalMax(),
                    String.format("%.2f%%", r.getPercentage()),
                    r.getGrade(),
                    r.getStatus(),
                    r.getEvaluatedAt()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load results: " + e.getMessage(), 
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedReportCard() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student result from the table to view the report card.", 
                                          "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 1);
        String semText = (String) tableModel.getValueAt(selectedRow, 4); // "Semester X"
        int semester = Integer.parseInt(semText.replace("Semester ", "").trim());

        mainFrame.openReportCardForStudent(studentId, semester);
    }

    private void editSelectedMarks() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a result row from the table to edit its marks.", 
                                          "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 1);
        String semText = (String) tableModel.getValueAt(selectedRow, 4);
        int semester = Integer.parseInt(semText.replace("Semester ", "").trim());

        mainFrame.openMarksEntryForStudent(studentId, semester);
    }

    private void recalculateAll() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Recalculate all results in the database based on the latest subject marks and grading rules?",
            "Confirm Recalculation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int count = resultService.recalculateAllResults();
                JOptionPane.showMessageDialog(this, "Successfully recalculated " + count + " semester results.", 
                                              "Recalculation Complete", JOptionPane.INFORMATION_MESSAGE);
                loadResults();
                mainFrame.refreshDashboardMetrics();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Recalculation failed: " + e.getMessage(), 
                                              "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedResult() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a result row to delete.", 
                                          "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resultId = (int) tableModel.getValueAt(selectedRow, 0);
        String roll = (String) tableModel.getValueAt(selectedRow, 2);
        String name = (String) tableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the result record for " + roll + " (" + name + ")?\n" +
            "Note: The entered marks will remain and can be re-evaluated at any time.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                resultService.deleteResult(resultId);
                JOptionPane.showMessageDialog(this, "Result deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadResults();
                mainFrame.refreshDashboardMetrics();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting result: " + e.getMessage(), 
                                              "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

