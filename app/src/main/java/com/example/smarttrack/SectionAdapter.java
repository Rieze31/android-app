package com.example.smarttrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {
    private List<SectionModel> dataList;
    private Context context;
    private FirebaseFirestore db;
    private SectionModel selectedSectionItem;
    private String selectedQuarter;
    private TeacherSectionPage activityInstance;
    public SectionAdapter(Context context, List<SectionModel> dataList, FirebaseFirestore db,String selectedQuarter,TeacherSectionPage activityInstance) {
        this.db = db;
        this.context = context;
        this.dataList = dataList;
        this.selectedQuarter = selectedQuarter;
        this.activityInstance = activityInstance;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_recycler, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SectionModel sectionItem = dataList.get(position);

        // Bind data to views
        holder.studentMidTermAverage.setText(sectionItem.getAverage());
        holder.studentGrade.setText(sectionItem.getGrade());
        holder.studentNameTextView.setText(sectionItem.getStudentName());
        // Load image using Picasso
        Picasso.get().load(sectionItem.getStudentImage()).into(holder.studentImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedSectionItem = sectionItem; // Set selectedSectionItem to the clicked sectionItem
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.inflate(R.menu.section_popup_menu);

                    // Check if the item is restricted
                    boolean isRestricted = selectedSectionItem.isRestricted();
                    // Update the text of the menu item based on the restriction status
                    MenuItem restrictMenuItem = popupMenu.getMenu().findItem(R.id.action_restrict);
                    if (isRestricted) {
                        restrictMenuItem.setTitle("Unrestrict");
                    } else {
                        restrictMenuItem.setTitle("Restrict");
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();
                            if (itemId == R.id.action_view_profile) {
                                // Handle action to view profile
                                Intent intent = new Intent(context, ViewProfile.class);
                                intent.putExtra("LRN", selectedSectionItem.getLRN()); // Pass LRN to StudentEditProfile activity
                                intent.putExtra("email", selectedSectionItem.getEmail()); // Pass email to StudentEditProfile activity
                                intent.putExtra("FirstName", selectedSectionItem.getFirstName()); // Pass first name to StudentEditProfile activity
                                intent.putExtra("LastName", selectedSectionItem.getLastName()); // Pass last name to StudentEditProfile activity
                                intent.putExtra("imageURL", selectedSectionItem.getStudentImage()); // Pass image URL to StudentEditProfile activity
                                intent.putExtra("editable", false); // Indicate that fields are not editable
                                context.startActivity(intent);
                                return true;

                            } else if (itemId == R.id.action_add_grades) {
                                // Check if the selectedQuarter is "1st Quarter" or "2nd Quarter"
                                if (selectedQuarter.equals("1st Quarter")) {
                                    // Show dialog to input grades for 1st quarter
                                    showInputDialog("1st Quarter");
                                } else if (selectedQuarter.equals("2nd Quarter")) {
                                    // Show dialog to input grades for 2nd quarter
                                    showInputDialog("2nd Quarter");
                                } else {
                                    // Inform user to select a valid quarter
                                    Toast.makeText(context, "Please select a valid quarter to add grades.", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            } else if (itemId == R.id.action_restrict) {
                                // Update isRestricted field in Firestore
                                boolean newRestriction = !isRestricted; // Toggle restriction
                                updateRestrictionInFirestore(selectedSectionItem.getCode(), selectedSectionItem.getLRN(), newRestriction);
                                return true;
                            } else if (itemId == R.id.action_remove) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Are you sure you want to remove this student in this class?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Obtain the class code and student ID for the selected item
                                                String classCode = sectionItem.getCode(); // Assuming you have a getCode() method in StudentClassModel
                                                String studentId = sectionItem.getLRN(); // Assuming you have a getLRN() method in StudentClassModel

                                                removeStudentFromClass(classCode, studentId);
                                                // Remove the item from the list and notify adapter
                                                dataList.remove(adapterPosition);
                                                notifyItemRemoved(adapterPosition);
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                // Show the AlertDialog
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentNameTextView, studentGrade, studentMidTermAverage;
        ImageView studentImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            studentMidTermAverage = itemView.findViewById(R.id.studentAverage);
            studentGrade = itemView.findViewById(R.id.studentGrades);
            studentNameTextView = itemView.findViewById(R.id.studentName);
            studentImageView = itemView.findViewById(R.id.studentImage);
        }
    }

    // Method to update isRestricted field in Firestore
    private void updateRestrictionInFirestore(String classCode, String LRN, boolean isRestricted) {
        // Get a reference to the document in Firestore
        DocumentReference studentRef = db.collection("Classes").document(classCode)
                .collection("Students").document(LRN);

        // Check the current value of isRestricted in Firestore
        studentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve the current value of isRestricted
                Boolean currentIsRestricted = documentSnapshot.getBoolean("isRestricted");

                // Check if the current value of isRestricted is different from the new value
                if (currentIsRestricted != null && currentIsRestricted != isRestricted) {
                    // Update the isRestricted field in Firestore
                    studentRef.update("isRestricted", isRestricted)
                            .addOnSuccessListener(aVoid -> {
                                // Update successful
                                // Show appropriate message based on restriction status
                                String message = isRestricted ? "Restricted" : "Unrestricted";
                                Toast.makeText(context, message + " access for student", Toast.LENGTH_SHORT).show();

                                // Update the dataList
                                updateDataList(classCode, LRN, isRestricted);

                                // Notify adapter that data has changed
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure
                                Toast.makeText(context, "Failed to update restriction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // No need to update if the current and new values are the same
                    String message = isRestricted ? "Student already restricted" : "Student already unrestricted";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Document does not exist
                Toast.makeText(context, "Student document not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle failure
            Toast.makeText(context, "Failed to check restriction status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    // Method to update dataList
    private void updateDataList(String classCode, String LRN, boolean isRestricted) {
        for (SectionModel item : dataList) {
            if (item.getCode().equals(classCode) && item.getLRN().equals(LRN)) {
                item.setRestricted(isRestricted);
                break;
            }
        }
    }
    private void showInputDialog(String quarter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Student Grade for " + quarter);

        // Set up the input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newGrade = input.getText().toString();
                // Add the grade to Firestore database under the Classes.Students sub-collection for the specified quarter
                addGradeToFirestore(selectedSectionItem.getCode(), selectedSectionItem.getLRN(), quarter, newGrade);
                if(quarter.equals("1st Quarter")){
                    activityInstance.fetchStudentsByClassCode(selectedSectionItem.getCode());
                } else if(quarter.equals("2nd Quarter")){
                    activityInstance.fetchStudentsByClassCode2(selectedSectionItem.getCode(), dataList);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addGradeToFirestore(String classCode, String LRN, String quarter, String grade) {
        // Get a reference to the document in Firestore
        DocumentReference studentRef = db.collection("Classes").document(classCode)
                .collection("Students").document(LRN);

        // Add the grade to Firestore
        studentRef.update("grades." + quarter, grade)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    Toast.makeText(context, "Grade updated successfully for " + quarter, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(context, "Failed to add grade for " + quarter + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void removeStudentFromClass(String classCode, String studentId) {
        // Print out classCode and studentId for debugging
        Log.d("DEBUG", "Class Code: " + classCode);
        Log.d("DEBUG", "Student ID: " + studentId);

        // Get a reference to the document in Firestore
        DocumentReference studentRef = db.collection("Classes").document(classCode)
                .collection("Students").document(studentId);

        // Delete the document
        studentRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DEBUG", "DocumentSnapshot successfully deleted!");
                        // Document successfully deleted
                        Toast.makeText(context, "Student has been remove from the class", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DEBUG", "Error deleting document", e);
                        // Handle any errors
                    }
                });
    }
}
