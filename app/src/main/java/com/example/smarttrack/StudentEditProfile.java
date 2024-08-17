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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StudentEditProfile extends AppCompatActivity {

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
        setContentView(R.layout.activity_student_edit_profile);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        btnBackLogin = (ImageButton) findViewById(R.id.btnBackLogin);
        btnBackLogin.setOnClickListener(v -> onBackPressed());
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
        Intent intent = getIntent();
        if (intent != null) {

            String fName = intent.getStringExtra("fName");
            String lName = intent.getStringExtra("lName");
            String imageURL = intent.getStringExtra("imageURL");
            boolean editable = intent.getBooleanExtra("editable", true); // Check if fields are editable


            // Set values to EditText fields


            txtFName.setText(fName);
            txtLName.setText(lName);

            // Load image using Picasso
            Picasso.get().load(imageURL).into(addImage);

            // Set EditText fields as non-editable if necessary
            if (!editable) {


                txtFName.setEnabled(false);
                txtLName.setEnabled(false);
                btnSave.setVisibility(View.GONE); // Hide the save button
            }
        }
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

        // Get the current user's ID
        String userId = mAuth.getCurrentUser().getUid();

        // Update user data in Firestore
        db.collection("Users").document(userId)
                .update("FirstName", newFirstName,
                        "LastName", newLastName
                       )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(StudentEditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StudentEditProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "Error updating profile", e);
                    }
                });

        // Upload profile picture if selected
        if (selectedImageUri != null) {
            uploadProfilePicture(userId);
        }

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
                                        Toast.makeText(StudentEditProfile.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(StudentEditProfile.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}
