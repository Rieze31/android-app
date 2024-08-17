package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherSectionPage2 extends AppCompatActivity {
    private TextView txtCode, txtGradeSection;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private SectionAdapter2 adapter;
    private TextView txtNumOfStudents;
    private Spinner cbQuarters;
    private List<SectionModel2> dataList;
    private FirebaseFirestore db;
    private ImageButton btnBackLogin;
    private List<SectionModel2> originalList;
    // Add this global variable
    private AppCompatButton btnRestrict;
    // Add this variable to TeacherSectionPage2.java
    private ListenerRegistration isRestrictedListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_teacher_section_page2);

        btnBackLogin = (ImageButton) findViewById(R.id.btnBackLogin);
        btnBackLogin.setOnClickListener(v -> onBackPressed());
        // Initialize Firestore

        txtNumOfStudents = findViewById(R.id.txtNumOfStudents);
        db = FirebaseFirestore.getInstance();
        txtCode = findViewById(R.id.txtCode);
        txtGradeSection = findViewById(R.id.txtSection);
        cbQuarters = findViewById(R.id.cbQuarters);
        searchView = findViewById(R.id.txtSearch);
        recyclerView = findViewById(R.id.sectionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dataList = new ArrayList<>();

        originalList = new ArrayList<>();
        adapter = new SectionAdapter2(TeacherSectionPage2.this, dataList, db, "", TeacherSectionPage2.this);
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateNumberOfStudents();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updateNumberOfStudents();
            }
        });
        int totalItems = adapter.getItemCount();
        TextView txtTotalItems = findViewById(R.id.txtNumOfStudents);
        txtTotalItems.setText("Total Students: " + totalItems);
        Intent intent = getIntent();
        String grade = intent.getStringExtra("grade");
        String classCode = getIntent().getStringExtra("code");
        if (classCode != null) {
            txtCode.setText("Code: " + classCode);
            txtGradeSection.setText("Grade & Section: " + grade);
            fetchStudentsByClassCode(classCode);
            updateNumberOfStudents();
        }
        // Initialize the btnRestrict button in the onCreate() method
        btnRestrict = findViewById(R.id.btnRestrict);
        btnRestrict.setOnClickListener(v -> updateIsRestricted(classCode));

        cbQuarters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedQuarter = cbQuarters.getSelectedItem().toString();

                if (selectedQuarter.equals("3rd Quarter")) {
                    // Now you have the selected quarter, you can create the adapter and populate the RecyclerView
                    adapter = new SectionAdapter2(TeacherSectionPage2.this, dataList, db, selectedQuarter, TeacherSectionPage2.this);
                    recyclerView.setAdapter(adapter);
                    // Fetch students based on the selected quarter
                    fetchStudentsByClassCode(classCode);
                    updateNumberOfStudents();
                } else if (selectedQuarter.equals("4th Quarter")) {
                    // Create a new list for the 4th quarter students
                    List<SectionModel2> dataList2 = new ArrayList<>();

                    // Now you have the selected quarter, you can create the adapter and populate the RecyclerView
                    adapter = new SectionAdapter2(TeacherSectionPage2.this, dataList2, db, selectedQuarter,TeacherSectionPage2.this);
                    recyclerView.setAdapter(adapter);
                    // Fetch students based on the selected quarter
                    fetchStudentsByClassCode2(classCode, dataList2);
                    updateNumberOfStudents();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });
        // Initialize Spinner adapters
        ArrayAdapter<CharSequence> quarterAdapter = ArrayAdapter.createFromResource(this,
                R.array.Quarters2, R.layout.custom_spinner_item);
        quarterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cbQuarters.setAdapter(quarterAdapter);
        // Set up the search view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });
    }

    // Call startListeningForIsRestrictedChanges() in onResume()
    @Override
    protected void onResume() {
        super.onResume();
        String classCode = getIntent().getStringExtra("code");
        if (classCode != null) {
            startListeningForIsRestrictedChanges(classCode);
        }
    }

    // Call stopListeningForIsRestrictedChanges() in onPause()
    @Override
    protected void onPause() {
        super.onPause();
        if (isRestrictedListener != null) {
            isRestrictedListener.remove();
        }
    }
    private void filterData(String query) {
        dataList.clear();
        if (query.isEmpty()) {
            dataList.addAll(originalList);
        } else {
            for (SectionModel2 model : originalList) {
                if (model.getStudentName().toLowerCase().contains(query.toLowerCase())) {
                    dataList.add(model);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void fetchStudentsByClassCode(String classCode) {
        db.collection("Classes").document(classCode)
                .collection("Students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    originalList.clear(); // Clear originalList
                    dataList.clear(); // Clear dataList

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String LRN = documentSnapshot.getString("LRN");
                        Boolean isRestricted = documentSnapshot.getBoolean("isRestricted");
                        boolean restricted = isRestricted != null && isRestricted; // Default to false if null
                        String email = documentSnapshot.getString("email");
                        String thirdQuarterGrade = documentSnapshot.getString("grades.3rd Quarter");

                        Double thirdGrade = null;


                        // Parse grades to Double if not null
                        if (thirdQuarterGrade != null) {
                            thirdGrade = Double.valueOf(thirdQuarterGrade);
                        }


                        String studentFirstName = documentSnapshot.getString("FirstName");
                        String studentLastName = documentSnapshot.getString("LastName");
                        String profilePictureUrl = documentSnapshot.getString("profilePicture");

                        String thirdGradeString = thirdGrade != null ? String.valueOf(thirdGrade) : "N/A";
                        originalList.add(new SectionModel2(studentFirstName + " " + studentLastName, profilePictureUrl, classCode, LRN, email, studentFirstName, studentLastName, restricted,"Grade: "+thirdGradeString,""));
                    }
                    dataList.addAll(originalList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to retrieve students: " + e.getMessage());
                    Toast.makeText(this, "Failed to retrieve students", Toast.LENGTH_SHORT).show();
                });
    }
    public void fetchStudentsByClassCode2(String classCode, List<SectionModel2> dataList2) {
        db.collection("Classes").document(classCode)
                .collection("Students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    dataList2.clear(); // Clear the list

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String LRN = documentSnapshot.getString("LRN");
                        Boolean isRestricted = documentSnapshot.getBoolean("isRestricted");
                        boolean restricted = isRestricted != null && isRestricted; // Default to false if null
                        String email = documentSnapshot.getString("email");
                        String thirdQuarterGrade = documentSnapshot.getString("grades.3rd Quarter");
                        String fourthQuarterGrade = documentSnapshot.getString("grades.4th Quarter");

                        Double thirdGrade = null;
                        Double fourthGrade = null;
                        Double average = null;

                        // Parse grades to Double if not null
                        if (thirdQuarterGrade != null) {
                            thirdGrade = Double.valueOf(thirdQuarterGrade);
                        }
                        if (fourthQuarterGrade != null) {
                            fourthGrade = Double.valueOf(fourthQuarterGrade);
                        }

                        // Calculate average if both grades are available
                        if (thirdGrade != null && fourthGrade != null) {
                            average = (thirdGrade + fourthGrade) / 2;
                        }

                        String studentFirstName = documentSnapshot.getString("FirstName");
                        String studentLastName = documentSnapshot.getString("LastName");
                        String profilePictureUrl = documentSnapshot.getString("profilePicture");

                        // Construct SectionModel2 with grades and average
                        String fourthGradeString = fourthGrade != null ? String.valueOf(fourthGrade) : "N/A";
                        String averageString = average != null ? String.valueOf(average) : "N/A";
                        dataList2.add(new SectionModel2(studentFirstName + " " + studentLastName, profilePictureUrl, classCode, LRN, email, studentFirstName, studentLastName, restricted, "Grade: " +fourthGradeString, "Average: " + averageString));
                    }

                    adapter.notifyDataSetChanged(); // Notify adapter that the data has changed
                });
    }

    // Create a method to fetch student data and update txtNumOfStudents
    public void updateNumberOfStudents() {
        int itemCount = dataList.size();
        txtNumOfStudents.setText("Total students: " + itemCount);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
    // Add this method to TeacherSectionPage2.java
    private void updateIsRestricted(String classCode) {
        db.collection("Classes")
                .document(classCode)
                .collection("Students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isCurrentlyRestricted = true; // Set initial value
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        isCurrentlyRestricted = documentSnapshot.getBoolean("isRestricted");
                        documentSnapshot.getReference().update("isRestricted",!isCurrentlyRestricted);
                    }
                    // Fetch students after updating the isRestricted field
                    fetchStudentsByClassCode(classCode);
                    // Change the text of the button
                    String buttonText = isCurrentlyRestricted? "Unrestrict all" : "Restrict all";
                    btnRestrict.setText(buttonText);
                    // Show Toast message
                    String toastMessage = isCurrentlyRestricted? "Successful Unrestriction Access for all students" : "Successful Restriction Access for all students";
                    Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to update isRestricted field: " + e.getMessage());
                    Toast.makeText(this, "Failed to update isRestricted field", Toast.LENGTH_SHORT).show();
                });
    }


    private void startListeningForIsRestrictedChanges(String classCode) {
        if (isRestrictedListener != null) {
            isRestrictedListener.remove();
        }
        isRestrictedListener = db.collection("Classes")
                .document(classCode)
                .collection("Students")
                .whereEqualTo("isRestricted", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("Firestore", "Failed to listen for isRestricted changes: " + e.getMessage());
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            boolean isCurrentlyRestricted = queryDocumentSnapshots.size() > 0;
                            String buttonText = isCurrentlyRestricted? "Unrestrict all" : "Restrict all";
                            btnRestrict.setText(buttonText);
                        }
                    }
                });
    }
}
