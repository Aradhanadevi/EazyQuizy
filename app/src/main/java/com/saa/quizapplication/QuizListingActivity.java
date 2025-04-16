package com.saa.quizapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saa.quizapplication.adapters.QuizAdapter;
import com.saa.quizapplication.model.Quiz;

import java.util.ArrayList;
import java.util.List;

public class QuizListingActivity extends AppCompatActivity {
    private TextView headingTextView;
    private RecyclerView recyclerView;
    private QuizAdapter adapter;
    private List<Quiz> quizList;
    private DatabaseReference databaseReference;
    private String department, semester, subject, chapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_listing);

        headingTextView = findViewById(R.id.headingTextView);
        recyclerView = findViewById(R.id.recyclerViewQuizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        quizList = new ArrayList<>();
        adapter = new QuizAdapter(this, quizList);
        recyclerView.setAdapter(adapter);

        sharedPreferences = getSharedPreferences("QuizAppPrefs", Context.MODE_PRIVATE);

        // Get data from Intent
        Intent intent = getIntent();
        department = intent.getStringExtra("department");
        semester = intent.getStringExtra("semester");
        subject = intent.getStringExtra("subject");
        chapter = intent.getStringExtra("chapter");

        if (department == null || semester == null || subject == null || chapter == null) {
            // Retrieve from SharedPreferences if intent data is missing
            department = sharedPreferences.getString("department", null);
            semester = sharedPreferences.getString("semester", null);
            subject = sharedPreferences.getString("subject", null);
            chapter = sharedPreferences.getString("chapter", null);
        } else {
            // Save to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("department", department);
            editor.putString("semester", semester);
            editor.putString("subject", subject);
            editor.putString("chapter", chapter);
            editor.apply();
        }

        if (department == null || semester == null || subject == null || chapter == null) {
            Toast.makeText(this, "Invalid data received.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set Heading
        headingTextView.setText(department + " Semester " + semester + " - " + subject + " - " + chapter);

        // Firebase Database Path
        databaseReference = FirebaseDatabase.getInstance().getReference("Quizzes");

        fetchQuizzes();
    }

    private void fetchQuizzes() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quizList.clear();
                for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                    Quiz quiz = quizSnapshot.getValue(Quiz.class);
                    if (quiz != null) {
                        quizList.add(quiz);
                    }
                }

                if (quizList.isEmpty()) {
                    Toast.makeText(QuizListingActivity.this, "No quizzes found.", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuizListingActivity.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
                Log.e("QuizListingActivity", "Database error: " + error.getMessage());
            }
        });
    }
}
