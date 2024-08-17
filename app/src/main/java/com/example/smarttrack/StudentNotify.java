package com.example.smarttrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentNotify extends Fragment {

    private RecyclerView recyclerView;
    private NotifyAdapter adapter;
    private List<NotifyModel> dataList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_notify, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataList = new ArrayList<>();
        adapter = new NotifyAdapter(getContext(), dataList);
        recyclerView.setAdapter(adapter);

        // Retrieve notifications where the current user's email matches the recipient email
        retrieveNotifications();

        return view;
    }

    private void retrieveNotifications() {
        // Get the current user's email
        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        // Retrieve notifications from Firestore where the recipient email matches the current user's email
        db.collection("Notifications")
                .whereEqualTo("recipientEmail", currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dataList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get notification details
                            String senderName = document.getString("senderName");
                            String senderEmail = document.getString("senderEmail");
                            String message = document.getString("message");

                            // Create a NotifyModel object
                            NotifyModel notification = new NotifyModel(senderName, senderEmail, currentUserEmail, message);

                            // Add the notification to the dataList
                            dataList.add(notification);
                        }
                        // Notify adapter that data has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                    }
                });
    }
}