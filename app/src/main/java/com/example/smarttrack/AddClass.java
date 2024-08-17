package com.example.smarttrack;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddClass extends AppCompatActivity {

    private TextInputEditText txtSection, txtCode, txtSubject;
    private Spinner cbGrade, cbStrand;
    private ImageButton btnAdd;
    private String userFullName;
    private String userProfilePictureUrl;
    private FirebaseFirestore db;
    private ImageButton btnBackLogin;
    private FirebaseAuth auth; // Assuming you're using Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        btnBackLogin = (ImageButton) findViewById(R.id.btnBackLogin);
        btnBackLogin.setOnClickListener(v -> onBackPressed());
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        retrieveUserData();
        // Initialize views
        txtSection = findViewById(R.id.txtSection);
        txtCode = findViewById(R.id.txtCode);
        txtSubject = findViewById(R.id.txtSubject);
        cbGrade = findViewById(R.id.cbGrade);
        cbStrand = findViewById(R.id.cbStrand);
        btnAdd = findViewById(R.id.btnAdd);
        TextInputLayout textInputLayout = findViewById(R.id.textInputLayout3);
        TextInputEditText txtCode = findViewById(R.id.txtCode);

// Set custom end icon click listener
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the text from the TextInputEditText
                String textToCopy = txtCode.getText().toString();

                // Copy text to clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", textToCopy);
                clipboard.setPrimaryClip(clip);

                // Show toast indicating text has been copied
                Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        // Initialize Spinner adapters
        ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(this,
                R.array.Grades, android.R.layout.simple_spinner_item);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cbGrade.setAdapter(gradeAdapter);

        ArrayAdapter<CharSequence> strandAdapter = ArrayAdapter.createFromResource(this,
                R.array.Strand, android.R.layout.simple_spinner_item);
        strandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cbStrand.setAdapter(strandAdapter);

        ClassModel classDetails = getIntent().getParcelableExtra("classDetails");
        if (classDetails != null) {
            // Populate fields with existing data for editing
            txtSection.setText(classDetails.getSubject());
            txtCode.setText(classDetails.getCode());
            txtSubject.setText(classDetails.getSubject());
            setSpinnerSelection(cbGrade, classDetails.getGrade());
            setSpinnerSelection(cbStrand, classDetails.getStrand());
        }
        // Generate random code and set it to the txtCode field
        String randomCode = generateRandomCode();
        txtCode.setText(randomCode);
        txtCode.setEnabled(false); // Make it non-editable
        btnAdd.setOnClickListener(v -> addClassToFirestore());

    }

    // Method to generate random alphanumeric code
    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomCode = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            randomCode.append(characters.charAt(rnd.nextInt(characters.length())));
        }
        return randomCode.toString();
    }
    private void setSpinnerSelection(Spinner spinner, String selectedItem) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(selectedItem);
            if (position != -1) {
                spinner.setSelection(position);
            }
        }
    }

    private void retrieveUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users")
                    .whereEqualTo("Email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve user data
                                String firstName = document.getString("FirstName");
                                String lastName = document.getString("LastName");
                                userFullName = firstName + " " + lastName;
                                userProfilePictureUrl = document.getString("profilePictureUrl");
                            }
                        } else {
                            // Handle errors
                            Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void addClassToFirestore() {
        // Get other class data from user input
        String section = txtSection.getText().toString().trim();
        String code = txtCode.getText().toString().trim();
        String subject = txtSubject.getText().toString().trim();
        String grade = cbGrade.getSelectedItem().toString();
        String strand = cbStrand.getSelectedItem().toString();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String teacherEmail = currentUser.getEmail();
        // Check if any field is empty
        if (section.isEmpty() || code.isEmpty() || subject.isEmpty()) {
            Toast.makeText(AddClass.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map to store class data
        Map<String, Object> classData = new HashMap<>();
        classData.put("section", section);
        classData.put("code", code);
        classData.put("semester", "1st Semester");
        classData.put("subject", subject);
        classData.put("grade", grade);
        classData.put("strand", strand);
        classData.put("teacherName", userFullName); // Add user's full name
        classData.put("teacherImage", userProfilePictureUrl); // Add user's profile picture URL
        classData.put("teacherEmail",teacherEmail); // Add user's email for replacement of ID to retrieve their data

        // Add the class data to Firestore
        db.collection("Classes").document(code).set(classData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddClass.this, "Class added successfully", Toast.LENGTH_SHORT).show();
                        class_page.getInstance().loadClassData();
                        finish(); // Finish the activity after adding the class
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddClass.this, "Failed to add class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}
