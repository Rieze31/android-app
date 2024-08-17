package com.example.smarttrack;

public class SectionModel {
    private String studentName;
    private String code;
    private String grade;
    private String average;
    private String studentImage;
    private String LRN;
    private String email;
    private String firstName;
    private String lastName;

    private boolean restricted; // New boolean field

    public SectionModel(String studentName, String studentImage, String code, String LRN, String email, String firstName, String lastName, boolean restricted, String grade, String average) {
        this.studentName = studentName;
        this.studentImage = studentImage;
        this.code = code;
        this.grade = grade;
        this.average = average;
        this.LRN = LRN;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.restricted = restricted; // Initialize the restricted field
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentImage() {
        return studentImage;
    }

    public void setStudentImage(String studentImage) {
        this.studentImage = studentImage;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String classCode) {
        this.code = code;
    }

    public String getLRN() {
        return LRN;
    }

    public void setLRN(String LRN) {
        this.LRN = LRN;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String firstQuarter) {
        this.grade= grade;
    }
    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }
}
