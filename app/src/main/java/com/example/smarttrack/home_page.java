package com.example.smarttrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class home_page extends AppCompatActivity {
    Button btnLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this,student_login_page.class);
        startActivity(intent);
        finish();
    }
}