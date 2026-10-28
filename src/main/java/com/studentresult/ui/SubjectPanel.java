package com.studentresult.ui;

import com.studentresult.model.Subject;
import com.studentresult.service.SubjectService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Subject Management Panel: Configure fixed semester curriculum subjects,
 * subject codes, maximum marks, passing marks, and credits with editable sample defaults.
 */
public class SubjectPanel extends JPanel {

    private final SubjectService subjectService;
    private final MainFrame mainFrame;

    private JComboBox<String> semesterFilterCombo;
    private JTable subjectTable;
    private DefaultTableModel tableModel;

    public SubjectPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.subjectService = new SubjectService();

        initComponents();
        loadSubjects();
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

        JLabel title = new JLabel("Subject Curriculum Configuration");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Configure semester subjects, codes, maximum marks, and passing thresholds");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        titleGrp.add(title);
        titleGrp.add(subtitle);

        // Header Buttons
        JPanel actionBtnGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnGrp.setOpaque(false);

        JButton addBtn = UITheme.createStyledButton("+ Add Subject", true);
        addBtn.addActionListener(e -> showSubjectDialog(null));

        JButton editBtn = UITheme.createStyledButton("Edit Selected", false);
        editBtn.addActionListener(e -> editSelectedSubject());

        JButton deleteBtn = UITheme.createStyledButton("Delete", false);
        deleteBtn.setForeground(UITheme.STATUS_FAIL_TXT);
        deleteBtn.addActionListener(e -> deleteSelectedSubject());

        JButton resetDefaultsBtn = UITheme.createStyledButton("Reset Sample Defaults", false);
        resetDefaultsBtn.addActionListener(e -> resetDefaultSubjects());

        actionBtnGrp.add(addBtn);
        actionBtnGrp.add(editBtn);
        actionBtnGrp.add(deleteBtn);
        actionBtnGrp.add(resetDefaultsBtn);

        headerPanel.add(titleGrp, BorderLayout.WEST);
        headerPanel.add(actionBtnGrp, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BorderLayout(12, 12));

        // Sample Defaults Info Banner
        JPanel noticeBanner = new JPanel(new BorderLayout());
        noticeBanner.setBackground(new Color(239, 246, 255));
        noticeBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(191, 219, 254), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        JLabel noticeLbl = new JLabel("<html><b>Sample Default Curriculum:</b> Pre-configured with typical engineering / college subjects for Semesters 1 to 4. Feel free to add, modify, or customize them to match your college syllabus.</html>");
        noticeLbl.setFont(UITheme.FONT_SMALL);
        noticeLbl.setForeground(new Color(29, 78, 216));
        noticeBanner.add(noticeLbl, BorderLayout.CENTER);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        JLabel semLbl = new JLabel("Filter by Semester:");
        semLbl.setFont(UITheme.FONT_BOLD);
        semLbl.setForeground(UITheme.TEXT_PRIMARY);

        String[] semesters = {"All Semesters", "Semester 1", "Semester 2", "Semester 3", 
                              "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8"};
        semesterFilterCombo = UITheme.createComboBox(semesters);
        semesterFilterCombo.addActionListener(e -> loadSubjects());

        filterBar.add(semLbl);
        filterBar.add(semesterFilterCombo);

        JPanel topBarContainer = new JPanel();
        topBarContainer.setLayout(new BoxLayout(topBarContainer, BoxLayout.Y_AXIS));
        topBarContainer.setOpaque(false);
        topBarContainer.add(noticeBanner);
        topBarContainer.add(javax.swing.Box.createVerticalStrut(10));
        topBarContainer.add(filterBar);

