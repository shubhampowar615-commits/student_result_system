package com.studentresult.model;

/**
 * Model class representing a Student in the institution.
 */
public class Student {
    private int id;
    private String rollNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private int semester;
    private String department;
    private String batchYear;
    private String createdAt;

    public Student() {
    }

    public Student(int id, String rollNumber, String firstName, String lastName, 
                   String email, String phone, int semester, String department, 
                   String batchYear, String createdAt) {
        this.id = id;
        this.rollNumber = rollNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.semester = semester;
        this.department = department;
        this.batchYear = batchYear;
        this.createdAt = createdAt;
    }

    public Student(String rollNumber, String firstName, String lastName, 
                   String email, String phone, int semester, String department, 
                   String batchYear) {
        this.rollNumber = rollNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.semester = semester;
        this.department = department;
        this.batchYear = batchYear;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        if (lastName == null || lastName.trim().isEmpty()) {
            return firstName != null ? firstName : "";
        }
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return rollNumber + " - " + getFullName();
    }
}

