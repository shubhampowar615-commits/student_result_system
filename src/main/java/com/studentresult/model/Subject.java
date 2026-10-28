package com.studentresult.model;

/**
 * Model class representing a Subject in a semester curriculum.
 */
public class Subject {
    private int id;
    private String subjectCode;
    private String subjectName;
    private int semester;
    private double maxMarks;
    private double passMarks;
    private int credits;

    public Subject() {
    }

    public Subject(int id, String subjectCode, String subjectName, int semester, 
                   double maxMarks, double passMarks, int credits) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.semester = semester;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
        this.credits = credits;
    }

    public Subject(String subjectCode, String subjectName, int semester, 
                   double maxMarks, double passMarks, int credits) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.semester = semester;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
        this.credits = credits;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
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

    @Override
    public String toString() {
        return subjectCode + ": " + subjectName + " (Sem " + semester + ")";
    }
}

