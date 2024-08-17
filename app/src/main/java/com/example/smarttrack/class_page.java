package com.example.smarttrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class class_page extends Fragment {
    private TextView txtNumOfClass;
    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private Spinner cbSemester;
    FloatingActionButton btnAddClass;
    private List<ClassModel> dataList;
    private FirebaseFirestore db;
    private SearchView searchView;

    private static class_page instance = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_page, container, false);



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataList = new ArrayList<>();
        cbSemester = view.findViewById(R.id.cbSemester);
        adapter = new ClassAdapter(getContext(), dataList);
        recyclerView.setAdapter(adapter);

        txtNumOfClass = view.findViewById(R.id.txtNumOfClass);
        // Find the search view
        searchView = view.findViewById(R.id.txtSearch);
        btnAddClass = view.findViewById(R.id.btnAdd);
        // Set up search functionality
        // Initialize Spinner adapters
        ArrayAdapter<CharSequence> semesterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Semester, R.layout.custom_spinner_item);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cbSemester.setAdapter(semesterAdapter);
        setupSearchFunctionality();

        // Set listener for semester spinner
        cbSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Load class data for the selected semester
                loadClassData();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Load class data initially based on the selected semester
        loadClassData();


        btnAddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userUID = currentUser.getUid();
                    // Fetch the user's LRN from Firestore
                    db.collection("Users").document(userUID)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                        String userProfilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                                        if (userProfilePictureUrl != null && !userProfilePictureUrl.isEmpty()) {
                                            // User has a profile picture, proceed to navigate to the respective activity
                                            String selectedSemester = cbSemester.getSelectedItem().toString();
                                            if (selectedSemester.equals("1st Semester")) {
                                                // Navigate to AddClass activity
                                                Intent intent = new Intent(getActivity(), AddClass.class);
                                                startActivity(intent);
                                            } else if (selectedSemester.equals("2nd Semester")) {
                                                // Navigate to AddSecondSem activity
                                                Intent intent = new Intent(getActivity(), AddSecondSem.class);
                                                startActivity(intent);
                                            }
                                        } else {
                                            // User does not have a profile picture, show a dialog
                                            showProfilePictureRequiredDialog();
                                        }
                                } else {
                                    // Document does not exist, handle this case
                                    showToast("User document does not exist");
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle failures in fetching document snapshot
                                showToast("Failed to fetch user data: " + e.getMessage());
                            });
                } else {
                    // User is not signed in, handle this case
                    showToast("User not signed in. Please sign in again.");
                }
            }
        });
    }
    public void loadClassData() {

        // Get the current user's email from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String teacherEmail = currentUser.getEmail();
            String selectedSemester = cbSemester.getSelectedItem().toString();
            // or if you identify teachers by name:
            // String teacherName = currentUser.getDisplayName();

            db.collection("Classes")
                    .whereEqualTo("semester", selectedSemester)
                    .whereEqualTo("teacherEmail", teacherEmail) // or "teacherName" depending on how you identify teachers
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            dataList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String section = document.getString("section");
                                String code = document.getString("code");
                                String subject = document.getString("subject");
                                String grade = document.getString("grade");
                                String strand = document.getString("strand");
                                String teacherName = document.getString("teacherName");
                                String teacherImage = document.getString("teacherImage");

                                dataList.add(new ClassModel(subject, grade + " - " + strand + " - " + section, teacherName, teacherImage, code, strand, section));
                            }
                            adapter.notifyDataSetChanged();
                            updateNumOfClassTextView();
                        } else {
                            // Handle errors
                        }
                    });
        } else {
            // User is not signed in, handle this case accordingly
        }

    }
    private void setupSearchFunctionality() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    // If the search query is empty, load all data
                    loadClassData();
                } else {
                    // Filter the data based on the search query
                    List<ClassModel> filteredList = filter(dataList, newText);
                    adapter.setData(filteredList);
                }
                return true;
            }
        });
    }

    private List<ClassModel> filter(List<ClassModel> models, String query) {
        query = query.toLowerCase();
        List<ClassModel> filteredList = new ArrayList<>();
        for (ClassModel model : models) {
            if (model.getSubject().toLowerCase().contains(query)
                    || model.getGrade().toLowerCase().contains(query)
                    || model.getTeacherName().toLowerCase().contains(query)
                    || model.getStrand().toLowerCase().contains(query)
                    || model.getSection().toLowerCase().contains(query)
                   ) {
                filteredList.add(model);
            }
        }
        return filteredList;
    }
    public void updateNumOfClassTextView() {
        int itemCount = dataList.size();
        txtNumOfClass.setText("Number of classes: " + itemCount);
    }
    // Method to show dialog indicating profile picture is required
    private void showProfilePictureRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Profile Picture Required");
        builder.setMessage("You need to set a profile picture to create a class.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method to display toast messages
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    public static class_page getInstance() {
        return instance;
    }
}


