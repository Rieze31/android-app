package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            checkUserAccessLevel(mAuth.getCurrentUser().getUid());
        } else{
            Intent intent = new Intent(MainActivity.this, choose_role.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        new Handler().postDelayed(new Runnable(){
            @Override
             public void run(){

            }
        }, 1000);
    }
    private void checkUserAccessLevel (String uid){
        DocumentReference df = fStore.collection("Users").document(uid);
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("TAG", "onSuccess" + documentSnapshot.getData());

                if (documentSnapshot.getString("isTeacher") != null) {
                    //user is teacher
                    Intent intent = new Intent(MainActivity.this, teacher_homepage.class);
                    startActivity(intent);
                    finish();
                }
                if (documentSnapshot.getString("isStudent") != null) {
                    //user is student
                    Intent intent = new Intent(MainActivity.this, student_homepage.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}