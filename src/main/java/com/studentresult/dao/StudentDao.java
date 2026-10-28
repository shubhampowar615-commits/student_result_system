package com.studentresult.dao;

import com.studentresult.model.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student CRUD operations and searching.
 */
public class StudentDao {

    /**
     * Inserts a new student record into the database.
     */
    public boolean insert(Student student) throws SQLException {
        String sql = """
            INSERT INTO students (roll_number, first_name, last_name, email, phone, semester, department, batch_year)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getRollNumber().trim());
            ps.setString(2, student.getFirstName().trim());
            ps.setString(3, student.getLastName() != null ? student.getLastName().trim() : "");
            ps.setString(4, student.getEmail() != null ? student.getEmail().trim() : "");
            ps.setString(5, student.getPhone() != null ? student.getPhone().trim() : "");
            ps.setInt(6, student.getSemester());
            ps.setString(7, student.getDepartment() != null ? student.getDepartment().trim() : "");
            ps.setString(8, student.getBatchYear() != null ? student.getBatchYear().trim() : "");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        student.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing student record.
     */
    public boolean update(Student student) throws SQLException {
        String sql = """
            UPDATE students 
            SET roll_number = ?, first_name = ?, last_name = ?, email = ?, phone = ?, 
                semester = ?, department = ?, batch_year = ?
            WHERE id = ?;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getRollNumber().trim());
            ps.setString(2, student.getFirstName().trim());
            ps.setString(3, student.getLastName() != null ? student.getLastName().trim() : "");
            ps.setString(4, student.getEmail() != null ? student.getEmail().trim() : "");
            ps.setString(5, student.getPhone() != null ? student.getPhone().trim() : "");
            ps.setInt(6, student.getSemester());
            ps.setString(7, student.getDepartment() != null ? student.getDepartment().trim() : "");
            ps.setString(8, student.getBatchYear() != null ? student.getBatchYear().trim() : "");
            ps.setInt(9, student.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a student and cascades related marks and results.
     */
    public boolean delete(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds a student by primary key ID.
     */
    public Student findById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds a student by roll number (case-insensitive).
     */
    public Student findByRollNumber(String rollNumber) throws SQLException {
        String sql = "SELECT * FROM students WHERE roll_number = ? COLLATE NOCASE;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber != null ? rollNumber.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Checks if a roll number already exists, optionally excluding a student ID (for update checks).
     */
    public boolean existsByRollNumber(String rollNumber, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE roll_number = ? COLLATE NOCASE AND id != ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber != null ? rollNumber.trim() : "");
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all students, ordered by roll number ascending.
     */
    public List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY roll_number ASC;";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves students enrolled in a specific semester.
     */
    public List<Student> findBySemester(int semester) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE semester = ? ORDER BY roll_number ASC;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Searches students by name (first or last) or roll number, with optional semester filter.
     */
    public List<Student> search(String query, Integer semester) throws SQLException {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1 ");
        
        boolean hasQuery = query != null && !query.trim().isEmpty();
        if (hasQuery) {
            sql.append("AND (roll_number LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR (first_name || ' ' || last_name) LIKE ?) ");
        }
        if (semester != null && semester > 0) {
            sql.append("AND semester = ? ");
        }
        sql.append("ORDER BY roll_number ASC;");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasQuery) {
                String like = "%" + query.trim() + "%";
                ps.setString(paramIndex++, like);
                ps.setString(paramIndex++, like);
                ps.setString(paramIndex++, like);
                ps.setString(paramIndex++, like);
            }
            if (semester != null && semester > 0) {
                ps.setInt(paramIndex++, semester);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Returns total number of registered students.
     */
    public int countTotalStudents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students;";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("id"),
            rs.getString("roll_number"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getInt("semester"),
            rs.getString("department"),
            rs.getString("batch_year"),
            rs.getString("created_at")
        );
    }
}

