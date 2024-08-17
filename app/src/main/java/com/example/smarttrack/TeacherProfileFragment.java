package com.example.smarttrack;

import static android.app.Activity.RESULT_OK;
import static com.example.smarttrack.TeacherEditProfile.PICK_IMAGE_REQUEST;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherProfileFragment extends Fragment {




    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private Uri selectedImageUri;
    private String mParam2;
    TextView txtFullName, txtEmail;
    Button btnLogout, btnEditProfile;
    ImageView teacherImage;
    private static TeacherProfileFragment instance = null;
    public TeacherProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TeacherProfileFragment newInstance(String param1, String param2) {
        TeacherProfileFragment fragment = new TeacherProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        instance = this;

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                // Resize the bitmap to 130sdp
                bitmap = Bitmap.createScaledBitmap(bitmap, convertDpToPx(130), convertDpToPx(130), true);
                teacherImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_profile, container, false);

        txtFullName = view.findViewById(R.id.txtFullName);
        txtEmail = view.findViewById(R.id.txtEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.editProfile);
        teacherImage = view.findViewById(R.id.teacherImage); // Add this line

        // Retrieve user data from Firestore
        loadData();

        btnLogout.setOnClickListener(v -> logout());
        btnEditProfile.setOnClickListener(v -> editProfile());
        return view;
    }
    private int convertDpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    public void loadData(){
        // Retrieve user data from Firestore
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String firstName = document.getString("FirstName");
                                String lastName = document.getString("LastName");
                                String fullName = firstName + " " + lastName;
                                String email = currentUser.getEmail();

                                // Update UI with full name and email
                                txtFullName.setText(fullName);
                                txtEmail.setText(email);

                                // Load profile picture
                                loadProfilePicture(uid);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to retrieve user information", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    public void loadProfilePicture(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                    // Use Picasso to load the image into the ImageView
                    Picasso.get().load(profilePictureUrl).into(teacherImage);
                } else {
                    // Handle case where profile picture URL is not available
                    Log.e("TAG", "Profile picture URL is null or empty");
                }
            } else {
                // Handle case where user document does not exist
                Log.e("TAG", "User document does not exist");
            }
        }).addOnFailureListener(e -> {
            // Handle any errors
            Log.e("TAG", "Error getting user document", e);
        });
    }

    public void logout(){

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), "Logout Successfully",
                                Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getActivity(),teacher_login_page.class);
                        startActivity(intent);
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
    public void editProfile(){
        Intent intent = new Intent(getActivity(), TeacherEditProfile.class);
        startActivity(intent);
    }
    public static TeacherProfileFragment getInstance() {
        return instance;
    }
}