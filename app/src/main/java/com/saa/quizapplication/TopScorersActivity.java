package com.saa.quizapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TopScorersActivity extends AppCompatActivity {

    private ListView quizListView;
    private TextView topScorersTitle;
    private Button btnBackToQuiz;
    private ArrayAdapter<String> quizAdapter;
    private List<String> quizTitles = new ArrayList<>();
    private List<String> quizIds = new ArrayList<>();
    private DatabaseReference quizRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_scorers);

        quizListView = findViewById(R.id.quizListView);
        topScorersTitle = findViewById(R.id.topScorersTitle);
        btnBackToQuiz = findViewById(R.id.btnBackToList);
        btnBackToQuiz.setVisibility(View.GONE);

        quizRef = FirebaseDatabase.getInstance().getReference("Quizzes");

        quizAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles);
        quizListView.setAdapter(quizAdapter);

        fetchQuizList();

        quizListView.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedQuizId = quizIds.get(position);
            String selectedQuizTitle = quizTitles.get(position);
            fetchTop5Scores(selectedQuizId, selectedQuizTitle);
        });

        btnBackToQuiz.setText("Back to Quiz List");
        btnBackToQuiz.setOnClickListener(view -> showQuizList());
    }

    private void fetchQuizList() {
        quizRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quizTitles.clear();
                quizIds.clear();

                for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                    String quizId = quizSnapshot.getKey();
                    String quizTitle = quizSnapshot.child("quizTitle").getValue(String.class);

                    if (quizTitle != null) {
                        quizTitles.add(quizTitle);
                        quizIds.add(quizId);
                    }
                }

                quizAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TopScorersActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTop5Scores(String quizId, String quizTitle) {
        DatabaseReference scoresRef = quizRef.child(quizId).child("userScores");

        scoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HashMap<String, Object>> scoreEntries = new ArrayList<>();
                btnBackToQuiz.setVisibility(View.VISIBLE);

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    Integer score = userSnapshot.child("score").getValue(Integer.class);

                    if (username != null && score != null) {
                        HashMap<String, Object> entry = new HashMap<>();
                        entry.put("username", username);
                        entry.put("score", score);
                        scoreEntries.add(entry);
                    }
                }

                Collections.sort(scoreEntries, (o1, o2) -> (int) o2.get("score") - (int) o1.get("score"));

                if (scoreEntries.size() > 5) {
                    scoreEntries = scoreEntries.subList(0, 5);
                }

                ArrayList<String> scoreList = new ArrayList<>();
                for (int i = 0; i < scoreEntries.size(); i++) {
                    HashMap<String, Object> entry = scoreEntries.get(i);
                    String username = (String) entry.get("username");
                    int score = (int) entry.get("score");

                    scoreList.add((i + 1) + ". " + username + " - " + score + " points");
                }

                topScorersTitle.setText("Top 5 Scorers for " + quizTitle);
                ArrayAdapter<String> scoreAdapter = new ArrayAdapter<>(TopScorersActivity.this, android.R.layout.simple_list_item_1, scoreList);
                quizListView.setAdapter(scoreAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TopScorersActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuizList() {
        fetchQuizList();
        quizListView.setAdapter(quizAdapter);
        topScorersTitle.setText("Select a Quiz to View Top Scorers");
        btnBackToQuiz.setVisibility(View.GONE);
    }
}
