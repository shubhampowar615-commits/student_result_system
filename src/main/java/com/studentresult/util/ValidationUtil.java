package com.studentresult.util;

import java.util.regex.Pattern;

/**
 * Utility providing input validation methods across the application.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[0-9+()\\s-]{7,15}$");

    private static final Pattern ROLL_NUMBER_PATTERN = 
        Pattern.compile("^[A-Za-z0-9/_-]{2,30}$");

    /**
     * Checks if a string is null, empty, or whitespace-only.
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validates student roll number format.
     */
    public static boolean isValidRollNumber(String rollNumber) {
        if (isNullOrBlank(rollNumber)) {
            return false;
        }
        return ROLL_NUMBER_PATTERN.matcher(rollNumber.trim()).matches();
    }

    /**
     * Validates optional email. Blank emails are considered valid if optional.
     */
    public static boolean isValidEmail(String email, boolean required) {
        if (isNullOrBlank(email)) {
            return !required;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates optional phone number. Blank phone is valid if optional.
     */
    public static boolean isValidPhone(String phone, boolean required) {
        if (isNullOrBlank(phone)) {
            return !required;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates marks obtained against maximum marks.
     * Marks must be within [0, maxMarks].
     */
    public static boolean isValidMarks(double marks, double maxMarks) {
        return marks >= 0.0 && marks <= maxMarks;
    }

    /**
     * Validates subject configuration (max marks must be positive, pass marks <= max marks).
     */
    public static boolean isValidSubjectMarks(double maxMarks, double passMarks) {
        return maxMarks > 0.0 && passMarks >= 0.0 && passMarks <= maxMarks;
    }

    /**
     * Validates semester number (standard college semester 1 to 8).
     */
    public static boolean isValidSemester(int semester) {
        return semester >= 1 && semester <= 12;
    }
}

