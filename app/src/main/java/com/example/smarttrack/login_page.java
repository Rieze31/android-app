package com.example.smarttrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class login_page extends AppCompatActivity {
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(v -> openActivity());
    }

    public void openActivity(){
        Intent intent = new Intent(this, choose_role.class);
        startActivity(intent);
    }
}