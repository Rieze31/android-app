package com.example.smarttrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder>{
    private List<ClassModel> dataList;
    private Context context;
    private Spinner cbSemester;

    private FirebaseFirestore db;
    public ClassAdapter(Context context,List<ClassModel> dataList) {
        this.context = context;
        this.dataList = dataList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        db = FirebaseFirestore.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClassModel item = dataList.get(position);

        // Bind data to views

        holder.subjectTextView.setText(item.getSubject());
        holder.gradeTextView.setText(item.getGrade());
        holder.teacherNameTextView.setText(item.getTeacherName());
        // Check if the image URL is not empty before loading
        if (item.getTeacherImage() != null && !item.getTeacherImage().isEmpty()) {
            Picasso.get().load(item.getTeacherImage()).into(holder.teacherImageView);
        } else {
            // Handle the case where the image URL is empty
            // For example, you can set a placeholder image or hide the ImageView
        }

        // Handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log the classCode
                Log.d("ClassAdapter", "ClassCode: " + item.getCode());

                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.class_popup_menu);

                // Retrieve position using holder
                int position = holder.getAdapterPosition();

                // Check if position is valid
                if (position != RecyclerView.NO_POSITION) {
                    final ClassModel classModel = dataList.get(position); // Make classModel final

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();

                            if (itemId == R.id.action_view) {
                                // Query Firestore to check the value of the semester field for the selected class
                                db.collection("Classes")
                                        .document(classModel.getCode()) // Assuming code is the document ID
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    // Get the value of the semester field
                                                    String semester = documentSnapshot.getString("semester");

                                                    // Check if the semester value is "1st Semester" or "2nd Semester"
                                                    if (semester != null && semester.equals("1st Semester")) {
                                                        Intent intent = new Intent(context, TeacherSectionPage.class);
                                                        intent.putExtra("code", classModel.getCode());
                                                        intent.putExtra("grade", classModel.getGrade());
                                                        intent.putExtra("strand", classModel.getStrand());
                                                        intent.putExtra("section", classModel.getSection());
                                                        context.startActivity(intent);
                                                    } else if (semester != null && semester.equals("2nd Semester")) {
                                                        Intent intent = new Intent(context, TeacherSectionPage2.class);
                                                        intent.putExtra("code", classModel.getCode());
                                                        intent.putExtra("grade", classModel.getGrade());
                                                        intent.putExtra("strand", classModel.getStrand());
                                                        intent.putExtra("section", classModel.getSection());
                                                        context.startActivity(intent);
                                                    } else {
                                                        // Handle other semester values if needed
                                                    }
                                                } else {
                                                    Log.d("ClassAdapter", "Document does not exist");
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("ClassAdapter", "Error querying Firestore", e);
                                            }
                                        });

                                return true;
                            } else if (itemId == R.id.action_edit) {
                                Intent intent = new Intent(context, EditClass.class);
                                // Put the necessary data into the intent's extras
                                intent.putExtra("code", classModel.getCode());
                                intent.putExtra("subject", classModel.getSubject());
                                intent.putExtra("grade", classModel.getGrade());
                                intent.putExtra("strand", classModel.getStrand());
                                intent.putExtra("section", classModel.getSection());
                                context.startActivity(intent);
                            } else if (itemId == R.id.action_delete) {
                                // Build the AlertDialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Are you sure you want to delete this class?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteItem(position,classModel);
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
                            }
                            return true;
                        }
                    });

                    popupMenu.show();
                }
            }
        });
    }
    public void setData(List<ClassModel> newData) {
        this.dataList.clear();
        this.dataList.addAll(newData);
        notifyDataSetChanged();
    }
    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSubject;
        private Spinner txtGrade;
        private Spinner txtStrand;
        private TextView txtSection;

        private TextView txtTeacherName;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtGrade = itemView.findViewById(R.id.cbGrade);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtStrand = itemView.findViewById(R.id.cbStrand);
            txtSection = itemView.findViewById(R.id.txtSection);
            txtTeacherName = itemView.findViewById(R.id.teacherName);
        }

        public void bind(ClassModel model) {
            txtSubject.setText(model.getSubject());

            // Set selected item for the grade Spinner
            ArrayAdapter<CharSequence> gradeAdapter = (ArrayAdapter<CharSequence>) txtGrade.getAdapter();
            if (gradeAdapter != null) {
                int gradePosition = gradeAdapter.getPosition(model.getGrade());
                if (gradePosition != -1) {
                    txtGrade.setSelection(gradePosition);
                }
            }

            // Set selected item for the strand Spinner
            ArrayAdapter<CharSequence> strandAdapter = (ArrayAdapter<CharSequence>) txtStrand.getAdapter();
            if (strandAdapter != null) {
                int strandPosition = strandAdapter.getPosition(model.getStrand());
                if (strandPosition != -1) {
                    txtStrand.setSelection(strandPosition);
                }
            }

            txtSection.setText(model.getSection());
            txtTeacherName.setText(model.getTeacherName());
        }
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectTextView, gradeTextView, teacherNameTextView;
        ImageView teacherImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subject);
            gradeTextView = itemView.findViewById(R.id.grade);
            teacherNameTextView = itemView.findViewById(R.id.teacherName);
            teacherImageView = itemView.findViewById(R.id.teacherImage);
        }
    }
    private void deleteItem(int position, ClassModel classModel) {
        // Step 1: Remove the item from the dataset
        dataList.remove(position);

        // Step 2: Notify the adapter that the dataset has changed
        notifyItemRemoved(position);

        // Step 3: Delete the corresponding document from Firestore
        String classCode = classModel.getCode();
        if (classCode != null) {
            db.collection("Classes")
                    .document(classCode) // Assuming each item has a unique identifier (e.g., document ID)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Document successfully deleted from Firestore
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to delete document from Firestore
                            // You might want to handle this scenario (e.g., show a toast)
                        }
                    });
        } else {
            // Handle null class code (e.g., log a message or show a toast)
            Log.e("deleteItem", "Class code is null");
        }
    }
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
