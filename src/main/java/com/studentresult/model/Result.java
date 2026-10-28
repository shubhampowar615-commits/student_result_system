package com.studentresult.model;

/**
 * Model class representing the semester evaluation result for a student.
 */
public class Result {
    private int id;
    private int studentId;
    private int semester;
    private double totalObtained;
    private double totalMax;
    private double percentage;
    private String grade;
    private String status; // PASS, FAIL, INCOMPLETE
    private String evaluatedAt;

    public Result() {
    }

    public Result(int id, int studentId, int semester, double totalObtained, 
                  double totalMax, double percentage, String grade, String status, String evaluatedAt) {
        this.id = id;
        this.studentId = studentId;
        this.semester = semester;
        this.totalObtained = totalObtained;
        this.totalMax = totalMax;
        this.percentage = percentage;
        this.grade = grade;
        this.status = status;
        this.evaluatedAt = evaluatedAt;
    }

    public Result(int studentId, int semester, double totalObtained, 
                  double totalMax, double percentage, String grade, String status) {
        this.studentId = studentId;
        this.semester = semester;
        this.totalObtained = totalObtained;
        this.totalMax = totalMax;
        this.percentage = percentage;
        this.grade = grade;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getTotalObtained() {
        return totalObtained;
    }

    public void setTotalObtained(double totalObtained) {
        this.totalObtained = totalObtained;
    }

    public double getTotalMax() {
        return totalMax;
    }

    public void setTotalMax(double totalMax) {
        this.totalMax = totalMax;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(String evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
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
}

