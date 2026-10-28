package com.studentresult.dao;

import com.studentresult.model.Result;
import com.studentresult.model.ResultSummary;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Semester Results storage, filtering, and summary metrics.
 */
public class ResultDao {

    /**
     * Saves or updates a computed semester result for a student.
     */
    public boolean saveResult(Result result) throws SQLException {
        String sql = """
            INSERT INTO results (student_id, semester, total_obtained, total_max, percentage, grade, status, evaluated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(student_id, semester)
            DO UPDATE SET
                total_obtained = excluded.total_obtained,
                total_max = excluded.total_max,
                percentage = excluded.percentage,
                grade = excluded.grade,
                status = excluded.status,
                evaluated_at = CURRENT_TIMESTAMP;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, result.getStudentId());
            ps.setInt(2, result.getSemester());
            ps.setDouble(3, result.getTotalObtained());
            ps.setDouble(4, result.getTotalMax());
            ps.setDouble(5, result.getPercentage());
            ps.setString(6, result.getGrade());
            ps.setString(7, result.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        result.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a single result for a specific student and semester.
     */
    public Result findByStudentAndSemester(int studentId, int semester) throws SQLException {
        String sql = "SELECT * FROM results WHERE student_id = ? AND semester = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all results joined with student details for tabular display.
     */
    public List<ResultSummary> findAllSummaries() throws SQLException {
        return searchAndFilter(null, null, null);
    }

    /**
     * Searches and filters results by query (student name or roll number),
     * semester, and status (PASS, FAIL, INCOMPLETE).
     */
    public List<ResultSummary> searchAndFilter(String query, Integer semester, String status) throws SQLException {
        List<ResultSummary> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT r.id AS result_id, r.student_id, s.roll_number, 
                   (s.first_name || ' ' || s.last_name) AS student_name,
                   s.department, s.batch_year, r.semester, r.total_obtained, 
                   r.total_max, r.percentage, r.grade, r.status, r.evaluated_at
            FROM results r
            JOIN students s ON r.student_id = s.id
            WHERE 1=1
        """);

        boolean hasQuery = query != null && !query.trim().isEmpty();
        if (hasQuery) {
            sql.append(" AND (s.roll_number LIKE ? OR s.first_name LIKE ? OR s.last_name LIKE ? OR (s.first_name || ' ' || s.last_name) LIKE ?)");
        }
        if (semester != null && semester > 0) {
            sql.append(" AND r.semester = ?");
        }
        if (status != null && !status.equalsIgnoreCase("ALL") && !status.trim().isEmpty()) {
            sql.append(" AND r.status = ?");
        }
        sql.append(" ORDER BY r.semester ASC, s.roll_number ASC;");

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
            if (status != null && !status.equalsIgnoreCase("ALL") && !status.trim().isEmpty()) {
                ps.setString(paramIndex++, status.trim().toUpperCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ResultSummary summary = new ResultSummary(
                        rs.getInt("result_id"),
                        rs.getInt("student_id"),
                        rs.getString("roll_number"),
                        rs.getString("student_name"),
                        rs.getString("department"),
                        rs.getString("batch_year"),
                        rs.getInt("semester"),
                        rs.getDouble("total_obtained"),
                        rs.getDouble("total_max"),
                        rs.getDouble("percentage"),
                        rs.getString("grade"),
                        rs.getString("status"),
                        rs.getString("evaluated_at")
                    );
                    list.add(summary);
                }
            }
        }
        return list;
    }

    /**
     * Retrieves summary result for a single student and semester (used for report cards).
     */
    public ResultSummary getSummaryForStudent(int studentId, int semester) throws SQLException {
        String sql = """
            SELECT r.id AS result_id, r.student_id, s.roll_number, 
                   (s.first_name || ' ' || s.last_name) AS student_name,
                   s.department, s.batch_year, r.semester, r.total_obtained, 
                   r.total_max, r.percentage, r.grade, r.status, r.evaluated_at
            FROM results r
            JOIN students s ON r.student_id = s.id
            WHERE r.student_id = ? AND r.semester = ?;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ResultSummary(
                        rs.getInt("result_id"),
                        rs.getInt("student_id"),
                        rs.getString("roll_number"),
                        rs.getString("student_name"),
                        rs.getString("department"),
                        rs.getString("batch_year"),
                        rs.getInt("semester"),
                        rs.getDouble("total_obtained"),
                        rs.getDouble("total_max"),
                        rs.getDouble("percentage"),
                        rs.getString("grade"),
                        rs.getString("status"),
                        rs.getString("evaluated_at")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Deletes a result record.
     */
    public boolean deleteResult(int resultId) throws SQLException {
        String sql = "DELETE FROM results WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resultId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Dashboard statistics: counts total evaluated results, pass count, fail count, and incomplete count.
     */
    public int countTotalResults() throws SQLException {
        return countBySql("SELECT COUNT(*) FROM results;");
    }

    public int countPassedResults() throws SQLException {
        return countBySql("SELECT COUNT(*) FROM results WHERE status = 'PASS';");
    }

    public int countFailedResults() throws SQLException {
        return countBySql("SELECT COUNT(*) FROM results WHERE status = 'FAIL';");
    }

    public int countIncompleteResults() throws SQLException {
        return countBySql("SELECT COUNT(*) FROM results WHERE status = 'INCOMPLETE';");
    }

    private int countBySql(String sql) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Result mapRow(ResultSet rs) throws SQLException {
        return new Result(
            rs.getInt("id"),
            rs.getInt("student_id"),
            rs.getInt("semester"),
            rs.getDouble("total_obtained"),
            rs.getDouble("total_max"),
            rs.getDouble("percentage"),
            rs.getString("grade"),
            rs.getString("status"),
            rs.getString("evaluated_at")
        );
    }
}

