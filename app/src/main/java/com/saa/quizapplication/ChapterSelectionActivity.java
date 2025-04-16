package com.saa.quizapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import com.saa.quizapplication.adapters.ChapterAdapter;
import com.saa.quizapplication.model.Chapter;
import java.util.ArrayList;
import java.util.List;

public class ChapterSelectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChapterAdapter adapter;
    private List<Chapter> chapterList;
    private DatabaseReference databaseReference;
    private String subject, department, semester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_selection);

        recyclerView = findViewById(R.id.recyclerViewChapters);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        chapterList = new ArrayList<>();

        // Get department, semester, and subject from intent
        Intent intent = getIntent();
        department = intent.getStringExtra("department");
        semester = intent.getStringExtra("semester");
        subject = intent.getStringExtra("subject");

        // Debugging: Check if values are received
//        Log.d("DEBUG", "Received Department: " + department);
//        Log.d("DEBUG", "Received Semester: " + semester);
//        Log.d("DEBUG", "Received Subject: " + subject);

        if (department == null || semester == null || subject == null) {
            Toast.makeText(this, "Invalid selection. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Now initialize the adapter AFTER getting department, semester, and subject
        adapter = new ChapterAdapter(this, chapterList, department, semester, subject);
        recyclerView.setAdapter(adapter);

        // Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("SelectQuiz")
                .child("Departments")
                .child(department)
                .child("Semester")
                .child(semester)
                .child("Subjects")
                .child(subject)
                .child("Chapters");

        Log.d("FirebasePath", "Fetching Chapters from: " + databaseReference.toString());

        // Fetch chapters from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chapterList.clear();
                for (DataSnapshot chapterSnapshot : snapshot.getChildren()) {
                    String chapterName = chapterSnapshot.getValue(String.class);
                    if (chapterName != null) {
                        chapterList.add(new Chapter(chapterName));
                    }
                }

                if (chapterList.isEmpty()) {
                    Toast.makeText(ChapterSelectionActivity.this, "No chapters found", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged(); // Notify adapter after data is fetched
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChapterSelectionActivity.this, "Failed to load chapters", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
