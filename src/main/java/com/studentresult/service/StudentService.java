package com.studentresult.service;

import com.studentresult.dao.StudentDao;
import com.studentresult.model.Student;
import com.studentresult.util.ValidationUtil;
import java.sql.SQLException;
import java.util.List;

/**
 * Service encapsulating business logic, validation, and operations for Students.
 */
public class StudentService {

    private final StudentDao studentDao;

    public StudentService() {
        this.studentDao = new StudentDao();
    }

    public StudentService(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    /**
     * Validates and saves a new student record.
     * Prevents duplicate roll numbers.
     */
    public void addStudent(Student student) throws IllegalArgumentException, SQLException {
        validateStudent(student, 0);
        boolean success = studentDao.insert(student);
        if (!success) {
            throw new SQLException("Failed to save student record to database.");
        }
    }

    /**
     * Validates and updates an existing student record.
     */
    public void updateStudent(Student student) throws IllegalArgumentException, SQLException {
        if (student.getId() <= 0) {
            throw new IllegalArgumentException("Student ID must be greater than zero for update.");
        }
        validateStudent(student, student.getId());
        boolean success = studentDao.update(student);
        if (!success) {
            throw new SQLException("Failed to update student record. Student not found or no changes made.");
        }
    }

    /**
     * Deletes a student record.
     */
    public void deleteStudent(int studentId) throws SQLException {
        boolean success = studentDao.delete(studentId);
        if (!success) {
            throw new SQLException("Failed to delete student record. Student not found.");
        }
    }

    /**
     * Retrieves all students.
     */
    public List<Student> getAllStudents() throws SQLException {
        return studentDao.findAll();
    }

    /**
     * Retrieves students by semester.
     */
    public List<Student> getStudentsBySemester(int semester) throws SQLException {
        return studentDao.findBySemester(semester);
    }

    /**
     * Searches students by query (name or roll number) and optional semester.
     */
    public List<Student> searchStudents(String query, Integer semester) throws SQLException {
        return studentDao.search(query, semester);
    }

    /**
     * Finds a student by ID.
     */
    public Student getStudentById(int id) throws SQLException {
        return studentDao.findById(id);
    }

    /**
     * Counts total students for dashboard.
     */
    public int getTotalStudentCount() throws SQLException {
        return studentDao.countTotalStudents();
    }

    /**
     * Validates student fields and prevents duplicate roll numbers.
     */
    public void validateStudent(Student student, int excludeId) throws IllegalArgumentException, SQLException {
        if (student == null) {
            throw new IllegalArgumentException("Student information cannot be null.");
        }

        if (!ValidationUtil.isValidRollNumber(student.getRollNumber())) {
            throw new IllegalArgumentException("Roll number is invalid. It must be alphanumeric and 2-30 characters.");
        }

        if (ValidationUtil.isNullOrBlank(student.getFirstName())) {
            throw new IllegalArgumentException("First name is required.");
        }

        if (!ValidationUtil.isValidSemester(student.getSemester())) {
            throw new IllegalArgumentException("Semester must be between 1 and 8.");
        }

        if (ValidationUtil.isNullOrBlank(student.getDepartment())) {
            throw new IllegalArgumentException("Department is required.");
        }

        if (ValidationUtil.isNullOrBlank(student.getBatchYear())) {
            throw new IllegalArgumentException("Batch year is required (e.g. 2024-2028).");
        }

        if (!ValidationUtil.isValidEmail(student.getEmail(), false)) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        if (!ValidationUtil.isValidPhone(student.getPhone(), false)) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }

        // Prevent duplicate roll numbers
        if (studentDao.existsByRollNumber(student.getRollNumber(), excludeId)) {
            throw new IllegalArgumentException("A student with Roll Number '" + student.getRollNumber().trim() + "' already exists.");
        }
    }
}

