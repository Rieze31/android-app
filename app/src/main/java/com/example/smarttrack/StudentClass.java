package com.example.smarttrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentClass extends Fragment {

    private RecyclerView recyclerView;
    private AppCompatButton btnView;
    private Spinner cbSemester;
    private FloatingActionButton btnJoinClass;
    private StudentClassAdapter adapter;
    private FirebaseFirestore db;
    private TextView txtJoinedClass;
    private List<StudentClassModel> dataList;
    private List<StudentClassModel> originalList;
    private SearchView searchView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_class, container, false);
        btnView = view.findViewById(R.id.btnView);
        searchView = view.findViewById(R.id.txtSearch);
        txtJoinedClass = view.findViewById(R.id.txtJoinedClass);
        btnJoinClass = view.findViewById(R.id.btnJoinClass);
        btnJoinClass.setOnClickListener(v -> joinClass());
        recyclerView = view.findViewById(R.id.recyclerView);
        cbSemester = view.findViewById(R.id.cbSemester);


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Initialize search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    // If the search query is empty, load all data
                    retrieveJoinedClasses();
                } else {
                    filter(newText);
                }

                return true;
            }
        });

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataList = new ArrayList<>();
        originalList = new ArrayList<>();
        adapter = new StudentClassAdapter(getContext(), dataList,db);
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateNumberOfJoinedClasses();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updateNumberOfJoinedClasses();
            }
        });
        ArrayAdapter<CharSequence> semesterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Semester, R.layout.custom_spinner_item);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cbSemester.setAdapter(semesterAdapter);

        // Set listener for semester spinner
        cbSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                retrieveJoinedClasses();
                updateNumberOfJoinedClasses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Load class data initially based on the selected semester
        retrieveJoinedClasses();

        updateNumberOfJoinedClasses();
        btnView.setOnClickListener(v -> {
            String selectedSemester = cbSemester.getSelectedItem().toString();
            if (selectedSemester.equals("1st Semester")) {
                viewGrades(selectedSemester);
            } else if (selectedSemester.equals("2nd Semester")) {
                viewGrades(selectedSemester);
            }
        });

        return view;
    }

    // Method to filter class data based on search query
    private void filter(String query) {
        List<StudentClassModel> filteredList = new ArrayList<>();
        List<StudentClassModel> currentList = adapter.getData(); // Get the current displayed data

        for (StudentClassModel item : currentList) {
            // Filter by subject or teacher name
            if (item.getSubject().toLowerCase().contains(query.toLowerCase()) ||
                    item.getTeacherName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }

            adapter.setData(filteredList);
            adapter.notifyDataSetChanged();
        }
    }

    public void joinClass() {
        // Inflate the custom layout
        View customView = getLayoutInflater().inflate(R.layout.join_custom, null);

        // Find the EditText in the custom layout
        EditText editTextCode = customView.findViewById(R.id.editTextCode);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(customView)
                .setTitle("Enter the code given by your teacher")
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String classCode = editTextCode.getText().toString();

                        // Check if the entered code exists in the classes database
                        db.collection("Classes")
                                .whereEqualTo("code", classCode)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().isEmpty()) {
                                            // Code exists, retrieve class details
                                            addStudentToSection(classCode);
                                            updateNumberOfJoinedClasses();
                                        } else {
                                            showToast("Incorrect code. Please try again.");
                                        }
                                    } else {
                                        showToast("Error checking code. Please try again.");
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addStudentToSection(String classCode) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userUID = currentUser.getUid();

            // Fetch the user's LRN from Firestore
            db.collection("Users").document(userUID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userLRN = documentSnapshot.getString("LRN");

                            // If LRN is available, add the user's information along with the class code to the section database
                            if (userLRN != null && !userLRN.isEmpty()) {
                                String userProfilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                                String firstName = documentSnapshot.getString("FirstName");
                                String lastName = documentSnapshot.getString("LastName");
                                String email = documentSnapshot.getString("Email");
                                if (userProfilePictureUrl != null && !userProfilePictureUrl.isEmpty()) {
                                    // Add the user's information along with the class code to the section database
                                    Map<String, Object> studentData = new HashMap<>();
                                    studentData.put("profilePicture", userProfilePictureUrl);
                                    studentData.put("FirstName", firstName);
                                    studentData.put("LastName", lastName);
                                    studentData.put("email", email);
                                    studentData.put("LRN", userLRN);
                                    studentData.put("code", classCode);
                                    studentData.put("isRestricted", true);
                                    // Add the student to the section database using LRN as the document ID
                                    db.collection("Classes").document(classCode)
                                            .collection("Students").document(userLRN)
                                            .set(studentData)
                                            .addOnSuccessListener(aVoid -> {
                                                showToast("Successfully joined the class.");
                                                retrieveJoinedClasses();
                                                updateNumberOfJoinedClasses();
                                            })
                                            .addOnFailureListener(e -> {
                                                showToast("Failed to join the class. Please try again.");
                                            });
                                } else {
                                    showToast("Profile picture is required to join the class.");
                                }
                            } else {
                                showToast("LRN not found for the user. Please update your profile.");
                            }
                        } else {
                            showToast("User document not found. Please try again.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToast("Failed to fetch user data. Please try again.");
                    });
        } else {
            showToast("User not signed in. Please sign in again.");
        }
    }
    private void retrieveJoinedClasses() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userUID = currentUser.getUid();
            String selectedSemester = cbSemester.getSelectedItem().toString();
            db.collection("Users").document(userUID)
                    .get()
                    .addOnSuccessListener(userDocumentSnapshot -> {
                        if (userDocumentSnapshot.exists()) {
                            String userLRN = userDocumentSnapshot.getString("LRN");
                            db.collectionGroup("Students")
                                    .whereEqualTo("LRN", userLRN)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        List<StudentClassModel> newDataList = new ArrayList<>(); // Create a new list
                                        for (QueryDocumentSnapshot classDocumentSnapshot : queryDocumentSnapshots) {
                                            String classCode = classDocumentSnapshot.getString("code");
                                            Log.d("Document ID", classCode);
                                            if (classCode != null) {
                                                // Fetch class details based on the selected semester
                                                db.collection("Classes")
                                                        .whereEqualTo("code", classCode) // Filter by class code
                                                        .whereEqualTo("semester", selectedSemester) // Filter by selected semester
                                                        .get()
                                                        .addOnSuccessListener(classSnapshot -> {
                                                            if (!classSnapshot.isEmpty()) { // Check if class exists for the selected semester
                                                                DocumentSnapshot classDetails = classSnapshot.getDocuments().get(0);
                                                                String subject = classDetails.getString("subject");
                                                                String teacherName = classDetails.getString("teacherName");
                                                                String teacherImage = classDetails.getString("teacherImage");
                                                                newDataList.add(new StudentClassModel(subject, teacherName, teacherImage, classCode,userLRN));
                                                                adapter.setData(newDataList); // Update adapter dataset
                                                                adapter.notifyDataSetChanged();
                                                                updateNumberOfJoinedClasses();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            showToast("Failed to fetch class details. Please try again.");
                                                        });
                                            } else {
                                                showToast("Class code not found for the user.");
                                            }
                                        }
                                        // Clear existing data before adding new data
                                        adapter.setData(newDataList);
                                        adapter.notifyDataSetChanged();
                                        updateNumberOfJoinedClasses();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firebase Error", "Failed to retrieve joined classes", e);
                                        showToast("Failed to retrieve joined classes. Please try again.");
                                    });
                        } else {
                            showToast("User document not found. Please try again.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToast("Failed to fetch user data. Please try again.");
                    });
        } else {
            showToast("User not signed in. Please sign in again.");
        }
    }
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void viewGrades(String semester) {
        final String selectedSemester = semester;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userUID = currentUser.getUid();

            db.collection("Users").document(userUID)
                    .get()
                    .addOnSuccessListener(userDocumentSnapshot -> {
                        if (userDocumentSnapshot.exists()) {
                            String userLRN = userDocumentSnapshot.getString("LRN");
                            AtomicInteger numOfStudentsFetched = new AtomicInteger(0);
                            final boolean[] hasRestrictedClasses = {false};

                            db.collectionGroup("Students")
                                    .whereEqualTo("LRN", userLRN)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        ArrayList<String> subjects = new ArrayList<>();
                                        ArrayList<String> firstGrades = new ArrayList<>();
                                        ArrayList<String> secondGrades = new ArrayList<>();

                                        for (QueryDocumentSnapshot studentDocumentSnapshot : queryDocumentSnapshots) {
                                            String classCode = studentDocumentSnapshot.getString("code");
                                            boolean isRestricted = studentDocumentSnapshot.getBoolean("isRestricted");

                                            if (isRestricted) {
                                                hasRestrictedClasses[0] = true;
                                                showRestrictedGradesDialog();
                                                return; // Exit the loop if there are restricted classes
                                            }

                                            if (classCode != null) {
                                                // Fetch class details based on the selected semester
                                                db.collection("Classes").document(classCode)
                                                        .get()
                                                        .addOnSuccessListener(classSnapshot -> {
                                                            if (classSnapshot.exists()) {
                                                                String semesterValue = classSnapshot.getString("semester");
                                                                if (semesterValue != null && semesterValue.equals(selectedSemester)) {
                                                                    String subject = classSnapshot.getString("subject");
                                                                    if (subject != null) {
                                                                        subjects.add(subject); // Add subject to the list
                                                                        Log.d("Subject", "Subject for class " + classCode + ": " + subject);
                                                                    } else {
                                                                        Log.d("Subject", "Subject not found in class details.");
                                                                    }

                                                                    // Retrieve grades based on the semester
                                                                    String grade1 = "";
                                                                    String grade2 = "";
                                                                    if (selectedSemester.equals("1st Semester")) {
                                                                        grade1 = studentDocumentSnapshot.getString("grades.1st Quarter");
                                                                        grade2 = studentDocumentSnapshot.getString("grades.2nd Quarter");
                                                                    } else if (selectedSemester.equals("2nd Semester")) {
                                                                        grade1 = studentDocumentSnapshot.getString("grades.3rd Quarter");
                                                                        grade2 = studentDocumentSnapshot.getString("grades.4th Quarter");
                                                                    }

                                                                    Log.d("Grades", "Grades for student in class " + classCode + ": " + grade1 + ", " + grade2);
                                                                    // Add the grades to the arrays
                                                                    firstGrades.add(grade1);
                                                                    secondGrades.add(grade2);
                                                                }
                                                            } else {
                                                                Log.d("Class", "Class details not found.");
                                                            }

                                                            if (numOfStudentsFetched.incrementAndGet() == queryDocumentSnapshots.size() && !hasRestrictedClasses[0]) {

                                                                if (selectedSemester.equals("1st Semester")) {
                                                                    // Prepare the data for the StudentCompleteGrades activity
                                                                    Intent intent = new Intent(getContext(), StudentCompleteGrades.class);
                                                                    intent.putStringArrayListExtra("subjects", subjects);
                                                                    intent.putStringArrayListExtra("firstGrades", firstGrades);
                                                                    intent.putStringArrayListExtra("secondGrades", secondGrades);

                                                                    // Check if the RecyclerView is empty
                                                                    if (subjects.isEmpty()) {
                                                                        showNeedClassDialog();
                                                                    } else {
                                                                        // Start the StudentCompleteGrades activity
                                                                        startActivity(intent);
                                                                    }
                                                                } else if (selectedSemester.equals("2nd Semester")) {
                                                                    // Prepare the data for the StudentCompleteGrades activity
                                                                    Intent intent = new Intent(getContext(), StudentCompleteGrades2.class);
                                                                    intent.putStringArrayListExtra("subjects", subjects);
                                                                    intent.putStringArrayListExtra("firstGrades", firstGrades);
                                                                    intent.putStringArrayListExtra("secondGrades", secondGrades);

                                                                    // Check if the RecyclerView is empty
                                                                    if (subjects.isEmpty()) {
                                                                        showNeedClassDialog();
                                                                    } else {
                                                                        // Start the StudentCompleteGrades activity
                                                                        startActivity(intent);
                                                                    }
                                                                }
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Firebase Error", "Failed to fetch class details", e);
                                                            showToast("Failed to fetch class details. Please try again.");
                                                        });
                                            } else {
                                                Log.d("Class", "Class code not found for the user.");
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firebase Error", "Failed to retrieve joined classes", e);
                                        showToast("Failed to retrieve joined classes. Please try again.");
                                    });
                        } else {
                            Log.d("User", "User document not found.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase Error", "Failed to retrieve user document", e);
                        showToast("Failed to retrieve user document. Please try again.");
                    });
        }
    }
        // Method to show a dialog indicating that grades are not complete yet
    private void showRestrictedGradesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Incomplete Grades")
                .setMessage("Your grades are not complete yet. Please wait for your instructor to finalize them.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }

    // Show a dialog to inform the user that they need to join a class first
    private void showNeedClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("No Classes Found")
                .setMessage("You haven't joined any classes yet. Please join a class first.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }

    private void updateNumberOfJoinedClasses() {
        int itemCount = recyclerView.getAdapter().getItemCount();
        txtJoinedClass.setText("Joined classes: " + itemCount);
    }


}