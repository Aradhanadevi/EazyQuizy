package com.saa.quizapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListofallAvailableQuizzes extends AppCompatActivity {
    private TextView heading;
    private ListView quizListView;
    private ArrayList<String> quizList; // Stores quiz names
    private ArrayList<String> quizIds;  // Stores quiz IDs
    private ArrayAdapter<String> adapter;
    private DatabaseReference quizRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listofall_available_quizzes);

        heading = findViewById(R.id.heading);
        quizListView = findViewById(R.id.quizListView);
        quizList = new ArrayList<>();
        quizIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizList);
        quizListView.setAdapter(adapter);

        // Retrieve values from intent
        String department = getIntent().getStringExtra("department");
        String semester = getIntent().getStringExtra("semester");
        String subject = getIntent().getStringExtra("subject");
        String chapter = getIntent().getStringExtra("chapter");

        if (department == null || semester == null || subject == null || chapter == null) {
            Toast.makeText(this, "Error: Missing quiz details!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Construct the final heading to match Firebase data
        String finalHeading = department + " Semester " + semester + " " + subject + " " + chapter;
        heading.setText(finalHeading);

        // Reference to Firebase
        quizRef = FirebaseDatabase.getInstance().getReference("Quizzes");

        // Fetch quizzes from Firebase
        fetchQuizzes(finalHeading);

        // Set click listener for quiz selection
        quizListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < quizIds.size()) {
                String selectedQuizId = quizIds.get(position);
                Log.d("QuizSelection", "Clicked quiz ID: " + selectedQuizId);

                Intent intent = new Intent(ListofallAvailableQuizzes.this, QuizQuestionsActivity.class);
                intent.putExtra("quizId", selectedQuizId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Error: Quiz ID not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchQuizzes(String finalHeading) {
        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                quizList.clear();
                quizIds.clear(); // Clear previous IDs

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizHeading = quizSnapshot.child("quizHeading").getValue(String.class);
                    String quizId = quizSnapshot.getKey(); // Get quiz ID

                    if (quizHeading != null && quizHeading.equals(finalHeading) && quizId != null) {
                        quizList.add(quizHeading); // Display name
                        quizIds.add(quizId);       // Store ID
                    }
                }

                if (quizList.isEmpty()) {
                    Toast.makeText(ListofallAvailableQuizzes.this, "No quizzes available", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to fetch quizzes: " + databaseError.getMessage());
                Toast.makeText(ListofallAvailableQuizzes.this, "Error fetching quizzes!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
