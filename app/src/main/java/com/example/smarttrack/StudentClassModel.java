package com.example.smarttrack;

public class StudentClassModel {
    private String LRN;
    private String subject;
    private String teacherName;
    private String classCode;
    private String teacherImage;

    public StudentClassModel(String subject, String teacherName, String teacherImage, String classCode,String LRN) {
        this.LRN = LRN;
        this.classCode= classCode;
        this.subject = subject;
        this.teacherName = teacherName;
        this.teacherImage = teacherImage;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }





    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    public String getTeacherImage() {
        return teacherImage;
    }

    public void setTeacherImage(String teacherImage) {
        this.teacherImage = teacherImage;
    }

    public String getCode() {
        return classCode;
    }

    public void setCode(String classCode) {
        this.classCode = classCode;
    }

    public String getLRN() {
        return LRN;
    }

    public void setLRN(String LRN) {
        this.LRN = LRN;
    }
}
