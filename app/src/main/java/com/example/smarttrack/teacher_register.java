package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class teacher_register extends AppCompatActivity {
    TextInputEditText txtFName, txtLName, txtEmail, txtPassword;
    Button btnReg;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;

    ProgressBar progressBar;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_teacher_register);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        btnBack = (ImageButton) findViewById(R.id.btnBackReg);

        btnBack.setOnClickListener(v -> Back());

        txtFName = (TextInputEditText) findViewById(R.id.txtFName);
        txtLName = (TextInputEditText) findViewById(R.id.txtLName);
        txtEmail = (TextInputEditText) findViewById(R.id.txtEmail);
        txtPassword = (TextInputEditText) findViewById(R.id.txtPassword);
        btnReg = (Button) findViewById(R.id.btnRegister);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnReg.setOnClickListener(v -> Register());
    }

    public void Back() {
        Intent intent = new Intent(this, teacher_login_page.class);
        startActivity(intent);
    }

    public void Register() {
        progressBar.setVisibility(View.VISIBLE);
        String fName, lName, email, password;
        fName = String.valueOf(txtFName.getText());
        lName = String.valueOf(txtLName.getText());
        email = String.valueOf(txtEmail.getText());
        password = String.valueOf(txtPassword.getText());

        if (TextUtils.isEmpty(fName) || TextUtils.isEmpty(lName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(teacher_register.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(teacher_register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(teacher_register.this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user is already registered
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    assert result != null;
                    boolean isNewUser = result.getSignInMethods().isEmpty();
                    FirebaseUser user = mAuth.getCurrentUser();

                    if (!isNewUser) {
                        // User already registered with the provided email
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(teacher_register.this, "User already registered. Please log in.", Toast.LENGTH_SHORT).show();
                    } else {
                        // User is not registered, proceed with registration
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            progressBar.setVisibility(View.GONE);
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            // Sign in success, update UI with the signed-in user's information
                                            Toast.makeText(teacher_register.this, "Account Created",
                                                    Toast.LENGTH_SHORT).show();

                                            // Store user info in Firestore
                                            Map<String, Object> userInfo = new HashMap<>();
                                            userInfo.put("FirstName", fName);
                                            userInfo.put("LastName", lName);
                                            userInfo.put("Email", email);
                                            userInfo.put("profilePictureUrl","");
                                            userInfo.put("isTeacher", "1");

                                            DocumentReference documentReference = fStore.collection("Users").document(user.getUid());
                                            documentReference.set(userInfo);

                                            Intent intent = new Intent(teacher_register.this, teacher_login_page.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(teacher_register.this, "Registration failed. Please try again.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    // Error occurred while fetching sign-in methods
                    Toast.makeText(teacher_register.this, "Registration failed. Please try again later.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
