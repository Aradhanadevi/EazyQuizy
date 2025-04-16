package com.saa.quizapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.saa.quizapplication.adapters.SubjectAdapter;
import com.saa.quizapplication.model.Subject;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SubjectSelectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SubjectAdapter adapter;
    private List<Subject> subjectList;
    private DatabaseReference databaseReference;
    private String department, semester; // Declare department and semester at class level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection);

        recyclerView = findViewById(R.id.recyclerViewSubjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Get department and semester from Intent first
        Intent intent = getIntent();
        department = intent.getStringExtra("department");
        semester = intent.getStringExtra("semester");

        if (department == null || semester == null) {
            Toast.makeText(this, "Invalid selection. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize the subject list and adapter
        subjectList = new ArrayList<>();
        adapter = new SubjectAdapter(this, subjectList, department, semester);
        recyclerView.setAdapter(adapter);

        // Set up Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("SelectQuiz")
                .child("Departments")
                .child(department)
                .child("Semester")
                .child(semester)
                .child("Subjects");

        Log.d("FirebasePath", "Path: " + databaseReference.toString());

        // Fetch data from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subjectName = subjectSnapshot.getKey(); // Get subject name (e.g., "Math", "Physics")
                    List<String> chapters = new ArrayList<>();

                    if (subjectSnapshot.hasChild("Chapters")) { // Check if chapters exist
                        for (DataSnapshot chapterSnapshot : subjectSnapshot.child("Chapters").getChildren()) {
                            chapters.add(chapterSnapshot.getKey()); // Add chapter names
                        }
                    }

                    Subject subject = new Subject(subjectName, chapters);
                    subjectList.add(subject);
                }

                if (subjectList.isEmpty()) {
                    Toast.makeText(SubjectSelectionActivity.this, "No subjects found", Toast.LENGTH_SHORT).show();
                } else {
                    adapter.notifyDataSetChanged(); // Notify adapter about data change
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SubjectSelectionActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
