package com.example.smarttrack;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditClass extends AppCompatActivity {
    private EditText txtCode, txtSubject, txtSection; // Add EditText fields
    private Spinner cbGrade, cbStrand; // Add Spinners
    private ImageButton btnBackLogin;
    ImageButton btnUpdate;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);
        db = FirebaseFirestore.getInstance();
        btnBackLogin = (ImageButton) findViewById(R.id.btnBackLogin);
        btnBackLogin.setOnClickListener(v -> onBackPressed());
        // Initialize EditText fields
        txtCode = findViewById(R.id.txtCode);
        txtSubject = findViewById(R.id.txtSubject);
        txtSection = findViewById(R.id.txtSection);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Initialize Spinners
        cbGrade = findViewById(R.id.cbGrade);
        cbStrand = findViewById(R.id.cbStrand);
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
        // Set adapters for Spinners with appropriate data
        ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(this, R.array.Grades, android.R.layout.simple_spinner_item);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cbGrade.setAdapter(gradeAdapter);

        ArrayAdapter<CharSequence> strandAdapter = ArrayAdapter.createFromResource(this, R.array.Strand, android.R.layout.simple_spinner_item);
        strandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cbStrand.setAdapter(strandAdapter);

        // Retrieve data from intent extras
        Intent intent = getIntent();
        String classCode = intent.getStringExtra("code");
        if (classCode != null) {
            // Use the classCode to fetch data from Firestore
            DocumentReference docRef = db.collection("Classes").document(classCode);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // DocumentSnapshot exists, retrieve data
                        String subject = documentSnapshot.getString("subject");
                        String grade = documentSnapshot.getString("grade");
                        String strand = documentSnapshot.getString("strand");
                        String section = documentSnapshot.getString("section");

                        // Set retrieved data to the EditText fields
                        txtCode.setText(classCode);
                        txtCode.setEnabled(false);
                        txtSubject.setText(subject);
                        txtSection.setText(section);

                        // Set selected items for Spinners
                        if (grade != null) {
                            int gradePosition = gradeAdapter.getPosition(grade);
                            cbGrade.setSelection(gradePosition);
                        }

                        if (strand != null) {
                            int strandPosition = strandAdapter.getPosition(strand);
                            cbStrand.setSelection(strandPosition);
                        }

                        // Set button click listener
                        btnUpdate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Retrieve updated information from EditText fields and Spinners
                                String subject = txtSubject.getText().toString().trim();
                                String grade = cbGrade.getSelectedItem().toString().trim();
                                String strand = cbStrand.getSelectedItem().toString().trim();
                                String section = txtSection.getText().toString().trim();

                                // Validate data if necessary

                                // Update the corresponding document in Firestore
                                docRef.update("subject", subject,
                                                "grade", grade,
                                                "strand", strand,
                                                "section", section)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Document updated successfully
                                                Toast.makeText(EditClass.this, "Class information updated", Toast.LENGTH_SHORT).show();
                                                class_page.getInstance().loadClassData();
                                                finish();
                                                // Optionally, navigate back to the previous activity or perform any other action
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Failed to update document
                                                Toast.makeText(EditClass.this, "Failed to update class information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    } else {
                        // DocumentSnapshot doesn't exist
                        Toast.makeText(EditClass.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Error fetching document
                    Toast.makeText(EditClass.this, "Error fetching document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle the case where classCode is null (e.g., display an error message)
            Toast.makeText(this, "Class code is null", Toast.LENGTH_SHORT).show();
            // Optionally, you can finish the activity or take any other appropriate action
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}