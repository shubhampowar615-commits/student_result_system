package com.studentresult.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying input validation routines across the system.
 */
class ValidationUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"2024CS001", "CS-101", "21_IT_45", "R12345"})
    @DisplayName("Should accept valid roll number formats")
    void testValidRollNumbers(String roll) {
        assertTrue(ValidationUtil.isValidRollNumber(roll));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "a", "roll with spaces", "roll@#$%", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("Should reject invalid roll number formats")
    void testInvalidRollNumbers(String roll) {
        assertFalse(ValidationUtil.isValidRollNumber(roll));
    }

    @Test
    @DisplayName("Should validate email formats")
    void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("student@college.edu", true));
        assertTrue(ValidationUtil.isValidEmail("john.doe@sub.domain.com", true));
        assertFalse(ValidationUtil.isValidEmail("invalid-email", true));
        assertFalse(ValidationUtil.isValidEmail("", true));
        assertTrue(ValidationUtil.isValidEmail("", false)); // optional blank is valid
    }

    @Test
    @DisplayName("Should validate marks within legitimate 0 to max bounds")
    void testMarksRangeValidation() {
        assertTrue(ValidationUtil.isValidMarks(0.0, 100.0));
        assertTrue(ValidationUtil.isValidMarks(50.0, 100.0));
        assertTrue(ValidationUtil.isValidMarks(100.0, 100.0));
        assertFalse(ValidationUtil.isValidMarks(-1.0, 100.0));
        assertFalse(ValidationUtil.isValidMarks(100.1, 100.0));
    }

    @Test
    @DisplayName("Should validate subject marks constraints")
    void testSubjectMarksValidation() {
        assertTrue(ValidationUtil.isValidSubjectMarks(100.0, 40.0));
        assertTrue(ValidationUtil.isValidSubjectMarks(50.0, 20.0));
        assertFalse(ValidationUtil.isValidSubjectMarks(0.0, 0.0));     // maxMarks must be > 0
        assertFalse(ValidationUtil.isValidSubjectMarks(100.0, 105.0)); // passMarks > maxMarks
        assertFalse(ValidationUtil.isValidSubjectMarks(100.0, -5.0));  // passMarks < 0
    }

    @Test
    @DisplayName("Should validate semester bounds (1 to 8)")
    void testSemesterValidation() {
        assertTrue(ValidationUtil.isValidSemester(1));
        assertTrue(ValidationUtil.isValidSemester(8));
        assertFalse(ValidationUtil.isValidSemester(0));
        assertFalse(ValidationUtil.isValidSemester(-1));
        assertFalse(ValidationUtil.isValidSemester(15));
    }
}
