package com.studentresult.dao;

import com.studentresult.model.Subject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Subject curriculum management.
 */
public class SubjectDao {

    /**
     * Inserts a new subject into the curriculum.
     */
    public boolean insert(Subject subject) throws SQLException {
        String sql = """
            INSERT INTO subjects (subject_code, subject_name, semester, max_marks, pass_marks, credits)
            VALUES (?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subject.getSubjectCode().trim().toUpperCase());
            ps.setString(2, subject.getSubjectName().trim());
            ps.setInt(3, subject.getSemester());
            ps.setDouble(4, subject.getMaxMarks());
            ps.setDouble(5, subject.getPassMarks());
            ps.setInt(6, subject.getCredits());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        subject.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing subject.
     */
    public boolean update(Subject subject) throws SQLException {
        String sql = """
            UPDATE subjects
            SET subject_code = ?, subject_name = ?, semester = ?, max_marks = ?, pass_marks = ?, credits = ?
            WHERE id = ?;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getSubjectCode().trim().toUpperCase());
            ps.setString(2, subject.getSubjectName().trim());
            ps.setInt(3, subject.getSemester());
            ps.setDouble(4, subject.getMaxMarks());
            ps.setDouble(5, subject.getPassMarks());
            ps.setInt(6, subject.getCredits());
            ps.setInt(7, subject.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a subject by ID.
     */
    public boolean delete(int subjectId) throws SQLException {
        String sql = "DELETE FROM subjects WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds a subject by primary key ID.
     */
    public Subject findById(int id) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE id = ?;";
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
     * Finds a subject by its unique code (case-insensitive).
     */
    public Subject findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE subject_code = ? COLLATE NOCASE;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code != null ? code.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Checks if a subject code already exists, excluding a specific ID for edits.
     */
    public boolean existsByCode(String code, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subjects WHERE subject_code = ? COLLATE NOCASE AND id != ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code != null ? code.trim() : "");
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
     * Retrieves all subjects ordered by semester and subject code.
     */
    public List<Subject> findAll() throws SQLException {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY semester ASC, subject_code ASC;";
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
     * Retrieves subjects configured for a specific semester.
     */
    public List<Subject> findBySemester(int semester) throws SQLException {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE semester = ? ORDER BY subject_code ASC;";
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
     * Returns total count of subjects in the database.
     */
    public int countTotalSubjects() throws SQLException {
        String sql = "SELECT COUNT(*) FROM subjects;";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Resets default subjects to sample list.
     */
    public void resetToDefaults() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseManager.insertDefaultSubjectsList(conn);
        }
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        return new Subject(
            rs.getInt("id"),
            rs.getString("subject_code"),
            rs.getString("subject_name"),
            rs.getInt("semester"),
            rs.getDouble("max_marks"),
            rs.getDouble("pass_marks"),
            rs.getInt("credits")
        );
    }
}

