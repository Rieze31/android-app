package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ViewProfile extends AppCompatActivity {
    private TextView txtFName, txtLName, txtLRN, txtEmail;
    private ImageButton btnBack;
    private ImageView addImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
// Initialize views
        txtLRN = findViewById(R.id.txtLRN);
        txtEmail = findViewById(R.id.txtEmail);
        txtFName = findViewById(R.id.txtFName);
        txtLName = findViewById(R.id.txtLName);
        btnBack = findViewById(R.id.btnBackLogin);
        addImage = findViewById(R.id.addImage);
        btnBack.setOnClickListener(v -> onBackPressed());
        Intent intent = getIntent();
        if (intent != null) {
            String LRN = intent.getStringExtra("LRN");
            String email = intent.getStringExtra("email");
            String fName = intent.getStringExtra("FirstName");
            String lName = intent.getStringExtra("LastName");
            String imageURL = intent.getStringExtra("imageURL");
            boolean editable = intent.getBooleanExtra("editable", true); // Check if fields are editable


            // Set values to EditText fields
            txtLRN.setText(LRN);
            txtEmail.setText(email);
            txtFName.setText(fName);
            txtLName.setText(lName);

            // Load image using Picasso
            Picasso.get().load(imageURL).into(addImage);

            // Set EditText fields as non-editable if necessary
            if (!editable) {
                txtLRN.setEnabled(false);
                txtEmail.setEnabled(false);
                txtFName.setEnabled(false);
                txtLName.setEnabled(false);

            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}