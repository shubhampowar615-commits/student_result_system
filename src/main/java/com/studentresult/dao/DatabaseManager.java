package com.studentresult.dao;

import com.studentresult.util.PasswordHasher;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection, table creation, foreign keys,
 * and initial seeding of administrator accounts and sample default subjects.
 */
public class DatabaseManager {

    private static String dbUrl = "jdbc:sqlite:student_results.db";

    /**
     * Allows overriding the DB URL (e.g., for in-memory testing).
     */
    public static synchronized void setDbUrl(String newUrl) {
        dbUrl = newUrl;
    }

    public static synchronized String getDbUrl() {
        return dbUrl;
    }

    /**
     * Obtains a new connection to the SQLite database.
     * Always ensures PRAGMA foreign_keys = ON.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes all database tables and seeds defaults if they don't exist.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createTables(conn);
            seedDefaultAdmin(conn);
            seedDefaultSubjects(conn);
            seedSampleStudentsAndResultsIfEmpty(conn);
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'ADMIN',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 2. Students Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    roll_number TEXT UNIQUE NOT NULL COLLATE NOCASE,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    semester INTEGER NOT NULL,
                    department TEXT NOT NULL,
                    batch_year TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 3. Subjects Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS subjects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    subject_code TEXT UNIQUE NOT NULL COLLATE NOCASE,
                    subject_name TEXT NOT NULL,
                    semester INTEGER NOT NULL,
                    max_marks REAL NOT NULL,
                    pass_marks REAL NOT NULL,
                    credits INTEGER NOT NULL DEFAULT 3
                );
            """);

            // 4. Marks Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS marks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    subject_id INTEGER NOT NULL,
                    semester INTEGER NOT NULL,
                    marks_obtained REAL,
                    is_absent INTEGER NOT NULL DEFAULT 0,
                    exam_date TEXT,
                    remarks TEXT,
                    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                    UNIQUE(student_id, subject_id, semester)
                );
            """);

            // 5. Results Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS results (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    semester INTEGER NOT NULL,
                    total_obtained REAL NOT NULL,
                    total_max REAL NOT NULL,
                    percentage REAL NOT NULL,
                    grade TEXT NOT NULL,
                    status TEXT NOT NULL,
                    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                    UNIQUE(student_id, semester)
                );
            """);
        }
    }

    /**
     * Seeds a default admin account (username: admin, password: admin123) if no users exist.
     */
    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String salt = PasswordHasher.generateSalt();
                String hash = PasswordHasher.hashPassword("admin123", salt);

