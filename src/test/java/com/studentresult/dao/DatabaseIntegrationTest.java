package com.studentresult.dao;

import com.studentresult.model.MarkEntry;
import com.studentresult.model.Result;
import com.studentresult.model.ResultSummary;
import com.studentresult.model.Student;
import com.studentresult.model.Subject;
import com.studentresult.service.CalculationService;
import com.studentresult.service.ResultService;
import com.studentresult.service.StudentService;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying SQLite database operations, prepared statements,
 * unique constraints (duplicate roll number prevention), marks persistence, and result calculation.
 */
class DatabaseIntegrationTest {

    private static final String TEST_DB_FILE = "target/test_student_results.db";
    private static StudentDao studentDao;
    private static SubjectDao subjectDao;
    private static MarkDao markDao;
    private static ResultDao resultDao;
    private static StudentService studentService;
    private static ResultService resultService;

    @BeforeAll
    static void initTestDatabase() {
        File dbFile = new File(TEST_DB_FILE);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        // Point DatabaseManager to isolated test DB
        DatabaseManager.setDbUrl("jdbc:sqlite:" + TEST_DB_FILE);
        DatabaseManager.initializeDatabase();

        studentDao = new StudentDao();
        subjectDao = new SubjectDao();
        markDao = new MarkDao();
        resultDao = new ResultDao();
        studentService = new StudentService(studentDao);
        resultService = new ResultService(markDao, resultDao, studentDao, subjectDao, new CalculationService());
    }

    @AfterAll
    static void cleanup() {
        // Reset DB URL
        DatabaseManager.setDbUrl("jdbc:sqlite:student_results.db");
        File dbFile = new File(TEST_DB_FILE);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    @DisplayName("Should prevent duplicate roll numbers and enforce unique constraints")
    void testDuplicateRollNumberPrevention() throws SQLException {
        Student student1 = new Student("TEST_ROLL_101", "Karan", "Mehta", "karan@test.com", "9998887771", 1, "CS", "2024-2028");
        studentService.addStudent(student1);
        assertTrue(student1.getId() > 0);

        // Attempt to insert duplicate roll number should throw IllegalArgumentException
        Student studentDuplicate = new Student("TEST_ROLL_101", "Another", "Student", "another@test.com", "9998887772", 1, "CS", "2024-2028");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            studentService.addStudent(studentDuplicate);
        });

