package com.studentresult.model;

/**
 * Model class representing marks entered for a student in a specific subject.
 */
public class MarkEntry {
    private int id;
    private int studentId;
    private int subjectId;
    private int semester;
    private Double marksObtained; // Nullable if absent or not yet evaluated
    private boolean isAbsent;
    private String examDate;
    private String remarks;

    // Additional display fields (populated via joins for convenience in UI)
    private String subjectCode;
    private String subjectName;
    private double maxMarks;
    private double passMarks;
    private int credits;

    public MarkEntry() {
    }

    public MarkEntry(int id, int studentId, int subjectId, int semester, 
                     Double marksObtained, boolean isAbsent, String examDate, String remarks) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.semester = semester;
        this.marksObtained = marksObtained;
        this.isAbsent = isAbsent;
        this.examDate = examDate;
        this.remarks = remarks;
    }

    public MarkEntry(int studentId, int subjectId, int semester, 
                     Double marksObtained, boolean isAbsent, String examDate, String remarks) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.semester = semester;
        this.marksObtained = marksObtained;
        this.isAbsent = isAbsent;
        this.examDate = examDate;
        this.remarks = remarks;
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

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
    }

    public boolean isAbsent() {
        return isAbsent;
    }

    public void setAbsent(boolean absent) {
        isAbsent = absent;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getPassMarks() {
        return passMarks;
    }

    public void setPassMarks(double passMarks) {
        this.passMarks = passMarks;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    /**
     * Determines whether the student passed this particular subject.
     * If absent or marksObtained is null, returns false.
     */
    public boolean isSubjectPassed() {
        if (isAbsent || marksObtained == null) {
            return false;
        }
        return marksObtained >= passMarks;
    }

    /**
     * Returns subject status text: PASS, FAIL, or ABSENT.
     */
    public String getSubjectStatus() {
        if (isAbsent) {
            return "ABSENT";
        }
        if (marksObtained == null) {
            return "PENDING";
        }
        return marksObtained >= passMarks ? "PASS" : "FAIL";
    }
}

