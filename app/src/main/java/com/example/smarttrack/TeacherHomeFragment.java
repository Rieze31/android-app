package com.example.smarttrack;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TeacherHomeFragment extends Fragment {

    private TextView txtWelcome;
    private TextView txtWord;
    private TextView txtWordMeaning;
    private TextView txtDate; // New TextView for displaying the date
    private FirebaseFirestore fStore;
    private FirebaseUser currentUser;
    private Handler handler;
    private Runnable runnable;

    public TeacherHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fStore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        handler = new Handler();
        runnable = () -> {
            fetchWordOfTheDay();
            handler.postDelayed(runnable, 24 * 60 * 60 * 1000);
        };
        handler.post(runnable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_home, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtWelcome = view.findViewById(R.id.txtWelcome);
        txtWord = view.findViewById(R.id.txtWord);
        txtWordMeaning = view.findViewById(R.id.txtWordMeaning);
        txtDate = view.findViewById(R.id.txtDate);

        if (currentUser != null) {
            fStore.collection("Users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String firstName = document.getString("FirstName");
                                txtWelcome.setText(getString(R.string.welcome_message, firstName));
                            }
                        }
                    });

            fetchWordOfTheDay();
            String currentDate = getCurrentDate();
            txtDate.setText(currentDate);
        }
    }

    private void fetchWordOfTheDay() {
        fStore.collection("WordsOfTheDay")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get the word and its meaning from the document
                            String word = document.getString("word");
                            String meaning = document.getString("meaning");

                            // Update the TextViews with the word and its meaning
                            txtWord.setText(word);
                            txtWordMeaning.setText(meaning);
                        }
                    }
                });
    }
    private String getCurrentDate() {
        // Create a Date object representing the current date
        Date currentDate = new Date();

        // Create a date formatter
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Format the current date as a string using the date formatter
        return dateFormat.format(currentDate);
    }
}
