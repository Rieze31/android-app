package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TeacherProfile extends AppCompatActivity {
Button btnConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> Confirm());
    }
    public void Confirm(){
        Intent intent = new Intent(this, teacher_homepage.class);
        startActivity(intent);
        finish();
    }
}