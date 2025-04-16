package com.saa.quizapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saa.quizapplication.login_signup.SignIn;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizResultsActivity extends AppCompatActivity {
    private static final String TAG = "QuizResultsActivity";
    private LinearLayout scoresContainer;
    private String username, quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results);

        scoresContainer = findViewById(R.id.scoresContainer);
        Button homeButton = findViewById(R.id.btn_home);
        Button backToQuizListButton = findViewById(R.id.btn_back_to_quiz_list);

        quizId = getIntent().getStringExtra("quizId");
        if (quizId == null) {
            showToastAndFinish("Quiz not found!");
            return;
        }

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "Guest");

        if ("Guest".equals(username)) {
            showToastAndFinish("Error: Username is missing!");
            Log.e(TAG, "Username is missing.");
            return;
        }

        fetchAttemptsAndScores();

        homeButton.setOnClickListener(v -> navigateToHome());
        backToQuizListButton.setOnClickListener(v -> navigateToQuizList());
    }

    private void fetchAttemptsAndScores() {
        DatabaseReference attemptsRef = FirebaseDatabase.getInstance()
                .getReference("resultPageAttemptHistory").child(username).child(quizId);

        attemptsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "No attempts found.");
                    return;
                }

                List<AttemptData> attemptList = new ArrayList<>();

                for (DataSnapshot attemptSnapshot : snapshot.getChildren()) {
                    String attemptName = attemptSnapshot.getKey();
                    Integer score = attemptSnapshot.child("score").getValue(Integer.class);
                    Long timestamp = attemptSnapshot.child("timestamp").getValue(Long.class);

                    if (attemptName != null && score != null && timestamp != null) {
                        attemptList.add(new AttemptData(attemptName, score, timestamp));
                    }
                }

                // Sort attempts in descending order (latest first)
                Collections.sort(attemptList, (a, b) -> b.getAttemptNumber() - a.getAttemptNumber());

                displayAttempts(attemptList);

                // Send the latest quiz attempt result via email
                if (!attemptList.isEmpty()) {
                    AttemptData latestAttempt = attemptList.get(0);
                    sendQuizResults(latestAttempt);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void displayAttempts(List<AttemptData> attempts) {
        scoresContainer.removeAllViews();
        for (AttemptData attempt : attempts) {
            TextView textView = new TextView(this);
            String dateTime = convertTimestampToDateTime(attempt.getTimestamp());
            String timeZone = new SimpleDateFormat("zzz", Locale.getDefault()).format(new Date(attempt.getTimestamp()));

            textView.setText(String.format(Locale.getDefault(), "%s - Score: %d\n%s\nTime Zone: %s",
                    attempt.getAttemptName(), attempt.getScore(), dateTime, timeZone));
            textView.setTextSize(16);
            textView.setPadding(10, 10, 10, 10);

            View separator = new View(this);
            separator.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2));
            separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            scoresContainer.addView(textView);
            scoresContainer.addView(separator);
        }
    }

    private void sendQuizResults(AttemptData attempt) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.e(TAG, "No authenticated user found. Redirecting to login.");
            Toast.makeText(this, "Session expired! Please log in again.", Toast.LENGTH_SHORT).show();

            // Redirect user to login
            Intent intent = new Intent(this, SignIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String userEmail = user.getEmail();
        Log.d(TAG, "Preparing to send email to: " + userEmail);

        String subject = "Your Quiz Results";
        String dateTime = convertTimestampToDateTime(attempt.getTimestamp());
        String messageBody = "Hello " + username + ",\n\n"
                + "You recently completed a quiz. Here are your results:\n\n"
                + "Quiz ID: " + quizId + "\n"
                + "Attempt: " + attempt.getAttemptName() + "\n"
                + "Score: " + attempt.getScore() + "\n"
                + "Date: " + dateTime + "\n\n"
                + "Thank you for using our quiz app!";

        new Thread(() -> {
            try {
                EmailSender emailSender = new EmailSender();
                emailSender.sendEmail(userEmail, subject, messageBody);
                Log.d(TAG, "Email sending initiated successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error while sending email: " + e.getMessage(), e);
            }
        }).start();
    }

    private void navigateToHome() {
        Intent intent = new Intent(QuizResultsActivity.this, Welcome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToQuizList() {
        SharedPreferences sharedPreferences = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        Intent intent = new Intent(QuizResultsActivity.this, ListofallAvailableQuizzes.class);
        intent.putExtra("department", sharedPreferences.getString("department", ""));
        intent.putExtra("semester", sharedPreferences.getString("semester", ""));
        intent.putExtra("subject", sharedPreferences.getString("subject", ""));
        intent.putExtra("chapter", sharedPreferences.getString("chapter", ""));
        startActivity(intent);
        finish();
    }

    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String convertTimestampToDateTime(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Helper class for attempt data
    static class AttemptData {
        private final String attemptName;
        private final int score;
        private final long timestamp;

        public AttemptData(String attemptName, int score, long timestamp) {
            this.attemptName = attemptName;
            this.score = score;
            this.timestamp = timestamp;
        }

        public String getAttemptName() {
            return attemptName;
        }

        public int getScore() {
            return score;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int getAttemptNumber() {
            return Integer.parseInt(attemptName.replaceAll("[^0-9]", ""));
        }
    }
}
