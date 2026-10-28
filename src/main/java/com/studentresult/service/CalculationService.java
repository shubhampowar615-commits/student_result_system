package com.studentresult.service;

import com.studentresult.model.GradeScale;
import com.studentresult.model.MarkEntry;
import com.studentresult.model.Result;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service handling pure mathematical calculations, grading, pass/fail analysis,
 * and differentiation between incomplete and failed results.
 */
public class CalculationService {

    /**
     * Evaluation result container holding computed totals, percentage, grade, status, and remarks.
     */
    public static class Evaluation {
        private final double totalObtained;
        private final double totalMax;
        private final double percentage;
        private final String grade;
        private final String status; // PASS, FAIL, INCOMPLETE
        private final int totalSubjects;
        private final int passedSubjects;
        private final int failedSubjects;
        private final int absentSubjects;
        private final int missingSubjects;

        public Evaluation(double totalObtained, double totalMax, double percentage, 
                          String grade, String status, int totalSubjects, 
                          int passedSubjects, int failedSubjects, int absentSubjects, int missingSubjects) {
            this.totalObtained = totalObtained;
            this.totalMax = totalMax;
            this.percentage = percentage;
            this.grade = grade;
            this.status = status;
            this.totalSubjects = totalSubjects;
            this.passedSubjects = passedSubjects;
            this.failedSubjects = failedSubjects;
            this.absentSubjects = absentSubjects;
            this.missingSubjects = missingSubjects;
        }

        public double getTotalObtained() {
            return totalObtained;
        }

        public double getTotalMax() {
            return totalMax;
        }

        public double getPercentage() {
            return percentage;
        }

        public String getGrade() {
            return grade;
        }

        public String getStatus() {
            return status;
        }

        public int getTotalSubjects() {
            return totalSubjects;
        }

        public int getPassedSubjects() {
            return passedSubjects;
        }

        public int getFailedSubjects() {
            return failedSubjects;
        }

        public int getAbsentSubjects() {
            return absentSubjects;
        }

        public int getMissingSubjects() {
            return missingSubjects;
        }

        public boolean isPass() {
            return GradeScale.STATUS_PASS.equalsIgnoreCase(status);
        }

        public boolean isIncomplete() {
            return GradeScale.STATUS_INCOMPLETE.equalsIgnoreCase(status);
        }

        public boolean isFail() {
            return GradeScale.STATUS_FAIL.equalsIgnoreCase(status);
        }

        public Result toResult(int studentId, int semester) {
            return new Result(studentId, semester, totalObtained, totalMax, percentage, grade, status);
        }
    }

    /**
     * Evaluates a list of mark entries for a student's semester.
     * Enforces that incomplete results (absent or unentered marks) are treated
     * separately from failed results.
     *
     * @param markEntries List of subject mark entries
     * @return Evaluation object containing all calculated metrics
     */
    public Evaluation evaluate(List<MarkEntry> markEntries) {
        if (markEntries == null || markEntries.isEmpty()) {
            return new Evaluation(0.0, 0.0, 0.0, GradeScale.GRADE_INCOMPLETE, 
                                  GradeScale.STATUS_INCOMPLETE, 0, 0, 0, 0, 0);
        }

        double totalObtained = 0.0;
        double totalMax = 0.0;
        int passedCount = 0;
        int failedCount = 0;
        int absentCount = 0;
        int missingCount = 0;

        for (MarkEntry entry : markEntries) {
            double maxMarks = entry.getMaxMarks();
            totalMax += maxMarks;

            if (entry.isAbsent()) {
                absentCount++;
            } else if (entry.getMarksObtained() == null) {
                missingCount++;
            } else {
                double obtained = entry.getMarksObtained();
                totalObtained += obtained;

                if (obtained >= entry.getPassMarks()) {
                    passedCount++;
                } else {
                    failedCount++;
                }
            }
        }

        // Percentage calculation based on total max marks
        double percentage = 0.0;
        if (totalMax > 0.0) {
            percentage = round((totalObtained / totalMax) * 100.0, 2);
        }

        // Overall status logic:
        // 1. If any subject is ABSENT or MISSING -> INCOMPLETE (treated distinctly from FAIL)
        // 2. Otherwise, if failedCount > 0 -> FAIL
        // 3. Otherwise -> PASS
        String status;
        String grade;

        if (absentCount > 0 || missingCount > 0) {
            status = GradeScale.STATUS_INCOMPLETE;
            grade = GradeScale.GRADE_INCOMPLETE;
        } else if (failedCount > 0) {
            status = GradeScale.STATUS_FAIL;
            grade = GradeScale.GRADE_FAIL;
        } else {
            status = GradeScale.STATUS_PASS;
            grade = GradeScale.calculateGrade(percentage, status);
        }

        return new Evaluation(
            round(totalObtained, 2),
            round(totalMax, 2),
            percentage,
            grade,
            status,
            markEntries.size(),
            passedCount,
            failedCount,
            absentCount,
            missingCount
        );
    }

    /**
     * Utility method to round numbers cleanly to specified decimal places.
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

