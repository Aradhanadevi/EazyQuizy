package com.saa.quizapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class chapterDetailActivity extends AppCompatActivity {
    private String department, semester, subject, chapter;
    private TextView quizzesHeading;
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;
    private List<Quiz> quizList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_detail);

        quizzesHeading = findViewById(R.id.QuizzesHadding);
        recyclerView = findViewById(R.id.recyclerViewQuizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve data from Intent
        department = getIntent().getStringExtra("department");
        semester = getIntent().getStringExtra("semester");
        subject = getIntent().getStringExtra("subject");
        chapter = getIntent().getStringExtra("chapter");

        // If any value is missing, try retrieving from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        if (department == null) department = sharedPreferences.getString("department", "");
        if (semester == null) semester = sharedPreferences.getString("semester", "");
        if (subject == null) subject = sharedPreferences.getString("subject", "");
        if (chapter == null) chapter = sharedPreferences.getString("chapter", "");

        // If still empty, show error and exit
        if (department.isEmpty() || semester.isEmpty() || subject.isEmpty() || chapter.isEmpty()) {
            Toast.makeText(this, "Invalid data received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Save the selected values in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("department", department);
        editor.putString("semester", semester);
        editor.putString("subject", subject);
        editor.putString("chapter", chapter);
        editor.apply();

        // Set heading
        String selectedHeading = department + " Semester " + semester + " " + subject + " " + chapter;
        quizzesHeading.setText(selectedHeading);

        // Initialize list & adapter
        quizList = new ArrayList<>();
        quizAdapter = new QuizAdapter(this, quizList);
        recyclerView.setAdapter(quizAdapter);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Quizzes");

        fetchQuizzes(selectedHeading);

        // **Set item click listener**
        quizAdapter.setOnItemClickListener(position -> {
            if (position < quizList.size()) {
                String selectedQuizId = quizList.get(position).getQuizId();
                Log.d("QuizSelection", "Clicked quiz ID: " + selectedQuizId);

                Intent intent = new Intent(chapterDetailActivity.this, QuizQuestionsActivity.class);
                intent.putExtra("quizId", selectedQuizId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Error: Quiz ID not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchQuizzes(String selectedHeading) {
        databaseReference.orderByChild("quizHeading")
                .equalTo(selectedHeading)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        quizList.clear();
                        Log.d("FirebaseDebug", "Total quizzes found: " + snapshot.getChildrenCount());

                        for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                            Quiz quiz = quizSnapshot.getValue(Quiz.class);
                            String quizId = quizSnapshot.getKey();
                            Log.d("QuizData", "Quiz ID: " + quizId);
                            if (quiz != null && quiz.getQuizHeading().equals(selectedHeading)) {
                                quiz.setQuizId(quizId);
                                quizList.add(quiz);
                                Log.d("QuizData", "Quiz added: " + quiz.getQuizHeading());
                            }
                        }

                        if (quizList.isEmpty()) {
                            Log.w("QuizData", "No matching quizzes found.");
                            Toast.makeText(chapterDetailActivity.this, "No quizzes found for this heading.", Toast.LENGTH_SHORT).show();
                        }

                        quizAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseDebug", "Database error: " + error.getMessage());
                        Toast.makeText(chapterDetailActivity.this, "Failed to load quizzes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
