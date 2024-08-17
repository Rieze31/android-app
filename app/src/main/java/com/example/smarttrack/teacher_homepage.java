package com.example.smarttrack;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class teacher_homepage extends AppCompatActivity {
    DrawerLayout drawerLayout;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_teacher_homepage);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

// Obtain a reference to the class_page fragment

        replaceFragment(new TeacherHomeFragment());

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new TeacherHomeFragment());
            } else if (itemId == R.id.classes) {

                replaceFragment(new class_page());
            } else if (itemId == R.id.notify) {

                replaceFragment(new TeacherNotifyFragment());
            } else if (itemId == R.id.profile) {

                replaceFragment(new TeacherProfileFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, teacher_login_page.class);
        startActivity(intent);
        finish();
    }
}
