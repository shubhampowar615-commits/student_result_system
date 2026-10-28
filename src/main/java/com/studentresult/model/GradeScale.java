package com.studentresult.model;

/**
 * Standard Grading Scale for College Results.
 * Clearly labelled sample default grading system that can be referenced or adapted.
 */
public class GradeScale {

    public static final String GRADE_OUTSTANDING = "O";
    public static final String GRADE_EXCELLENT   = "A+";
    public static final String GRADE_VERY_GOOD   = "A";
    public static final String GRADE_GOOD        = "B+";
    public static final String GRADE_ABOVE_AVG   = "B";
    public static final String GRADE_PASS        = "C";
    public static final String GRADE_FAIL        = "F";
    public static final String GRADE_INCOMPLETE  = "I";

    public static final String STATUS_PASS       = "PASS";
    public static final String STATUS_FAIL       = "FAIL";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";

    /**
     * Determines letter grade based on percentage and pass/fail/incomplete status.
     * Note: Incomplete results are given grade 'I' and not 'F'.
     *
     * @param percentage percentage scored
     * @param status overall result status (PASS, FAIL, INCOMPLETE)
     * @return Grade string
     */
    public static String calculateGrade(double percentage, String status) {
        if (STATUS_INCOMPLETE.equalsIgnoreCase(status)) {
            return GRADE_INCOMPLETE;
        }
        if (STATUS_FAIL.equalsIgnoreCase(status)) {
            return GRADE_FAIL;
        }

        if (percentage >= 90.0) {
            return GRADE_OUTSTANDING; // 90 - 100
        } else if (percentage >= 80.0) {
            return GRADE_EXCELLENT;   // 80 - 89.99
        } else if (percentage >= 70.0) {
            return GRADE_VERY_GOOD;   // 70 - 79.99
        } else if (percentage >= 60.0) {
            return GRADE_GOOD;        // 60 - 69.99
        } else if (percentage >= 50.0) {
            return GRADE_ABOVE_AVG;   // 50 - 59.99
        } else if (percentage >= 40.0) {
            return GRADE_PASS;        // 40 - 49.99
        } else {
            return GRADE_FAIL;
        }
    }

    /**
     * Returns grade point representation for sample credit calculations.
     */
    public static double getGradePoint(String grade) {
        if (grade == null) return 0.0;
        return switch (grade.toUpperCase()) {
            case "O"  -> 10.0;
            case "A+" -> 9.0;
            case "A"  -> 8.0;
            case "B+" -> 7.0;
            case "B"  -> 6.0;
            case "C"  -> 5.0;
            default   -> 0.0;
        };
    }

    /**
     * Returns human-readable description for sample default rules.
     */
    public static String getGradingDescription() {
        return "Sample Default Grading Scale:\n" +
               "  90% - 100% : O  (Outstanding - 10 pts)\n" +
               "  80% - 89.9%: A+ (Excellent - 9 pts)\n" +
               "  70% - 79.9%: A  (Very Good - 8 pts)\n" +
               "  60% - 69.9%: B+ (Good - 7 pts)\n" +
               "  50% - 59.9%: B  (Above Average - 6 pts)\n" +
               "  40% - 49.9%: C  (Pass - 5 pts)\n" +
               "  < 40%      : F  (Fail - 0 pts)\n" +
               "  Absent/Miss: I  (Incomplete - 0 pts)";
    }
}