        assertTrue(thrown.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should perform Student CRUD operations successfully")
    void testStudentCRUD() throws SQLException {
        Student student = new Student("TEST_ROLL_102", "Pooja", "Nair", "pooja@test.com", "9998887773", 2, "IT", "2024-2028");
        studentService.addStudent(student);

        // Read by roll number
        Student fetched = studentDao.findByRollNumber("TEST_ROLL_102");
        assertNotNull(fetched);
        assertEquals("Pooja", fetched.getFirstName());
        assertEquals("Nair", fetched.getLastName());

        // Update
        fetched.setFirstName("Pooja Updated");
        studentService.updateStudent(fetched);

        Student updated = studentDao.findById(fetched.getId());
        assertEquals("Pooja Updated", updated.getFirstName());

        // Delete
        studentService.deleteStudent(fetched.getId());
        assertNull(studentDao.findById(fetched.getId()));
    }

    @Test
    @DisplayName("Should configure and retrieve Semester subjects correctly")
    void testSubjectManagement() throws SQLException {
        Subject subject = new Subject("TEST_SUB_1", "Compiler Design", 5, 100.0, 40.0, 4);
        subjectDao.insert(subject);
        assertTrue(subject.getId() > 0);

        Subject fetched = subjectDao.findById(subject.getId());
        assertNotNull(fetched);
        assertEquals("TEST_SUB_1", fetched.getSubjectCode());
        assertEquals(100.0, fetched.getMaxMarks());
        assertEquals(40.0, fetched.getPassMarks());

        // Verify duplicate code check
        assertTrue(subjectDao.existsByCode("TEST_SUB_1", 0));
        assertFalse(subjectDao.existsByCode("TEST_SUB_1", subject.getId())); // Excludes itself
    }

    @Test
    @DisplayName("Should record marks batch, auto-calculate result, and persist summary")
    void testMarksEntryAndResultCalculation() throws SQLException {
        // Create test student
        Student student = new Student("TEST_ROLL_103", "Sameer", "Khan", "sameer@test.com", "9998887774", 3, "CS", "2023-2027");
        studentService.addStudent(student);

        // Fetch subjects for semester 3 (which was seeded by DatabaseManager)
        List<Subject> sem3Subjects = subjectDao.findBySemester(3);
        assertFalse(sem3Subjects.isEmpty());

        List<MarkEntry> marks = new java.util.ArrayList<>();
        for (Subject sub : sem3Subjects) {
            MarkEntry m = new MarkEntry();
            m.setSubjectId(sub.getId());
            m.setSubjectCode(sub.getSubjectCode());
            m.setSubjectName(sub.getSubjectName());
            m.setMaxMarks(sub.getMaxMarks());
            m.setPassMarks(sub.getPassMarks());
            m.setCredits(sub.getCredits());
            m.setMarksObtained(sub.getMaxMarks() * 0.85); // 85% scored
            m.setAbsent(false);
            marks.add(m);
        }

        // Save & calculate
        CalculationService.Evaluation eval = resultService.saveMarksAndCalculate(student.getId(), 3, marks);
        assertNotNull(eval);
        assertTrue(eval.isPass());
        assertEquals("A+", eval.getGrade());
        assertEquals(85.0, eval.getPercentage(), 0.1);

        // Verify summary retrieval for Report Card
        ResultSummary summary = resultService.getReportCardSummary(student.getId(), 3);
        assertNotNull(summary);
        assertEquals("TEST_ROLL_103", summary.getRollNumber());
        assertEquals(85.0, summary.getPercentage(), 0.1);
        assertEquals("PASS", summary.getStatus());
        assertEquals("A+", summary.getGrade());
    }

    @Test
    @DisplayName("Should successfully load and save marks for a newly added student with no foreign key errors")
    void testSaveMarksForNewlyAddedStudentWithoutForeignKeyError() throws SQLException {
        // 1. Add a brand new student
        Student newStudent = new Student("TEST_ROLL_NEW_999", "Rahul", "Dev", "rahul.dev@test.com", "9876500000", 1, "CS", "2024-2028");
        studentService.addStudent(newStudent);
        assertTrue(newStudent.getId() > 0, "Student ID must be generated");

        // 2. Load marks entry list (simulating the UI MarksEntryPanel)
        List<MarkEntry> entries = resultService.getMarksForEntry(newStudent.getId(), 1);
        assertFalse(entries.isEmpty(), "Semester 1 should have subjects");

        // Verify that every single entry has a valid positive subjectId (NOT 0)
        for (MarkEntry entry : entries) {
            assertTrue(entry.getSubjectId() > 0, "Subject ID must be a positive foreign key referring to subjects.id: " + entry.getSubjectCode());
            assertNotNull(subjectDao.findById(entry.getSubjectId()), "Referred subject must exist in subjects table");
            // Fill in valid marks
            entry.setMarksObtained(entry.getMaxMarks() * 0.75);
            entry.setAbsent(false);
            entry.setRemarks("Passed test");
        }

        // 3. Save marks and calculate result - must NOT throw SQLITE_CONSTRAINT_FOREIGNKEY
        CalculationService.Evaluation eval = assertDoesNotThrow(() -> {
            return resultService.saveMarksAndCalculate(newStudent.getId(), 1, entries);
        }, "Saving marks for newly added student must not throw foreign key constraint violation");

        assertNotNull(eval);
        assertTrue(eval.isPass());
        assertEquals("A", eval.getGrade());

        // 4. Verify marks and result in database
        ResultSummary summary = resultService.getReportCardSummary(newStudent.getId(), 1);
        assertNotNull(summary);
        assertEquals("TEST_ROLL_NEW_999", summary.getRollNumber());
        assertEquals("PASS", summary.getStatus());
        assertEquals(75.0, summary.getPercentage(), 0.1);
    }
}

