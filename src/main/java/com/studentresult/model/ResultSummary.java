package com.studentresult.model;

/**
 * Data Transfer Object (DTO) combining Student details and Result evaluation
 * for intuitive display in Results Swing tables, search filters, and Report Cards.
 */
public class ResultSummary {
    private int resultId;
    private int studentId;
    private String rollNumber;
    private String studentName;
    private String department;
    private String batchYear;
    private int semester;
    private double totalObtained;
    private double totalMax;
    private double percentage;
    private String grade;
    private String status;
    private String evaluatedAt;

    public ResultSummary() {
    }

    public ResultSummary(int resultId, int studentId, String rollNumber, String studentName, 
                         String department, String batchYear, int semester, double totalObtained, 
                         double totalMax, double percentage, String grade, String status, String evaluatedAt) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.studentName = studentName;
        this.department = department;
        this.batchYear = batchYear;
        this.semester = semester;
        this.totalObtained = totalObtained;
        this.totalMax = totalMax;
        this.percentage = percentage;
        this.grade = grade;
        this.status = status;
        this.evaluatedAt = evaluatedAt;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(String batchYear) {
        this.batchYear = batchYear;
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
}

