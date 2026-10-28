package com.studentresult.service;

import com.studentresult.model.GradeScale;
import com.studentresult.model.MarkEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying pure calculation logic, grade assignments,
 * and the strict distinction between incomplete and failed results.
 */
class CalculationServiceTest {

    private CalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new CalculationService();
    }

    private MarkEntry createEntry(String code, double max, double pass, Double obtained, boolean absent) {
        MarkEntry entry = new MarkEntry();
        entry.setSubjectCode(code);
        entry.setMaxMarks(max);
        entry.setPassMarks(pass);
        entry.setMarksObtained(obtained);
        entry.setAbsent(absent);
        return entry;
    }

    @Test
    @DisplayName("Should evaluate PASS with Outstanding 'O' grade when percentage >= 90")
    void testPassOutstandingGrade() {
        List<MarkEntry> marks = List.of(
            createEntry("CS101", 100.0, 40.0, 95.0, false),
            createEntry("CS102", 100.0, 40.0, 92.0, false),
            createEntry("CS103", 100.0, 40.0, 90.0, false)
        );

        CalculationService.Evaluation eval = calculationService.evaluate(marks);

        assertEquals(277.0, eval.getTotalObtained());
        assertEquals(300.0, eval.getTotalMax());
        assertEquals(92.33, eval.getPercentage(), 0.01);
        assertEquals(GradeScale.STATUS_PASS, eval.getStatus());
        assertEquals(GradeScale.GRADE_OUTSTANDING, eval.getGrade());
        assertEquals(3, eval.getPassedSubjects());
        assertEquals(0, eval.getFailedSubjects());
        assertEquals(0, eval.getAbsentSubjects());
    }

    @Test
    @DisplayName("Should evaluate PASS with 'A+' grade when percentage is between 80 and 89.9")
    void testPassExcellentGrade() {
        List<MarkEntry> marks = List.of(
            createEntry("CS101", 100.0, 40.0, 85.0, false),
            createEntry("CS102", 100.0, 40.0, 82.0, false),
            createEntry("CS103", 100.0, 40.0, 88.0, false)
        );

        CalculationService.Evaluation eval = calculationService.evaluate(marks);

        assertEquals(255.0, eval.getTotalObtained());
        assertEquals(300.0, eval.getTotalMax());
        assertEquals(85.0, eval.getPercentage());
        assertEquals(GradeScale.STATUS_PASS, eval.getStatus());
        assertEquals(GradeScale.GRADE_EXCELLENT, eval.getGrade());
    }

    @Test
    @DisplayName("Should evaluate FAIL when student fails in any single subject even if total marks are high")
    void testFailWhenOneSubjectBelowPassingThreshold() {
        List<MarkEntry> marks = List.of(
            createEntry("CS101", 100.0, 40.0, 95.0, false),
            createEntry("CS102", 100.0, 40.0, 90.0, false),
            createEntry("CS103", 100.0, 40.0, 32.0, false) // Failed (32 < 40)
        );

        CalculationService.Evaluation eval = calculationService.evaluate(marks);

        assertEquals(217.0, eval.getTotalObtained());
        assertEquals(300.0, eval.getTotalMax());
        assertEquals(GradeScale.STATUS_FAIL, eval.getStatus());
        assertEquals(GradeScale.GRADE_FAIL, eval.getGrade());
        assertEquals(2, eval.getPassedSubjects());
        assertEquals(1, eval.getFailedSubjects());
    }

    @Test
    @DisplayName("Should evaluate INCOMPLETE (not FAIL) when student is absent in any subject")
    void testIncompleteWhenStudentIsAbsent() {
        List<MarkEntry> marks = List.of(
            createEntry("CS101", 100.0, 40.0, 85.0, false),
            createEntry("CS102", 100.0, 40.0, null, true), // Marked Absent
            createEntry("CS103", 100.0, 40.0, 78.0, false)
        );

        CalculationService.Evaluation eval = calculationService.evaluate(marks);

        assertEquals(163.0, eval.getTotalObtained());
        assertEquals(300.0, eval.getTotalMax());
        // Must be INCOMPLETE, not FAIL
        assertEquals(GradeScale.STATUS_INCOMPLETE, eval.getStatus());
        assertEquals(GradeScale.GRADE_INCOMPLETE, eval.getGrade());
        assertEquals(1, eval.getAbsentSubjects());
        assertTrue(eval.isIncomplete());
        assertFalse(eval.isFail());
    }

    @Test
    @DisplayName("Should evaluate INCOMPLETE (not FAIL) when subject marks are missing/unentered")
    void testIncompleteWhenMarksAreUnentered() {
        List<MarkEntry> marks = List.of(
            createEntry("CS101", 100.0, 40.0, 85.0, false),
            createEntry("CS102", 100.0, 40.0, null, false), // Marks not entered
            createEntry("CS103", 100.0, 40.0, 78.0, false)
        );

        CalculationService.Evaluation eval = calculationService.evaluate(marks);

        assertEquals(GradeScale.STATUS_INCOMPLETE, eval.getStatus());
        assertEquals(GradeScale.GRADE_INCOMPLETE, eval.getGrade());
        assertEquals(1, eval.getMissingSubjects());
        assertTrue(eval.isIncomplete());
    }

    @Test
    @DisplayName("Should handle empty marks list gracefully as INCOMPLETE")
    void testEmptyListEvaluation() {
        CalculationService.Evaluation eval = calculationService.evaluate(Collections.emptyList());

        assertEquals(0.0, eval.getTotalObtained());
        assertEquals(0.0, eval.getTotalMax());
        assertEquals(0.0, eval.getPercentage());
        assertEquals(GradeScale.STATUS_INCOMPLETE, eval.getStatus());
    }

    @Test
    @DisplayName("Should verify round method half-up rounding")
    void testRounding() {
        assertEquals(75.56, CalculationService.round(75.556, 2));
        assertEquals(75.55, CalculationService.round(75.554, 2));
    }
}

