package com.example.smarttrack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    Button btnResetPassword;
    ImageButton btnBack;
    TextInputEditText txtEmail;
    String strEmail;
FirebaseAuth mAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
        btnResetPassword = findViewById(R.id.btnResetPassword);
        txtEmail = findViewById(R.id.txtEmail);
        progressBar = findViewById(R.id.progressBar2);
        mAuth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strEmail = txtEmail.getText().toString().trim();
                if (!TextUtils.isEmpty(strEmail)){
                    ResetPassword();
                } else{
                    txtEmail.setError("Email field can't be empty");
                }
            }
        });
    }
    private void ResetPassword(){
        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setVisibility(View.INVISIBLE);

        mAuth.sendPasswordResetEmail(strEmail)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ForgotPassword.this, "Reset Password link has been sent to your registered email",Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ForgotPassword.this, "Error :- " + e.getMessage(),Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.GONE);
                        btnResetPassword.setVisibility(View.VISIBLE);
                    }
                });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}