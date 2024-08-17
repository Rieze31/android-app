package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class choose_role extends AppCompatActivity {

    private ImageButton btnStudent;
    private ImageButton btnTeacher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_choose_role);



        btnStudent = (ImageButton) findViewById(R.id.btnStudent);
        btnStudent.setOnClickListener(v -> openStudentLogin());

        btnTeacher = (ImageButton) findViewById(R.id.btnTeacher);
        btnTeacher.setOnClickListener(v -> openTeacherLogin());
    }

    public void Back(){
        Intent intent = new Intent(this, login_page.class);
        startActivity(intent);
    }

    public void openStudentLogin(){
        Intent intent = new Intent(this, student_login_page.class);
        startActivity(intent);
    }

    public void openTeacherLogin(){
        Intent intent = new Intent(this, teacher_login_page.class);
        startActivity(intent);
    }
}