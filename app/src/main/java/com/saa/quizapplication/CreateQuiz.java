package com.saa.quizapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
//import com.google.auth.oauth2.GoogleCredentials;


import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateQuiz extends AppCompatActivity {
    TextView getDetails;
    EditText noOfQuestionsInput, quizTitleedt;
    Button generateQuestionButton, saveQuizButton;
    LinearLayout questionsContainer;
    String selectedDepartment, selectedSemester, selectedSubject, selectedChapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference("Quizzes");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        generateQuestionButton = findViewById(R.id.generateQuestions);
        noOfQuestionsInput = findViewById(R.id.noofquestions);
        questionsContainer = findViewById(R.id.questionsContainer);
        getDetails = findViewById(R.id.quizheading);
        saveQuizButton = findViewById(R.id.generatequizinfirebase);
        quizTitleedt = findViewById(R.id.quizTitle);

        Intent getDetailsfromSelectedDetails = getIntent();
        selectedDepartment = getDetailsfromSelectedDetails.getStringExtra("department");
        selectedSemester = getDetailsfromSelectedDetails.getStringExtra("semester");
        selectedSubject = getDetailsfromSelectedDetails.getStringExtra("subject");
        selectedChapter = getDetailsfromSelectedDetails.getStringExtra("chapter");

        String quizHeading = selectedDepartment + " Semester " + selectedSemester + " " + selectedSubject + " " + selectedChapter;
        getDetails.setText(quizHeading);

        generateQuestionButton.setOnClickListener(view -> generateQuestions());
        saveQuizButton.setOnClickListener(view -> saveQuizToFirebase(quizHeading));
    }

    private void generateQuestions() {
        String input = noOfQuestionsInput.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            noOfQuestionsInput.setError("Enter a valid number");
            return;
        }

        int numberOfQuestions;
        try {
            numberOfQuestions = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            noOfQuestionsInput.setError("Invalid number");
            return;
        }

        questionsContainer.removeAllViews();

        for (int i = 1; i <= numberOfQuestions; i++) {
            TextView questionLabel = new TextView(this);
            questionLabel.setText("Question " + i);
            questionsContainer.addView(questionLabel);

            EditText questionInput = new EditText(this);
            questionInput.setHint("Enter question " + i);
            questionInput.setTag("question_" + i);
            questionsContainer.addView(questionInput);

            LinearLayout optionsContainer = new LinearLayout(this);
            optionsContainer.setOrientation(LinearLayout.VERTICAL);

            for (int j = 1; j <= 4; j++) {
                EditText optionInput = new EditText(this);
                optionInput.setHint("Option " + j);
                optionInput.setTag("question_" + i + "_option_" + j);
                optionsContainer.addView(optionInput);
            }
            questionsContainer.addView(optionsContainer);

            EditText correctOptionInput = new EditText(this);
            correctOptionInput.setHint("Enter correct option (1-4)");
            correctOptionInput.setTag("question_" + i + "_correct");
            questionsContainer.addView(correctOptionInput);

            EditText explanationInput = new EditText(this);
            explanationInput.setHint("Enter explanation");
            explanationInput.setTag("question_" + i + "_explanation");
            questionsContainer.addView(explanationInput);
        }

        saveQuizButton.setVisibility(View.VISIBLE);
    }

    private void saveQuizToFirebase(String quizHeading) {
        String quizTitle = quizTitleedt.getText().toString().trim();
        if (TextUtils.isEmpty(quizTitle)) {
            quizTitleedt.setError("Enter a quiz title");
            return;
        }

        String input = noOfQuestionsInput.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "Enter number of questions", Toast.LENGTH_SHORT).show();
            return;
        }

        int numberOfQuestions;
        try {
            numberOfQuestions = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> quizData = new HashMap<>();
        quizData.put("quizHeading", quizHeading);
        quizData.put("quizTitle", quizTitle);

        List<Map<String, Object>> questionsList = new ArrayList<>();

        for (int i = 1; i <= numberOfQuestions; i++) {
            Map<String, Object> questionData = new HashMap<>();
            EditText questionInput = questionsContainer.findViewWithTag("question_" + i);

            if (questionInput == null || TextUtils.isEmpty(questionInput.getText().toString().trim())) {
                Toast.makeText(this, "Question " + i + " is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            questionData.put("question", questionInput.getText().toString().trim());

            List<String> optionsList = new ArrayList<>();
            for (int j = 1; j <= 4; j++) {
                EditText optionInput = questionsContainer.findViewWithTag("question_" + i + "_option_" + j);
                if (optionInput == null || TextUtils.isEmpty(optionInput.getText().toString().trim())) {
                    Toast.makeText(this, "Option " + j + " for question " + i + " is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                optionsList.add(optionInput.getText().toString().trim());
            }
            questionData.put("options", optionsList);

            EditText correctOptionInput = questionsContainer.findViewWithTag("question_" + i + "_correct");
            if (correctOptionInput == null || TextUtils.isEmpty(correctOptionInput.getText().toString().trim())) {
                Toast.makeText(this, "Correct option for question " + i + " is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            questionData.put("correctOption", correctOptionInput.getText().toString().trim());

            EditText explanationInput = questionsContainer.findViewWithTag("question_" + i + "_explanation");
            if (explanationInput != null && !TextUtils.isEmpty(explanationInput.getText().toString().trim())) {
                questionData.put("explanation", explanationInput.getText().toString().trim());
            }

            questionsList.add(questionData);
        }

        quizData.put("questions", questionsList);

        databaseReference.push().setValue(quizData).addOnSuccessListener(aVoid -> {
            Toast.makeText(CreateQuiz.this, "Quiz saved successfully!", Toast.LENGTH_SHORT).show();

            // ✅ Step 6: Send push notification to topic "allUsers"
            new Thread(() -> sendNotificationToAllUsers(quizTitle)).start();

        }).addOnFailureListener(e ->
                Toast.makeText(CreateQuiz.this, "Failed to save quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void sendNotificationToAllUsers(String quizTitle) {
        try {
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "key=YOUR_SERVER_KEY"); // 🔥 Replace with your Firebase server key
            conn.setRequestProperty("Content-Type", "application/json");

            String body = "{"
                    + "\"to\": \"/topics/allUsers\","
                    + "\"notification\": {"
                    + "\"title\": \"New Quiz Available!\","
                    + "\"body\": \"" + quizTitle + " is now live. Try it now!\""
                    + "}"
                    + "}";

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d("FCM_RESPONSE", "Response Code: " + responseCode);

        } catch (Exception e) {
            Log.e("FCM_ERROR", "Error sending FCM notification", e);
        }
    }
}
