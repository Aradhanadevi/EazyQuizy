package com.saa.quizapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import com.saa.quizapplication.adapters.QuizAdapter;
import com.saa.quizapplication.model.Quiz;
import java.util.ArrayList;
import java.util.List;

public class QuizSelectionActivity extends AppCompatActivity {
    private TextView headingTextView;
    private RecyclerView recyclerView;
    private QuizAdapter adapter;
    private List<Quiz> quizList;
    private DatabaseReference databaseReference;

    private String department, semester, subject, chapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_selection);

        headingTextView = findViewById(R.id.headingTextView);
        recyclerView = findViewById(R.id.recyclerViewQuizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        quizList = new ArrayList<>();
        adapter = new QuizAdapter(this, quizList);
        recyclerView.setAdapter(adapter);

        // Get data from intent
        department = getIntent().getStringExtra("department");
        semester = getIntent().getStringExtra("semester");
        subject = getIntent().getStringExtra("subject");
        chapter = getIntent().getStringExtra("chapter");

        if (department == null || semester == null || subject == null || chapter == null) {
            Toast.makeText(this, "Invalid data received.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the heading text
        headingTextView.setText(department + " - Sem " + semester + " - " + subject + " - " + chapter);

        // Firebase database reference for quizzes
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("SelectQuiz")
                .child("Departments")
                .child(department)
                .child("Semester")
                .child(semester)
                .child("Subjects")
                .child(subject)
                .child("Chapters")
                .child(chapter)
                .child("Quizzes");

        // Fetch quizzes
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quizList.clear();
                for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                    String quizName = quizSnapshot.getValue(String.class);
                    if (quizName != null) {
                        quizList.add(new Quiz(quizName));
                    }
                }

                if (quizList.isEmpty()) {
                    Toast.makeText(QuizSelectionActivity.this, "No quizzes found", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuizSelectionActivity.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
