package com.saa.quizapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private DatabaseReference databaseReference;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        username = getIntent().getStringExtra("username");
        historyContainer = findViewById(R.id.historyContainer);
        databaseReference = FirebaseDatabase.getInstance().getReference("quiz_attempts").child(username);

        fetchQuizHistory();
    }

    private void fetchQuizHistory() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

                for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                    String quizTitle = quizSnapshot.child("quizTitle").getValue(String.class);
                    String quizHeading = quizSnapshot.child("quizHeading").getValue(String.class);
                    Integer score = quizSnapshot.child("score").getValue(Integer.class);
                    Integer totalQuestions = quizSnapshot.child("totalQuestions").getValue(Integer.class);
                    Long timestamp = quizSnapshot.child("timestamp").getValue(Long.class);

                    String formattedDate = timestamp != null ? sdf.format(new Date(timestamp)) : "Unknown Date";

                    // Inflate card layout
                    View cardView = LayoutInflater.from(HistoryActivity.this)
                            .inflate(R.layout.quiz_history_card, historyContainer, false);

                    // Set data to card
                    TextView quizHeadingView = cardView.findViewById(R.id.quizHeadingTextView);
                    TextView quizTitleView = cardView.findViewById(R.id.quizTitleTextView);
                    TextView scoreView = cardView.findViewById(R.id.scoreTextView);
                    TextView dateView = cardView.findViewById(R.id.dateTextView);

                    quizHeadingView.setText(quizHeading != null ? quizHeading : "Unknown Quiz");
                    quizTitleView.setText(quizTitle != null ? quizTitle : "No Title");
                    scoreView.setText("Score: " + (score != null ? score : 0) + " / " + (totalQuestions != null ? totalQuestions : 0));
                    dateView.setText("Attempted on: " + formattedDate);

                    // Add card to container
                    historyContainer.addView(cardView);
                }

                if (!snapshot.hasChildren()) {
                    TextView noHistory = new TextView(HistoryActivity.this);
                    noHistory.setText("No quiz history found.");
                    noHistory.setTextSize(16f);
                    historyContainer.addView(noHistory);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HistoryActivity", "Error fetching quiz history", error.toException());
            }
        });
    }
}