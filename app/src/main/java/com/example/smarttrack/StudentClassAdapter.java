package com.example.smarttrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StudentClassAdapter extends RecyclerView.Adapter<StudentClassAdapter.ViewHolder>{
    private List<StudentClassModel> dataList;
    private Context context;
    private FirebaseFirestore db;


    public StudentClassAdapter(Context context,List<StudentClassModel> dataList,  FirebaseFirestore db) {
        this.db = db;
        this.context = context;
        this.dataList = dataList;

    }
    // Method to return the current list of data being displayed
    public List<StudentClassModel> getData() {
        return dataList;
    }
    // Method to update adapter dataset
    public void setData(List<StudentClassModel> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_class_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StudentClassModel classItem = dataList.get(position);

        // Bind data to views
        holder.subjectTextView.setText(classItem.getSubject());
        holder.teacherNameTextView.setText(classItem.getTeacherName());
        Picasso.get().load(classItem.getTeacherImage()).into(holder.teacherImageView);

        // Handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.inflate(R.menu.student_popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();
                            if (itemId == R.id.action_leave) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Are you sure you want to leave in this class?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Obtain the class code and student ID for the selected item
                                                String classCode = classItem.getCode(); // Assuming you have a getCode() method in StudentClassModel
                                                String studentId = classItem.getLRN(); // Assuming you have a getLRN() method in StudentClassModel

                                                deleteDocumentFromFirestore(classCode, studentId);
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
        TextView subjectTextView, yourGradeTextView, teacherNameTextView;
        ImageView teacherImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subject);

            teacherNameTextView = itemView.findViewById(R.id.teacherName);
            teacherImageView = itemView.findViewById(R.id.teacherImage);
        }
    }
    // Method to delete document from Firestore
    private void deleteDocumentFromFirestore(String classCode, String studentId) {
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
