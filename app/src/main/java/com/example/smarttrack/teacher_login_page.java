package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class teacher_login_page extends AppCompatActivity {
    ImageButton btnBack;
    TextView createAccount, forgotPassword;
    TextInputEditText txtEmail, txtPassword;
    Button btnLogin;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_teacher_login_page);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        btnBack = (ImageButton) findViewById(R.id.btnBackReg);
        btnBack.setOnClickListener(v -> Back());
        forgotPassword = (TextView) findViewById(R.id.btnForgotPassword);
        forgotPassword.setOnClickListener(v -> openForgotPasswordPage());
        createAccount = (TextView) findViewById(R.id.btnCreateAccount);
        createAccount.setOnClickListener(v -> openRegisterPage());

        txtEmail = (TextInputEditText) findViewById(R.id.txtEmail);
        txtPassword = (TextInputEditText) findViewById(R.id.txtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        btnLogin.setOnClickListener(v -> Login());
    }

    public void Back() {
        Intent intent = new Intent(this, choose_role.class);
        startActivity(intent);
    }

    public void openRegisterPage() {
        Intent intent = new Intent(this, teacher_register.class);
        startActivity(intent);
    }
    public void openForgotPasswordPage() {
        Intent intent = new Intent(this, ForgotPassword.class);
        startActivity(intent);
    }
    public void Login() {
        progressBar.setVisibility(View.VISIBLE);
        String email, password;
        email = String.valueOf(txtEmail.getText());
        password = String.valueOf(txtPassword.getText());

        if (TextUtils.isEmpty(email)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(teacher_login_page.this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(teacher_login_page.this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Sign-in successful, check user access level
                        checkUserAccessLevel(authResult.getUser().getUid());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Sign-in failed, display error message
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(teacher_login_page.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressBar.setVisibility(View.GONE);
                        // Sign in success, update UI with the signed-in user's information
                        checkUserAccessLevel(authResult.getUser().getUid());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user.
                        Toast.makeText(teacher_login_page.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkUserAccessLevel(String uid) {
        DocumentReference df = fStore.collection("Users").document(uid);
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("isTeacher") != null) {
                    // User is a teacher
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(teacher_login_page.this, "Login Success.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(teacher_login_page.this, teacher_homepage.class);
                    startActivity(intent);
                    finish();
                } else {
                    // User is not a teacher
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(teacher_login_page.this, "Account not found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.e("teacher_login_page", "Error checking user access level", e);
                Toast.makeText(teacher_login_page.this, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
