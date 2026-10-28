package com.studentresult.ui;

import com.studentresult.model.Student;
import com.studentresult.service.StudentService;
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
 * Student Management Panel: Add, view, edit, delete, and search students
 * by roll number or name with validation and duplicate prevention.
 */
public class StudentPanel extends JPanel {

    private final StudentService studentService;
    private final MainFrame mainFrame;

    private JTextField searchField;
    private JComboBox<String> semesterFilterCombo;
    private JTable studentTable;
    private DefaultTableModel tableModel;

    public StudentPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.studentService = new StudentService();

        initComponents();
        loadStudents();
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

        JLabel title = new JLabel("Student Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Enroll, view, update, and search student records");
        subtitle.setFont(UITheme.FONT_SUBTITLE);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        titleGrp.add(title);
        titleGrp.add(subtitle);

        // Header Action Buttons
        JPanel actionBtnGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnGrp.setOpaque(false);

        JButton addBtn = UITheme.createStyledButton("+ Add New Student", true);
        addBtn.addActionListener(e -> showStudentDialog(null));

        JButton editBtn = UITheme.createStyledButton("Edit Selected", false);
        editBtn.addActionListener(e -> editSelectedStudent());

        JButton deleteBtn = UITheme.createStyledButton("Delete", false);
        deleteBtn.setForeground(UITheme.STATUS_FAIL_TXT);
        deleteBtn.addActionListener(e -> deleteSelectedStudent());

        actionBtnGrp.add(addBtn);
        actionBtnGrp.add(editBtn);
        actionBtnGrp.add(deleteBtn);

        headerPanel.add(titleGrp, BorderLayout.WEST);
        headerPanel.add(actionBtnGrp, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Table Container Card
        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BorderLayout(12, 12));

        // Search & Filter Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search (Roll / Name):");
        searchLbl.setFont(UITheme.FONT_BOLD);
        searchLbl.setForeground(UITheme.TEXT_PRIMARY);

        searchField = UITheme.createTextField(18);
        searchField.addActionListener(e -> applyFilter());

        JLabel semLbl = new JLabel("Filter Semester:");
        semLbl.setFont(UITheme.FONT_BOLD);
        semLbl.setForeground(UITheme.TEXT_PRIMARY);

        String[] semesters = {"All Semesters", "Semester 1", "Semester 2", "Semester 3", 
                              "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8"};
        semesterFilterCombo = UITheme.createComboBox(semesters);
        semesterFilterCombo.addActionListener(e -> applyFilter());

        JButton searchBtn = UITheme.createStyledButton("Search", true);
        searchBtn.addActionListener(e -> applyFilter());

        JButton resetBtn = UITheme.createStyledButton("Reset", false);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            semesterFilterCombo.setSelectedIndex(0);
            loadStudents();
        });

        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(semLbl);
        filterBar.add(semesterFilterCombo);
        filterBar.add(searchBtn);
        filterBar.add(resetBtn);

        cardPanel.add(filterBar, BorderLayout.NORTH);

        // Student JTable
        String[] columns = {"ID", "Roll Number", "First Name", "Last Name", "Semester", "Department", "Batch Year", "Email", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        UITheme.styleTable(studentTable);
        // Hide internal DB ID column from user view, but keep data accessible
        studentTable.getColumnModel().getColumn(0).setMinWidth(40);
        studentTable.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GREY));
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        add(cardPanel, BorderLayout.CENTER);
    }

    public void loadStudents() {
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField.getText().trim();
        int selectedIndex = semesterFilterCombo.getSelectedIndex();
        Integer semester = selectedIndex > 0 ? selectedIndex : null;

        try {
            List<Student> students = studentService.searchStudents(query, semester);
            tableModel.setRowCount(0);
            for (Student s : students) {
                tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getRollNumber(),
                    s.getFirstName(),
                    s.getLastName(),
                    "Sem " + s.getSemester(),
                    s.getDepartment(),
                    s.getBatchYear(),
                    s.getEmail(),
                    s.getPhone()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load students: " + e.getMessage(), 
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to edit.", 
                                          "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Student student = studentService.getStudentById(studentId);
            if (student != null) {
                showStudentDialog(student);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching student details: " + e.getMessage(), 
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to delete.", 
                                          "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 0);
        String rollNumber = (String) tableModel.getValueAt(selectedRow, 1);
        String studentName = tableModel.getValueAt(selectedRow, 2) + " " + tableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete student: " + rollNumber + " (" + studentName + ")?\n" +
            "This will also delete associated marks and semester results.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.deleteStudent(studentId);
                JOptionPane.showMessageDialog(this, "Student deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadStudents();
                mainFrame.refreshDashboardMetrics();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting student: " + e.getMessage(), 
                                              "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showStudentDialog(Student studentToEdit) {
        boolean isEdit = (studentToEdit != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                     isEdit ? "Edit Student Details" : "Add New Student", true);
        dialog.setSize(440, 480);
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

        JTextField rollField = UITheme.createTextField(15);
        JTextField fnameField = UITheme.createTextField(15);
        JTextField lnameField = UITheme.createTextField(15);
        JTextField emailField = UITheme.createTextField(15);
        JTextField phoneField = UITheme.createTextField(15);
        JComboBox<Integer> semCombo = UITheme.createComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        JTextField deptField = UITheme.createTextField(15);
        deptField.setText("Computer Science");
        JTextField batchField = UITheme.createTextField(15);
        batchField.setText("2024-2028");

        if (isEdit) {
            rollField.setText(studentToEdit.getRollNumber());
            fnameField.setText(studentToEdit.getFirstName());
            lnameField.setText(studentToEdit.getLastName());
            emailField.setText(studentToEdit.getEmail());
            phoneField.setText(studentToEdit.getPhone());
            semCombo.setSelectedItem(studentToEdit.getSemester());
            deptField.setText(studentToEdit.getDepartment());
            batchField.setText(studentToEdit.getBatchYear());
        }

        addField(formPanel, gbc, "Roll Number *:", rollField);
        addField(formPanel, gbc, "First Name *:", fnameField);
        addField(formPanel, gbc, "Last Name:", lnameField);
        addField(formPanel, gbc, "Semester *:", semCombo);
        addField(formPanel, gbc, "Department *:", deptField);
        addField(formPanel, gbc, "Batch Year *:", batchField);
        addField(formPanel, gbc, "Email:", emailField);
        addField(formPanel, gbc, "Phone:", phoneField);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.BG_LIGHT);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_GREY));

        JButton saveBtn = UITheme.createStyledButton(isEdit ? "Update Student" : "Save Student", true);
        JButton cancelBtn = UITheme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String roll = rollField.getText().trim();
                String fname = fnameField.getText().trim();
                String lname = lnameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                int sem = (int) semCombo.getSelectedItem();
                String dept = deptField.getText().trim();
                String batch = batchField.getText().trim();

                if (isEdit) {
                    studentToEdit.setRollNumber(roll);
                    studentToEdit.setFirstName(fname);
                    studentToEdit.setLastName(lname);
                    studentToEdit.setEmail(email);
                    studentToEdit.setPhone(phone);
                    studentToEdit.setSemester(sem);
                    studentToEdit.setDepartment(dept);
                    studentToEdit.setBatchYear(batch);
                    studentService.updateStudent(studentToEdit);
                    JOptionPane.showMessageDialog(dialog, "Student updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Student newStudent = new Student(roll, fname, lname, email, phone, sem, dept, batch);
                    studentService.addStudent(newStudent);
                    JOptionPane.showMessageDialog(dialog, "Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadStudents();
                mainFrame.refreshDashboardMetrics();
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

