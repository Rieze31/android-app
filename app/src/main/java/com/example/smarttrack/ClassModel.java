package com.example.smarttrack;

import android.os.Parcel;
import android.os.Parcelable;

public class ClassModel implements Parcelable {
    private String code;
    private String section;
    private String strand;
    private String subject;
    private String teacherName;
    private String grade;
    private String teacherImage;

    public ClassModel(String subject, String grade, String teacherName, String teacherImage, String code, String strand, String section) {
        this.strand = strand;
        this.section = section;
        this.code = code;
        this.subject = subject;
        this.teacherName = teacherName;
        this.grade = grade;
        this.teacherImage = teacherImage;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
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

    // Parcelable implementation
    protected ClassModel(Parcel in) {
        code = in.readString();
        subject = in.readString();
        grade = in.readString();
        strand = in.readString();
        section = in.readString();
    }

    public static final Creator<ClassModel> CREATOR = new Creator<ClassModel>() {
        @Override
        public ClassModel createFromParcel(Parcel in) {
            return new ClassModel(in);
        }

        @Override
        public ClassModel[] newArray(int size) {
            return new ClassModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(subject);
        dest.writeString(grade);
        dest.writeString(strand);
        dest.writeString(section);
    }
}