package com.example.smarttrack;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TeacherEditProfile extends AppCompatActivity {

    public static final int PICK_IMAGE_REQUEST = 1;

    private TextView txtFName, txtLName;
    private TextInputEditText txtFNameInput, txtLNameInput;
    private ImageView addImage;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private ImageButton btnBackLogin;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_teacher_edit_profile);
        btnBackLogin = (ImageButton) findViewById(R.id.btnBackLogin);
        btnBackLogin.setOnClickListener(v -> onBackPressed());
        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize views
        txtFName = findViewById(R.id.txtFName);
        txtLName = findViewById(R.id.txtLName);
        txtFNameInput = findViewById(R.id.txtFName);
        txtLNameInput = findViewById(R.id.txtLName);
        addImage = findViewById(R.id.addImages);

        // Set click listener for addImage ImageView
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Retrieve user's data from Firestore
        retrieveUserData();
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void retrieveUserData() {
        // Get the current user's ID
        String userId = mAuth.getCurrentUser().getUid();

        // Retrieve user data from Firestore
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve first name and last name from the document
                        String firstName = documentSnapshot.getString("FirstName");
                        String lastName = documentSnapshot.getString("LastName");

                        // Set the retrieved values to the respective TextViews
                        txtFName.setText(firstName);
                        txtLName.setText(lastName);

                        // Load profile picture
                        loadProfilePicture(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e("TAG", "Error getting user data", e);
                });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData(); // Update selectedImageUri with the new image URI

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                // Resize the bitmap to 130sdp
                Bitmap resizedBitmap = resizeBitmap(bitmap, 130);
                // Set the resized bitmap to the ImageView
                addImage.setImageBitmap(resizedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveProfile() {
        // Method implementation remains the same
        // Get updated first name and last name

        String newFirstName = txtFNameInput.getText().toString().trim();
        String newLastName = txtLNameInput.getText().toString().trim();
        // Retrieve user data from Firestore
        String userId = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();

        // Update user data in Firestore

        db.collection("Users").document(userId)
                .update("FirstName", newFirstName,
                        "LastName", newLastName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(TeacherEditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        TeacherProfileFragment.getInstance().loadData();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TeacherEditProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "Error updating profile", e);
                    }
                });

        // Upload profile picture if selected
        if (selectedImageUri != null) {
            uploadProfilePicture(userId);
        }
        // Update teacher's data in all classes where the teacher's email matches the current user's email
        updateClassesTeacherData(newFirstName, newLastName, userEmail);
    }

    private void uploadProfilePicture(String userId) {
        StorageReference profilePicRef = storageRef.child("profile_pictures/" + userId + ".jpg");

        // Convert selected image to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageData = new byte[0];
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            // Update imageData with compressed byte array
            imageData = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Upload the image data to Firebase Storage
        UploadTask uploadTask = profilePicRef.putBytes(imageData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "Error uploading profile picture", e);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the download URL of the uploaded image
                profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Update user document with the profile picture URL
                        db.collection("Users").document(userId)
                                .update("profilePictureUrl", uri.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(TeacherEditProfile.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                                        updateClassesTeacherImage(uri.toString());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TeacherEditProfile.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                                        Log.e("TAG", "Error updating profile picture URL", e);
                                    }
                                });

                    }
                });
            }
        });
    }
    private void loadProfilePicture(String userId) {
        // Get reference to the profile picture in Firebase Storage
        StorageReference profilePicRef = FirebaseStorage.getInstance().getReference()
                .child("profile_pictures/" + userId + ".jpg");

        // Load the profile picture into the ImageView
        profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Use Picasso or Glide to load the image into the ImageView
            Picasso.get().load(uri).into(addImage);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.e("TAG", "Error loading profile picture", exception);
        });
    }
    private Bitmap resizeBitmap(Bitmap bitmap, int targetSizeInSdp) {
        int targetSizeInPx = (int) getResources().getDimensionPixelSize(R.dimen.one_picture_size);
        float scale = (float) targetSizeInPx / bitmap.getWidth();
        int targetHeightInPx = (int) (bitmap.getHeight() * scale);
        return Bitmap.createScaledBitmap(bitmap, targetSizeInPx, targetHeightInPx, true);
    }
    private void updateClassesTeacherData(String newFirstName, String newLastName, String userEmail) {
        // Query Firestore to get all classes where the teacher's email matches the current user's email
        db.collection("Classes")
                .whereEqualTo("teacherEmail", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String classDocumentId = documentSnapshot.getId();
                        // Update teacher's data in the class document
                        db.collection("Classes")
                                .document(classDocumentId)
                                .update("FirstName", newFirstName,
                                        "LastName", newLastName)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Teacher's data updated successfully for the class
                                        Log.d("TAG", "Teacher's data updated successfully for class: " + classDocumentId);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure
                                        Log.e("TAG", "Failed to update teacher's data for class: " + classDocumentId, e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("TAG", "Failed to fetch classes for the teacher", e);
                });
    }
    private void updateClassesTeacherImage(String newImageUrl) {
        // Query Firestore to get all classes where the teacher's email matches the current user's email
        db.collection("Classes")
                .whereEqualTo("teacherEmail", mAuth.getCurrentUser().getEmail())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String classDocumentId = documentSnapshot.getId();
                        // Update teacher's image URL in the class document
                        db.collection("Classes")
                                .document(classDocumentId)
                                .update("teacherImageUrl", newImageUrl)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Teacher's image URL updated successfully for the class
                                        Log.d("TAG", "Teacher's image URL updated successfully for class: " + classDocumentId);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure
                                        Log.e("TAG", "Failed to update teacher's image URL for class: " + classDocumentId, e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("TAG", "Failed to fetch classes for the teacher", e);
                });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
