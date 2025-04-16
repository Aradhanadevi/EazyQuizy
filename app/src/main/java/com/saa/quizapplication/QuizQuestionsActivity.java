package com.saa.quizapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saa.quizapplication.model.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizQuestionsActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private Map<Integer, String> selectedAnswers = new HashMap<>();
    private TextView questionText, progressText, explanationText;
    private RadioGroup optionsGroup;
    private Button nextButton, prevButton, submitButton;
    private ScrollView explanationContainer;
    private String quizId;
    private int score = 0;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_questions);

        quizId = getIntent().getStringExtra("quizId");
        if (quizId == null) {
            Toast.makeText(this, "Quiz not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        progressText = findViewById(R.id.progressText);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        submitButton = findViewById(R.id.submitButton);
        explanationContainer = findViewById(R.id.explanationContainer);
        explanationText = findViewById(R.id.explanationText);
        timerText = findViewById(R.id.timerText);

        //initially keep disabled previous button
        prevButton.setEnabled(false);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Quizzes")
                .child(quizId)
                .child("questions");

        fetchQuestions();

        nextButton.setOnClickListener(v -> {
            Log.e("Next Button", "Clicked");

            int selectedId = optionsGroup.getCheckedRadioButtonId();
            Log.e("Next Button", "Selected ID: " + selectedId);

            // If no answer is selected, show a warning and stop execution
            if (selectedId == -1) {
                Log.e("Next Button", "No answer selected!");
                Toast.makeText(this, "Please select an answer before proceeding!", Toast.LENGTH_SHORT).show();
                return; // Stop execution
            }

            RadioButton selectedRadioButton = findViewById(selectedId);
            if (selectedRadioButton == null) {
                Log.e("saveSelectedAnswer", "Error: selectedRadioButton is NULL!");
                Toast.makeText(this, "Please select an answer before proceeding! no null", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save answer only if a valid selection is made
            Log.e("Next Button", "Saving Answer");
            saveSelectedAnswer();

            if (currentQuestionIndex < questionList.size() - 1) {
                prevButton.setEnabled(true);
                currentQuestionIndex++;
                Log.e("Next Button", "Moving to Question: " + currentQuestionIndex);
                displayQuestion();
            }

            if (currentQuestionIndex == questionList.size() - 1) {
                submitButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.GONE);
            }
        });





        prevButton.setOnClickListener(v -> {
            if(currentQuestionIndex==1){
                prevButton.setEnabled(false);
            }
            if (currentQuestionIndex > 0) {
                nextButton.setVisibility(View.VISIBLE);
                currentQuestionIndex--;
                displayQuestion();
            }

        });

        submitButton.setOnClickListener(v -> {
            stopTimer();
            checkAnswersAndDisplayScore();
        });
    }

    private void fetchQuestions() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList.clear();
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    Question question = questionSnapshot.getValue(Question.class);
                    if (question != null && question.getQuestion() != null) {
                        questionList.add(question);
                    }
                }
                if (!questionList.isEmpty()) {
                    startTimer(questionList.size());
                    displayQuestion();
                } else {
                    questionText.setText("No questions loaded.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                questionText.setText("Error fetching questions.");
            }
        });
    }


    //for implementation of timer

    // Timer function
    private void startTimer(int numQuestions) {
        timeLeftInMillis = numQuestions * 60000; // 1 minute per question
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                timerText.setText("Time's Up!");
                checkAnswersAndDisplayScore();
            }
        }.start();
    }

    // Update timer text
    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        timerText.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    // Stop timer when submitting
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void displayQuestion() {
        Log.e("displayQuestion", "Current Index: " + currentQuestionIndex);

        if (currentQuestionIndex < 0 || currentQuestionIndex >= questionList.size()) {
            Log.e("displayQuestion", "Invalid question index: " + currentQuestionIndex);
            return;
        }

        Question currentQuestion = questionList.get(currentQuestionIndex);
        if (currentQuestion == null) {
            Log.e("displayQuestion", "Current question is NULL");
            return;
        }

        questionText.setText(currentQuestion.getQuestion());
        optionsGroup.removeAllViews();
        explanationContainer.setVisibility(View.GONE);

        if (currentQuestion.getOptions() == null || currentQuestion.getOptions().isEmpty()) {
            questionText.setText("Error: No options available.");
            return;
        }

        String correctAnswer = currentQuestion.getCorrectOption(); // Correct answer
        String selectedAnswer = selectedAnswers.get(currentQuestionIndex); // User's selected answer

        Log.e("displayQuestion", "Correct Answer: " + correctAnswer);
        Log.e("displayQuestion", "User Selected Answer: " + selectedAnswer);

        for (String option : currentQuestion.getOptions()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option);
            radioButton.setTextSize(18f);
            radioButton.setPadding(20, 20, 20, 20);
            radioButton.setBackgroundResource(R.drawable.radio_selected);
            radioButton.setButtonDrawable(null);
            radioButton.setTextColor(getResources().getColor(R.color.black));

            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 20);
            radioButton.setLayoutParams(params);

            optionsGroup.addView(radioButton);

            if (selectedAnswer != null) {
                // Show previously selected answer
                if (selectedAnswer.equals(option)) {
                    radioButton.setChecked(true);

                    // Show explanation when revisiting
                    explanationText.setText(currentQuestion.getExplanation());
                    explanationContainer.setVisibility(View.VISIBLE);

                    // Highlight the correct or incorrect answer
                    if (selectedAnswer.equals(correctAnswer)) {
                        radioButton.setBackgroundColor(getResources().getColor(R.color.green)); // Correct answer - Green
                    } else {
                        radioButton.setBackgroundColor(getResources().getColor(R.color.red)); // Wrong answer - Red
                    }
                }

                // Highlight the correct answer even if it's not the selected one
                if (option.equals(correctAnswer)) {
                    radioButton.setBackgroundColor(getResources().getColor(R.color.green));
                }
                radioButton.setEnabled(false); // Prevent re-selection
            }

            // Allow answer selection only if it hasn’t been selected before
            radioButton.setOnClickListener(v -> {
                if (selectedAnswer == null) {
                    saveSelectedAnswer();
                    checkAnswerAndShowExplanation(radioButton, currentQuestion);
                }
            });
        }

        progressText.setText((currentQuestionIndex + 1) + "/" + questionList.size());
    }







    private void saveSelectedAnswer() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();

        // Prevent crash if no selection
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer before proceeding!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        if (selectedRadioButton == null) {
            Toast.makeText(this, "Please select an answer before proceeding! no null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the selected answer
        selectedAnswers.put(currentQuestionIndex, selectedRadioButton.getText().toString());

        // Disable further selection
        for (int i = 0; i < optionsGroup.getChildCount(); i++) {
            optionsGroup.getChildAt(i).setEnabled(false);
        }
    }



    private void checkAnswerAndShowExplanation(RadioButton selectedRadioButton, Question question) {
        String selectedAnswer = selectedRadioButton.getText().toString();
        String correctAnswer = question.getCorrectOption();
        boolean isCorrect = selectedAnswer.equals(correctAnswer);

        // Change color based on correctness
        if (isCorrect) {
            selectedRadioButton.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            selectedRadioButton.setBackgroundColor(getResources().getColor(R.color.red));

            // Highlight the correct answer immediately in green
            for (int i = 0; i < optionsGroup.getChildCount(); i++) {
                RadioButton optionButton = (RadioButton) optionsGroup.getChildAt(i);
                if (optionButton.getText().toString().equals(correctAnswer)) {
                    optionButton.setBackgroundColor(getResources().getColor(R.color.green));
                    break;
                }
            }
        }

        // Show explanation
        explanationText.setText(question.getExplanation());
        explanationContainer.setVisibility(View.VISIBLE);
    }


    private void checkAnswersAndDisplayScore() {
        score = 0;
        for (int i = 0; i < questionList.size(); i++) {
            String correctAnswer = questionList.get(i).getCorrectOption();
            String selectedAnswer = selectedAnswers.get(i);

            if (selectedAnswer != null && selectedAnswer.equals(correctAnswer)) {
                score++;
            }
        }

        // Store the score in Firebase
        saveQuizAttempt(score,quizId);

        Toast.makeText(this, "Your Score: " + score + "/" + questionList.size(), Toast.LENGTH_LONG).show();

        // Open the new page to display attempts
        Intent intent = new Intent(QuizQuestionsActivity.this, QuizResultsActivity.class);
        intent.putExtra("quizId", quizId);
        startActivity(intent);
    }


    private void saveQuizAttempt(int score, String quizId) {
        SharedPreferences sharedPreferences = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        if (username.isEmpty()) {
            Log.e("saveQuizAttempt", "No username found in SharedPreferences");
            return;
        }

        long timestamp = System.currentTimeMillis();

        // Reference to fetch quiz details (quizTitle & quizHeading)
        DatabaseReference quizRef = FirebaseDatabase.getInstance()
                .getReference("Quizzes")
                .child(quizId);

        // Fetch quiz details first
        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e("saveQuizAttempt", "Quiz data not found for quizId: " + quizId);
                    return;
                }

                // Get quizTitle and quizHeading
                final String quizTitle = snapshot.child("quizTitle").getValue(String.class);
                final String quizHeading = snapshot.child("quizHeading").getValue(String.class);

                // Call method to store attempt (passing title and heading)
                storeQuizAttempt(username, quizId, score, timestamp, quizTitle, quizHeading, snapshot.child("questions").getRef());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("saveQuizAttempt", "Database error: " + error.getMessage());
            }
        });
    }

    // ✅ Separate method to store quiz attempt
    private void storeQuizAttempt(String username, String quizId, int score, long timestamp, String quizTitle, String quizHeading, DatabaseReference questionsRef) {
        DatabaseReference quizAttemptsRef = FirebaseDatabase.getInstance()
                .getReference("quiz_attempts")
                .child(username)
                .child(quizId);

        DatabaseReference userAttemptRef = FirebaseDatabase.getInstance()
                .getReference("resultPageAttemptHistory")
                .child(username)
                .child(quizId);

        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalQuestions = (int) snapshot.getChildrenCount();
                quizAttemptsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot attemptSnapshot) {
                        Integer existingScore = attemptSnapshot.child("score").getValue(Integer.class);

                        if (existingScore == null || score > existingScore) {
                            Map<String, Object> attemptData = new HashMap<>();
                            attemptData.put("score", score);
                            attemptData.put("totalQuestions", totalQuestions);
                            attemptData.put("timestamp", timestamp);
                            attemptData.put("quizTitle", quizTitle);
                            attemptData.put("quizHeading", quizHeading);

                            quizAttemptsRef.setValue(attemptData)
                                    .addOnSuccessListener(aVoid -> Log.d("saveQuizAttempt", "Quiz attempt saved successfully"))
                                    .addOnFailureListener(e -> Log.e("saveQuizAttempt", "Failed to save quiz attempt", e));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("saveQuizAttempt", "Database error: " + error.getMessage());
                    }
                });
                // Store under resultPageAttemptHistory
                userAttemptRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long attemptCount = snapshot.getChildrenCount() + 1;
                        String attemptKey = "attempt" + attemptCount;

                        Map<String, Object> attemptHistoryData = new HashMap<>();
                        attemptHistoryData.put("score", score);
                        attemptHistoryData.put("timestamp", timestamp);
                        attemptHistoryData.put("quizTitle", quizTitle);
                        attemptHistoryData.put("quizHeading", quizHeading);

                        userAttemptRef.child(attemptKey).setValue(attemptHistoryData)
                                .addOnSuccessListener(aVoid -> Log.d("saveQuizAttempt", "Attempt history saved successfully"))
                                .addOnFailureListener(e -> Log.e("saveQuizAttempt", "Failed to save attempt history", e));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("saveQuizAttempt", "Database error: " + error.getMessage());
                    }
                });
                // Reference to store the highest score under Quizzes -> quizID -> userScores -> username
                DatabaseReference quizHighScoreRef = FirebaseDatabase.getInstance()
                        .getReference("Quizzes")
                        .child(quizId)
                        .child("userScores")
                        .child(username);
                quizHighScoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer existingScore = snapshot.child("score").getValue(Integer.class);

                        if (existingScore == null || score > existingScore) {
                            Map<String, Object> quizUserScoreData = new HashMap<>();
                            quizUserScoreData.put("score", score);
                            quizUserScoreData.put("timestamp", timestamp);

                            quizHighScoreRef.setValue(quizUserScoreData)
                                    .addOnSuccessListener(aVoid -> Log.d("saveQuizAttempt", "High score saved successfully"))
                                    .addOnFailureListener(e -> Log.e("saveQuizAttempt", "Failed to save high score", e));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("saveQuizAttempt", "Database error: " + error.getMessage());
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("saveQuizAttempt", "Error fetching questions count: " + error.getMessage());
            }
        });
    }
}

//
//The saveQuizAttempt() method fetches the quiz details first.
//Then, it calls storeQuizAttempt() with the retrieved data.