        cardPanel.add(topBarContainer, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Subject Code", "Subject Name", "Semester", "Maximum Marks", "Passing Marks", "Credits"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        subjectTable = new JTable(tableModel);
        UITheme.styleTable(subjectTable);
        subjectTable.getColumnModel().getColumn(0).setMinWidth(40);
        subjectTable.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GREY));
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        add(cardPanel, BorderLayout.CENTER);
    }

    public void loadSubjects() {
        int selectedIndex = semesterFilterCombo.getSelectedIndex();
        try {
            List<Subject> list;
            if (selectedIndex > 0) {
                list = subjectService.getSubjectsBySemester(selectedIndex);
            } else {
                list = subjectService.getAllSubjects();
            }

            tableModel.setRowCount(0);
            for (Subject s : list) {
                tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getSubjectCode(),
                    s.getSubjectName(),
                    "Semester " + s.getSemester(),
                    s.getMaxMarks(),
                    s.getPassMarks(),
                    s.getCredits()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load subjects: " + e.getMessage(), 
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedSubject() {
        int selectedRow = subjectTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject from the table to edit.", 
                                          "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int subjectId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Subject subject = subjectService.getSubjectById(subjectId);
            if (subject != null) {
                showSubjectDialog(subject);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching subject details: " + e.getMessage(), 
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedSubject() {
        int selectedRow = subjectTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject from the table to delete.", 
                                          "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int subjectId = (int) tableModel.getValueAt(selectedRow, 0);
        String code = (String) tableModel.getValueAt(selectedRow, 1);
        String name = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete subject: " + code + " - " + name + "?\n" +
            "This will also delete marks entered for this subject.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                subjectService.deleteSubject(subjectId);
                JOptionPane.showMessageDialog(this, "Subject deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSubjects();
                mainFrame.refreshDashboardMetrics();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting subject: " + e.getMessage(), 
                                              "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetDefaultSubjects() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "This will restore sample subjects for Semesters 1 through 4.\nContinue?",
            "Confirm Reset Defaults",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                subjectService.resetToDefaultSubjects();
                JOptionPane.showMessageDialog(this, "Sample subjects restored successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSubjects();
                mainFrame.refreshDashboardMetrics();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error resetting subjects: " + e.getMessage(), 
                                              "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSubjectDialog(Subject subjectToEdit) {
        boolean isEdit = (subjectToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                     isEdit ? "Edit Subject" : "Add New Subject", true);
        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.PANEL_WHITE);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JTextField codeField = UITheme.createTextField(15);
        JTextField nameField = UITheme.createTextField(15);
        JComboBox<Integer> semCombo = UITheme.createComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        JTextField maxMarksField = UITheme.createTextField(15);
        maxMarksField.setText("100.0");
        JTextField passMarksField = UITheme.createTextField(15);
        passMarksField.setText("40.0");
        JTextField creditsField = UITheme.createTextField(15);
        creditsField.setText("3");

        if (isEdit) {
            codeField.setText(subjectToEdit.getSubjectCode());
            nameField.setText(subjectToEdit.getSubjectName());
            semCombo.setSelectedItem(subjectToEdit.getSemester());
            maxMarksField.setText(String.valueOf(subjectToEdit.getMaxMarks()));
            passMarksField.setText(String.valueOf(subjectToEdit.getPassMarks()));
            creditsField.setText(String.valueOf(subjectToEdit.getCredits()));
        }

        addField(formPanel, gbc, "Subject Code *:", codeField);
        addField(formPanel, gbc, "Subject Name *:", nameField);
        addField(formPanel, gbc, "Semester *:", semCombo);
        addField(formPanel, gbc, "Maximum Marks *:", maxMarksField);
        addField(formPanel, gbc, "Passing Marks *:", passMarksField);
        addField(formPanel, gbc, "Credits (0-10):", creditsField);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.BG_LIGHT);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_GREY));

        JButton saveBtn = UITheme.createStyledButton(isEdit ? "Update Subject" : "Save Subject", true);
        JButton cancelBtn = UITheme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                String name = nameField.getText().trim();
                int sem = (int) semCombo.getSelectedItem();
                double maxMarks = Double.parseDouble(maxMarksField.getText().trim());
                double passMarks = Double.parseDouble(passMarksField.getText().trim());
                int credits = Integer.parseInt(creditsField.getText().trim());

                if (isEdit) {
                    subjectToEdit.setSubjectCode(code);
                    subjectToEdit.setSubjectName(name);
                    subjectToEdit.setSemester(sem);
                    subjectToEdit.setMaxMarks(maxMarks);
                    subjectToEdit.setPassMarks(passMarks);
                    subjectToEdit.setCredits(credits);
                    subjectService.updateSubject(subjectToEdit);
                    JOptionPane.showMessageDialog(dialog, "Subject updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Subject newSub = new Subject(code, name, sem, maxMarks, passMarks, credits);
                    subjectService.addSubject(newSub);
                    JOptionPane.showMessageDialog(dialog, "Subject added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadSubjects();
                mainFrame.refreshDashboardMetrics();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Maximum marks, passing marks, and credits must be valid numbers.", 
                                              "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException | SQLException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String labelText, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);

        gbc.gridy++;
    }
}

