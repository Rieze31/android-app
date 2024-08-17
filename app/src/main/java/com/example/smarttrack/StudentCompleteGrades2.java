package com.example.smarttrack;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;

public class StudentCompleteGrades2 extends AppCompatActivity {
    private ImageButton btnBackLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_student_complete_grades2);
        btnBackLogin = (ImageButton) findViewById(R.id.btnBackLogin);
        btnBackLogin.setOnClickListener(v -> onBackPressed());
        // Get reference to the TableLayout
        TableLayout tableLayout = findViewById(R.id.tableLayout3);

        // Create the header row for subjects and grading information
        TableRow headerRow = new TableRow(this);

        // Add header TextViews for Subjects, First Grading, Second Grading, and Final Grades
        addHeaderTextView(headerRow, "Subjects\n", dpToPx(100));  // Adjust width for Subjects column
        addHeaderTextView(headerRow, "3rd\nQuarter", dpToPx(70));  // Adjust width for grading columns
        addHeaderTextView(headerRow, "4th\nQuarter", dpToPx(70));  // Adjust width for grading columns
        addHeaderTextView(headerRow, "Final\nGrades", dpToPx(70));  // Adjust width for grading columns

        // Add the header row to the TableLayout
        tableLayout.addView(headerRow);


        // Inside onCreate() method of StudentCompleteGrades activity
        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<String> subjects = intent.getStringArrayListExtra("subjects");
            ArrayList<String> firstGrades = intent.getStringArrayListExtra("firstGrades");
            ArrayList<String> secondGrades = intent.getStringArrayListExtra("secondGrades");

            if (subjects != null && firstGrades != null && secondGrades != null
                    && subjects.size() == firstGrades.size() && subjects.size() == secondGrades.size()) {
                double totalFinalGrade = 0;
                for (int i = 0; i < subjects.size(); i++) {
                    try {
                        // Create a new row for each subject
                        TableRow row = new TableRow(this);

                        // Create a TextView for the subject
                        TextView subjectTextView = new TextView(this);
                        subjectTextView.setText(subjects.get(i));
                        subjectTextView.setLayoutParams(new TableRow.LayoutParams(dpToPx(100), TableRow.LayoutParams.WRAP_CONTENT)); // Adjust width for Subjects column
                        subjectTextView.setMaxLines(2); // Allow subject to wrap to the next line if needed
                        subjectTextView.setBackgroundResource(R.drawable.valuecellborder);
                        subjectTextView.setMaxLines(1);
                        subjectTextView.setEllipsize(TextUtils.TruncateAt.END);
                        // Add the subject TextView to the row
                        row.addView(subjectTextView);

                        // Add TextViews for first and second quarter grades
                        TextView firstGradingTextView = new TextView(this);
                        firstGradingTextView.setText(firstGrades.get(i));
                        setGradingStyle(firstGradingTextView);
                        row.addView(firstGradingTextView);

                        TextView secondGradingTextView = new TextView(this);
                        secondGradingTextView.setText(secondGrades.get(i));
                        setGradingStyle(secondGradingTextView);
                        row.addView(secondGradingTextView);

                        // Calculate and add final grades
                        double firstGrade = parseGrade(firstGrades.get(i));
                        double secondGrade = parseGrade(secondGrades.get(i));
                        double finalGrade = (firstGrade + secondGrade) / 2;
                        totalFinalGrade += finalGrade;
                        TextView finalGradesTextView = new TextView(this);
                        finalGradesTextView.setText(String.valueOf(finalGrade));
                        setGradingStyle(finalGradesTextView);
                        row.addView(finalGradesTextView);

                        // Add the row to the TableLayout
                        tableLayout.addView(row);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Error adding row: " + e.getMessage());
                    }
                }

                // Calculate the average final grade (GWA)
                double gwa = totalFinalGrade / subjects.size();

                // Display the GWA in the TextView
                TextView gwaTextView = findViewById(R.id.gwa);
                gwaTextView.setText(String.valueOf(gwa + " GWA"));

                // Determine the ranking based on GWA
                TextView rankingTextView = findViewById(R.id.ranking);
                if (gwa < 90) {
                    rankingTextView.setText("No Honor");
                } else if (gwa >= 90 && gwa <= 94) {
                    rankingTextView.setText("With Honor");
                } else if (gwa >= 95 && gwa <= 97) {
                    rankingTextView.setText("With High Honor");
                } else if (gwa >= 98 && gwa <= 100) {
                    rankingTextView.setText("With Highest Honor");
                }
            } else {
                Log.d("StudentCompleteGrades2", "Invalid data received. Subjects: " + subjects + ", FirstGrades: " + firstGrades + ", SecondGrades: " + secondGrades);
                showToast("Invalid data received. Please try again.");
            }
        } else {
            Log.d("StudentCompleteGrades", "Intent is null.");
            showToast("Intent is null. Please try again.");

        }
    }

    // Method to parse the grade string and handle empty values
    private double parseGrade(String gradeString) {
        if (gradeString != null && !gradeString.trim().isEmpty()) {
            try {
                return Double.parseDouble(gradeString);
            } catch (NumberFormatException e) {
                // Handle invalid grade format
                e.printStackTrace();
                showToast("Invalid grade format: " + gradeString);
            }
        }
        // Return 0 if grade is empty or invalid
        return 0;
    }
    // Method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Method to set style for header TextView
    private void setHeaderStyle(TextView textView) {
        textView.setBackgroundResource(R.color.header);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setPadding(8, 8, 8, 8); // Add padding to the TextView
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD); // Set text to bold
        textView.setGravity(Gravity.CENTER); // Center align text
    }

    // Method to set style for grading TextViews
    private void setGradingStyle(TextView textView) {
        textView.setBackgroundResource(R.drawable.valuecellborder);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setGravity(Gravity.CENTER); // Center align text
        textView.setMaxLines(2); // Allow subject to wrap to the next line if needed
        // Add lines between columns and rows

    }

    // Method to add a header TextView to the header row with specified text and width
    private void addHeaderTextView(TableRow headerRow, String text, int width) {
        TextView headerTextView = new TextView(this);
        headerTextView.setText(text);
        setHeaderStyle(headerTextView);
        TableRow.LayoutParams params = new TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT);
        headerTextView.setLayoutParams(params);
        headerRow.addView(headerTextView);
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}