                String insertSql = "INSERT INTO users (username, password_hash, salt, full_name, role) VALUES (?, ?, ?, ?, ?);";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, hash);
                    ps.setString(3, salt);
                    ps.setString(4, "System Administrator");
                    ps.setString(5, "ADMIN");
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Seeds editable sample default subjects for Semesters 1 through 4 if subjects table is empty.
     */
    public static void seedDefaultSubjects(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM subjects;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertDefaultSubjectsList(conn);
            }
        }
    }

    public static void insertDefaultSubjectsList(Connection conn) throws SQLException {
        String insertSql = "INSERT OR IGNORE INTO subjects (subject_code, subject_name, semester, max_marks, pass_marks, credits) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            // Semester 1 Subjects
            addSubjectBatch(ps, "CS101", "Engineering Mathematics I", 1, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS102", "Problem Solving & C Programming", 1, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS103", "Digital Logic & Computer Systems", 1, 100.0, 40.0, 3);
            addSubjectBatch(ps, "CS104", "Engineering Physics", 1, 100.0, 40.0, 3);
            addSubjectBatch(ps, "CS105", "Technical Communication & English", 1, 50.0, 20.0, 2);

            // Semester 2 Subjects
            addSubjectBatch(ps, "CS201", "Discrete Mathematics", 2, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS202", "Data Structures & Algorithms", 2, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS203", "Object Oriented Programming (Java)", 2, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS204", "Computer Architecture & Organization", 2, 100.0, 40.0, 3);
            addSubjectBatch(ps, "CS205", "Environmental Science & Sustainability", 2, 50.0, 20.0, 2);

            // Semester 3 Subjects
            addSubjectBatch(ps, "CS301", "Database Management Systems", 3, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS302", "Operating Systems", 3, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS303", "Computer Networks", 3, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS304", "Software Engineering Principles", 3, 100.0, 40.0, 3);
            addSubjectBatch(ps, "CS305", "Web Technologies Lab", 3, 50.0, 20.0, 2);

            // Semester 4 Subjects
            addSubjectBatch(ps, "CS401", "Design & Analysis of Algorithms", 4, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS402", "Theory of Computation", 4, 100.0, 40.0, 3);
            addSubjectBatch(ps, "CS403", "Artificial Intelligence Basics", 4, 100.0, 40.0, 4);
            addSubjectBatch(ps, "CS404", "Cyber Security & Cryptography", 4, 100.0, 40.0, 3);
            addSubjectBatch(ps, "CS405", "Python Programming Lab", 4, 50.0, 20.0, 2);

            ps.executeBatch();
        }
    }

    private static void addSubjectBatch(PreparedStatement ps, String code, String name, 
                                        int sem, double max, double pass, int credits) throws SQLException {
        ps.setString(1, code);
        ps.setString(2, name);
        ps.setInt(3, sem);
        ps.setDouble(4, max);
        ps.setDouble(5, pass);
        ps.setInt(6, credits);
        ps.addBatch();
    }

    /**
     * Seeds a few sample student records and evaluates their results if the student table is empty.
     * This allows immediate demonstration and testing of all features out of the box.
     */
    private static void seedSampleStudentsAndResultsIfEmpty(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM students;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert Sample Students
                String insertStudentSql = """
                    INSERT INTO students (roll_number, first_name, last_name, email, phone, semester, department, batch_year)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;
                try (PreparedStatement ps = conn.prepareStatement(insertStudentSql)) {
                    // Student 1: Aarav Sharma (Semester 1, high scorer)
                    ps.setString(1, "2024CS001");
                    ps.setString(2, "Aarav");
                    ps.setString(3, "Sharma");
                    ps.setString(4, "aarav.sharma@college.edu");
                    ps.setString(5, "9876543210");
                    ps.setInt(6, 1);
                    ps.setString(7, "Computer Science");
                    ps.setString(8, "2024-2028");
                    ps.executeUpdate();

                    // Student 2: Diya Patel (Semester 1, passed)
                    ps.setString(1, "2024CS002");
                    ps.setString(2, "Diya");
                    ps.setString(3, "Patel");
                    ps.setString(4, "diya.patel@college.edu");
                    ps.setString(5, "9876543211");
                    ps.setInt(6, 1);
                    ps.setString(7, "Computer Science");
                    ps.setString(8, "2024-2028");
                    ps.executeUpdate();

                    // Student 3: Rohan Gupta (Semester 1, failed in one subject)
                    ps.setString(1, "2024CS003");
                    ps.setString(2, "Rohan");
                    ps.setString(3, "Gupta");
                    ps.setString(4, "rohan.gupta@college.edu");
                    ps.setString(5, "9876543212");
                    ps.setInt(6, 1);
                    ps.setString(7, "Computer Science");
                    ps.setString(8, "2024-2028");
                    ps.executeUpdate();

                    // Student 4: Ananya Iyer (Semester 1, absent in one subject -> INCOMPLETE)
                    ps.setString(1, "2024CS004");
                    ps.setString(2, "Ananya");
                    ps.setString(3, "Iyer");
                    ps.setString(4, "ananya.iyer@college.edu");
                    ps.setString(5, "9876543213");
                    ps.setInt(6, 1);
                    ps.setString(7, "Computer Science");
                    ps.setString(8, "2024-2028");
                    ps.executeUpdate();

                    // Student 5: Vikram Verma (Semester 2, high scorer)
                    ps.setString(1, "2023CS015");
                    ps.setString(2, "Vikram");
                    ps.setString(3, "Verma");
                    ps.setString(4, "vikram.v@college.edu");
                    ps.setString(5, "9876543214");
                    ps.setInt(6, 2);
                    ps.setString(7, "Information Technology");
                    ps.setString(8, "2023-2027");
                    ps.executeUpdate();
                }

                // Retrieve subject IDs for Semester 1: CS101..CS105
                int cs101 = getSubjectId(conn, "CS101");
                int cs102 = getSubjectId(conn, "CS102");
                int cs103 = getSubjectId(conn, "CS103");
                int cs104 = getSubjectId(conn, "CS104");
                int cs105 = getSubjectId(conn, "CS105");

                // Seed marks for Student 1 (Aarav): Pass with O Grade
                insertMark(conn, 1, cs101, 1, 92.0, 0, "2024-12-10", "Excellent");
                insertMark(conn, 1, cs102, 1, 95.0, 0, "2024-12-12", "Outstanding");
                insertMark(conn, 1, cs103, 1, 88.0, 0, "2024-12-14", "Very Good");
                insertMark(conn, 1, cs104, 1, 90.0, 0, "2024-12-16", "Great work");
                insertMark(conn, 1, cs105, 1, 46.0, 0, "2024-12-18", "Fluent");
                insertResult(conn, 1, 1, 411.0, 450.0, 91.33, "O", "PASS");

                // Seed marks for Student 2 (Diya): Pass with A Grade
                insertMark(conn, 2, cs101, 1, 75.0, 0, "2024-12-10", "Good");
                insertMark(conn, 2, cs102, 1, 78.0, 0, "2024-12-12", "Good");
                insertMark(conn, 2, cs103, 1, 72.0, 0, "2024-12-14", "Good");
                insertMark(conn, 2, cs104, 1, 70.0, 0, "2024-12-16", "Good");
                insertMark(conn, 2, cs105, 1, 38.0, 0, "2024-12-18", "Good");
                insertResult(conn, 2, 1, 333.0, 450.0, 74.00, "A", "PASS");

                // Seed marks for Student 3 (Rohan): Failed in CS104 (scored 28 < 40)
                insertMark(conn, 3, cs101, 1, 55.0, 0, "2024-12-10", "Average");
                insertMark(conn, 3, cs102, 1, 60.0, 0, "2024-12-12", "Average");
                insertMark(conn, 3, cs103, 1, 45.0, 0, "2024-12-14", "Needs improvement");
                insertMark(conn, 3, cs104, 1, 28.0, 0, "2024-12-16", "Fail in Theory");
                insertMark(conn, 3, cs105, 1, 30.0, 0, "2024-12-18", "Pass");
                insertResult(conn, 3, 1, 218.0, 450.0, 48.44, "F", "FAIL");

                // Seed marks for Student 4 (Ananya): Absent in CS103 -> INCOMPLETE
                insertMark(conn, 4, cs101, 1, 80.0, 0, "2024-12-10", "Good");
                insertMark(conn, 4, cs102, 1, 85.0, 0, "2024-12-12", "Very Good");
                insertMark(conn, 4, cs103, 1, null, 1, "2024-12-14", "Medical Leave");
                insertMark(conn, 4, cs104, 1, 82.0, 0, "2024-12-16", "Good");
                insertMark(conn, 4, cs105, 1, 40.0, 0, "2024-12-18", "Good");
                insertResult(conn, 4, 1, 287.0, 450.0, 63.78, "I", "INCOMPLETE");
            }
        }
    }

    private static int getSubjectId(Connection conn, String code) throws SQLException {
        String sql = "SELECT id FROM subjects WHERE subject_code = ? COLLATE NOCASE;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private static void insertMark(Connection conn, int studentId, int subjectId, int sem, 
                                   Double marks, int absent, String date, String remarks) throws SQLException {
        String sql = "INSERT INTO marks (student_id, subject_id, semester, marks_obtained, is_absent, exam_date, remarks) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, sem);
            if (marks != null) {
                ps.setDouble(4, marks);
            } else {
                ps.setNull(4, java.sql.Types.DOUBLE);
            }
            ps.setInt(5, absent);
            ps.setString(6, date);
            ps.setString(7, remarks);
            ps.executeUpdate();
        }
    }

    private static void insertResult(Connection conn, int studentId, int sem, double totalObtained, 
                                     double totalMax, double percentage, String grade, String status) throws SQLException {
        String sql = "INSERT INTO results (student_id, semester, total_obtained, total_max, percentage, grade, status) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sem);
            ps.setDouble(3, totalObtained);
            ps.setDouble(4, totalMax);
            ps.setDouble(5, percentage);
            ps.setString(6, grade);
            ps.setString(7, status);
            ps.executeUpdate();
        }
    }
}

