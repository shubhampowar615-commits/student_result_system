package com.studentresult.dao;

import com.studentresult.model.MarkEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Marks Entry and Subject-wise Evaluation storage.
 */
public class MarkDao {

    /**
     * Saves or updates a student's mark entry for a specific subject and semester.
     * Uses SQLite's ON CONFLICT(student_id, subject_id, semester) DO UPDATE.
     */
    public boolean upsertMark(MarkEntry entry) throws SQLException {
        String sql = """
            INSERT INTO marks (student_id, subject_id, semester, marks_obtained, is_absent, exam_date, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(student_id, subject_id, semester) 
            DO UPDATE SET 
                marks_obtained = excluded.marks_obtained,
                is_absent = excluded.is_absent,
                exam_date = excluded.exam_date,
                remarks = excluded.remarks;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entry.getStudentId());
            ps.setInt(2, entry.getSubjectId());
            ps.setInt(3, entry.getSemester());
            if (entry.getMarksObtained() != null) {
                ps.setDouble(4, entry.getMarksObtained());
            } else {
                ps.setNull(4, Types.DOUBLE);
            }
            ps.setInt(5, entry.isAbsent() ? 1 : 0);
            ps.setString(6, entry.getExamDate());
            ps.setString(7, entry.getRemarks());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Saves a batch of mark entries inside a single database transaction.
     */
    public boolean saveMarksBatch(List<MarkEntry> entries) throws SQLException {
        if (entries == null || entries.isEmpty()) {
            return false;
        }

        String sql = """
            INSERT INTO marks (student_id, subject_id, semester, marks_obtained, is_absent, exam_date, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(student_id, subject_id, semester) 
            DO UPDATE SET 
                marks_obtained = excluded.marks_obtained,
                is_absent = excluded.is_absent,
                exam_date = excluded.exam_date,
                remarks = excluded.remarks;
        """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (MarkEntry entry : entries) {
                    ps.setInt(1, entry.getStudentId());
                    ps.setInt(2, entry.getSubjectId());
                    ps.setInt(3, entry.getSemester());
                    if (entry.getMarksObtained() != null && !entry.isAbsent()) {
                        ps.setDouble(4, entry.getMarksObtained());
                    } else {
                        ps.setNull(4, Types.DOUBLE);
                    }
                    ps.setInt(5, entry.isAbsent() ? 1 : 0);
                    ps.setString(6, entry.getExamDate());
                    ps.setString(7, entry.getRemarks());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves all mark entries for a student and semester, joined with subject metadata.
     * Selects s.id AS subject_id so the foreign key reference to subjects is always valid,
     * even when the student has no recorded marks in the marks table yet.
     */
    public List<MarkEntry> findByStudentAndSemester(int studentId, int semester) throws SQLException {
        List<MarkEntry> list = new ArrayList<>();
        String sql = """
            SELECT s.id AS subject_id, m.id AS mark_id, m.student_id, m.semester, m.marks_obtained, m.is_absent, 
                   m.exam_date, m.remarks, s.subject_code, s.subject_name, s.max_marks, 
                   s.pass_marks, s.credits
            FROM subjects s
            LEFT JOIN marks m ON s.id = m.subject_id AND m.student_id = ? AND m.semester = ?
            WHERE s.semester = ?
            ORDER BY s.subject_code ASC;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, semester);
            ps.setInt(3, semester);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MarkEntry entry = new MarkEntry();
                    entry.setId(rs.getInt("mark_id"));
                    entry.setStudentId(studentId);
                    entry.setSubjectId(rs.getInt("subject_id"));
                    entry.setSemester(semester);
                    
                    double marks = rs.getDouble("marks_obtained");
                    if (rs.wasNull()) {
                        entry.setMarksObtained(null);
                    } else {
                        entry.setMarksObtained(marks);
                    }

                    entry.setAbsent(rs.getInt("is_absent") == 1);
                    entry.setExamDate(rs.getString("exam_date"));
                    entry.setRemarks(rs.getString("remarks"));

                    // Subject metadata
                    entry.setSubjectCode(rs.getString("subject_code"));
                    entry.setSubjectName(rs.getString("subject_name"));
                    entry.setMaxMarks(rs.getDouble("max_marks"));
                    entry.setPassMarks(rs.getDouble("pass_marks"));
                    entry.setCredits(rs.getInt("credits"));

                    list.add(entry);
                }
            }
        }
        return list;
    }

    /**
     * Deletes all marks for a student in a semester.
     */
    public boolean deleteByStudentAndSemester(int studentId, int semester) throws SQLException {
        String sql = "DELETE FROM marks WHERE student_id = ? AND semester = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, semester);
            return ps.executeUpdate() > 0;
        }
    }
}

