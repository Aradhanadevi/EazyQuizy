package com.saa.quizapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ScoreAnalysisActivity extends AppCompatActivity {
    private ListView quizListView;
    private TextView topScorersTitle;
    private PieChart pieChart;
    private BarChart barChart;
    private Button btnBackToQuiz;
    private ArrayAdapter<String> quizAdapter;
    private List<String> quizTitles = new ArrayList<>();
    private List<String> quizIds = new ArrayList<>();
    private DatabaseReference quizRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_analysis);

        quizListView = findViewById(R.id.quizListView);
        topScorersTitle = findViewById(R.id.topScorersTitle);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        btnBackToQuiz = findViewById(R.id.btnBackToList);
        btnBackToQuiz.setVisibility(View.GONE);  // Ensure it is visible
// Button now explicitly "Back to Quiz"

        quizRef = FirebaseDatabase.getInstance().getReference("Quizzes");

        quizAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizTitles);
        quizListView.setAdapter(quizAdapter);

        fetchQuizList();

        quizListView.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedQuizId = quizIds.get(position);
            String selectedQuizTitle = quizTitles.get(position);
            fetchAllScores(selectedQuizId, selectedQuizTitle);
        });

        btnBackToQuiz.setText("Back to Quiz"); // Explicitly setting button text
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
                Toast.makeText(ScoreAnalysisActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllScores(String quizId, String quizTitle) {
        DatabaseReference scoresRef = quizRef.child(quizId).child("userScores");

        scoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HashMap<String, Object>> scoreEntries = new ArrayList<>();
                int highestScore = 0;
                btnBackToQuiz.setVisibility(View.VISIBLE);  // Show button when data loads

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    Integer score = userSnapshot.child("score").getValue(Integer.class);

                    if (username != null && score != null) {
                        HashMap<String, Object> entry = new HashMap<>();
                        entry.put("username", username);
                        entry.put("score", score);
                        scoreEntries.add(entry);

                        if (score > highestScore) {
                            highestScore = score;
                        }
                    }
                }

                Collections.sort(scoreEntries, (o1, o2) -> (int) o2.get("score") - (int) o1.get("score"));

                ArrayList<PieEntry> pieEntries = new ArrayList<>();
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                ArrayList<String> scoreList = new ArrayList<>();
                for (int i = 0; i < scoreEntries.size(); i++) {
                    HashMap<String, Object> entry = scoreEntries.get(i);
                    String username = (String) entry.get("username");
                    int score = (int) entry.get("score");
                    float percentage = (highestScore == 0) ? 0 : ((float) score / highestScore) * 100;

                    pieEntries.add(new PieEntry(percentage, username));
                    barEntries.add(new BarEntry(i, score));

                    scoreList.add(username + " - " + score + " (" + (int) percentage + "%)");
                }

                updatePieChart(pieEntries, quizTitle);
                updateBarChart(barEntries, quizTitle, scoreEntries);
                updateScoreList(scoreList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScoreAnalysisActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                btnBackToQuiz.setVisibility(View.VISIBLE);  // Show button when data loads

            }
        });
    }

    private void updatePieChart(ArrayList<PieEntry> pieEntries, String quizTitle) {
        PieDataSet dataSet = new PieDataSet(pieEntries, "Score Distribution");
        dataSet.setColors(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.MAGENTA);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        Description description = new Description();
        description.setText("Score Distribution for " + quizTitle);
        pieChart.setDescription(description);

        pieChart.invalidate(); // Refresh chart
        pieChart.setVisibility(View.VISIBLE);
    }

    private void updateBarChart(ArrayList<BarEntry> barEntries, String quizTitle, List<HashMap<String, Object>> scoreEntries) {
        BarDataSet dataSet = new BarDataSet(barEntries, "Scores");

        // Generate different colors for each bar
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        colors.add(Color.MAGENTA);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.LTGRAY);
        colors.add(Color.DKGRAY);
        colors.add(Color.BLACK);
        colors.add(Color.rgb(255, 165, 0)); // Orange
        colors.add(Color.rgb(128, 0, 128)); // Purple

        // Apply different colors dynamically
        dataSet.setColors(colors.subList(0, Math.min(colors.size(), barEntries.size())));

        dataSet.setValueTextSize(12f);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        Description description = new Description();
        description.setText("Scores for " + quizTitle);
        barChart.setDescription(description);

        barChart.invalidate(); // Refresh chart
        barChart.setVisibility(View.VISIBLE);
    }

    private void updateScoreList(ArrayList<String> scoreList) {
        ArrayAdapter<String> scoreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scoreList);
        quizListView.setAdapter(scoreAdapter);
    }

    private void showQuizList() {
        fetchQuizList();
        quizListView.setAdapter(quizAdapter);
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        btnBackToQuiz.setVisibility(View.GONE);
    }
}