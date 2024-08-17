package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherNotifyFragment extends Fragment {

    private FirebaseAuth mAuth;

    public TeacherNotifyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_teacher_notify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        TextInputEditText txtFrom = view.findViewById(R.id.txtFrom);
        TextInputEditText txtTo = view.findViewById(R.id.txtTo);
        TextInputEditText txtMessage = view.findViewById(R.id.txtMessage);
        Button btnSend = view.findViewById(R.id.btnSend);



        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = txtFrom.getText().toString();
                String recipientEmail = txtTo.getText().toString();
                String message = txtMessage.getText().toString();

                // Check if the recipient email is a valid email address
                if (!isValidEmail(recipientEmail)) {
                    // Display an error message if the email is not valid
                    txtTo.setError("Enter a valid email address");
                    return; // Stop further execution
                }

                // Check if the message is empty
                if (message.trim().isEmpty()) {
                    // Display an error message if the message is empty
                    txtMessage.setError("Message cannot be empty");
                    return; // Stop further execution
                }
                if (subject.equals("")){
                    // Display an error message if the message is empty
                    txtFrom.setError("Subject cannot be empty");
                    return; // Stop further execution
                }
                // Get the current user's email
                String senderEmail = mAuth.getCurrentUser().getEmail();
                // Get the current user's display name
                String senderName = mAuth.getCurrentUser().getDisplayName();

                // Create a notification object
                NotifyModel notification = new NotifyModel(senderName, senderEmail, recipientEmail, message);

                // Save the notification to Firestore
                saveNotification(notification);
                sendEmail(subject,message,recipientEmail);
            }
        });
    }
    // Method to validate email address
    private boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    private void saveNotification(NotifyModel notification) {
        // Access Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user's ID
        String userId = mAuth.getCurrentUser().getUid();

        // Retrieve user data from Firestore
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve email from the document
                        String email = documentSnapshot.getString("Email");

                        // Retrieve first name and last name
                        String firstName = documentSnapshot.getString("FirstName");
                        String lastName = documentSnapshot.getString("LastName");
                        // Combine first name and last name to form sender name
                        String senderName = firstName + " " + lastName;

                        // Set sender email and name in the notification object
                        notification.setSenderEmail(email);
                        notification.setSenderName(senderName);

                        // Add the notification to the "Notifications" collection
                        db.collection("Notifications")
                                .add(notification)
                                .addOnSuccessListener(documentReference -> {
                                    // Notification saved successfully

                                })
                                .addOnFailureListener(e -> {
                                    // Error occurred while saving notification
                                    Toast.makeText(requireContext(), "Failed to send notification", Toast.LENGTH_SHORT).show();

                                });
                    } else {
                        // User document does not exist
                        Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error occurred while retrieving user data
                    Toast.makeText(requireContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show();

                });
    }
public void sendEmail(String subject, String content, String to_email){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to_email});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent,"Choose email client"));
}
}
