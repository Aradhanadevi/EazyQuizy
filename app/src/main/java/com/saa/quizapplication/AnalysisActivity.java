package com.saa.quizapplication;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.mikephil.charting.utils.ColorTemplate;
public class AnalysisActivity extends AppCompatActivity {
    private static final String TAG = "AnalysisActivity";
    private BarChart barChart;
    private PieChart pieChart;
    private String username;
    private DatabaseReference userRef;
    private DatabaseReference quizzesRef;
    private Map<String, List<Integer>> subjectScores = new HashMap<>();
    private Map<String, String> quizTitles = new HashMap<>(); // Store quizID -> quizTitle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);

        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "Guest");

        if ("Guest".equals(username)) {
            Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("resultPageAttemptHistory").child(username);
        quizzesRef = FirebaseDatabase.getInstance().getReference("Quizzes");

        fetchQuizData();
    }

    private void fetchQuizData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> quizIDs = new ArrayList<>();

                for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                    String quizID = quizSnapshot.getKey(); // Get Quiz ID
                    if (quizID == null) continue;

                    quizIDs.add(quizID); // Store for fetching quiz titles
                    List<Integer> scores = new ArrayList<>();

                    for (DataSnapshot attempt : quizSnapshot.getChildren()) {
                        Integer score = attempt.child("score").getValue(Integer.class);
                        if (score != null) {
                            scores.add(score);
                        }
                    }

                    if (!scores.isEmpty()) {
                        subjectScores.put(quizID, scores); // Temporarily store with quizID
                    }
                }

                if (subjectScores.isEmpty()) {
                    Toast.makeText(AnalysisActivity.this, "No data available", Toast.LENGTH_SHORT).show();
                    return;
                }

                fetchQuizTitles(quizIDs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void fetchQuizTitles(List<String> quizIDs) {
        for (String quizID : quizIDs) {
            quizzesRef.child(quizID).child("quizTitle").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String quizTitle = snapshot.getValue(String.class);
                        if (quizTitle != null) {
                            quizTitles.put(quizID, quizTitle); // Store quiz title
                        }
                    }

                    // Ensure all titles are fetched before updating charts
                    if (quizTitles.size() == subjectScores.size()) {
                        updateChartWithQuizTitles();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching quiz title: " + error.getMessage());
                }
            });
        }
    }

    private void updateChartWithQuizTitles() {
        Map<String, List<Integer>> updatedScores = new HashMap<>();

        for (Map.Entry<String, List<Integer>> entry : subjectScores.entrySet()) {
            String quizID = entry.getKey();
            String quizTitle = quizTitles.getOrDefault(quizID, quizID); // Use title if available, otherwise quizID
            updatedScores.put(quizTitle, entry.getValue());
        }

        displayBarChart(updatedScores);
        displayPieChart(updatedScores);
    }

    private void displayBarChart(Map<String, List<Integer>> subjectScores) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, List<Integer>> entry : subjectScores.entrySet()) {
            int avgScore = getAverage(entry.getValue());
            entries.add(new BarEntry(index, avgScore));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Average Score per Quiz");
        dataSet.setColor(Color.BLUE);
        BarData barData = new BarData(dataSet);

        barChart.setData(barData);
        barChart.invalidate(); // Refresh chart

        Description description = new Description();
        description.setText("Quiz Performance");
        barChart.setDescription(description);
    }



    private void displayPieChart(Map<String, List<Integer>> subjectScores) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Use predefined colors or define custom colors
        int[] colorArray = new int[]{
                Color.parseColor("#FF5733"), Color.parseColor("#33FF57"), Color.parseColor("#3357FF"),
                Color.parseColor("#F9A825"), Color.parseColor("#9C27B0"), Color.parseColor("#00BCD4"),
                Color.parseColor("#E91E63"), Color.parseColor("#8BC34A"), Color.parseColor("#FF9800"),
                Color.parseColor("#9E9E9E"), Color.parseColor("#3F51B5"), Color.parseColor("#CDDC39"),
                Color.parseColor("#607D8B"), Color.parseColor("#795548"), Color.parseColor("#673AB7"),
                Color.parseColor("#FFC107"), Color.parseColor("#FFEB3B"), Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"), Color.parseColor("#F44336"), Color.parseColor("#B71C1C"),
                Color.parseColor("#1B5E20"), Color.parseColor("#0D47A1"), Color.parseColor("#311B92"),
                Color.parseColor("#004D40")
        };


        int colorIndex = 0;
        for (Map.Entry<String, List<Integer>> entry : subjectScores.entrySet()) {
            int avgScore = getAverage(entry.getValue());
            entries.add(new PieEntry(avgScore, entry.getKey()));

            // Assign a distinct color for each entry
            colors.add(colorArray[colorIndex % colorArray.length]);
            colorIndex++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Quiz Performance");
        dataSet.setColors(colors); // Set custom colors
        dataSet.setValueTextSize(14f); // Make values more readable
        dataSet.setValueTextColor(Color.BLACK); // Ensure contrast
        dataSet.setSliceSpace(3f); // Space between slices for better clarity

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Customize legend
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextSize(12f);
        pieChart.getLegend().setWordWrapEnabled(true); // Ensure all legends are visible
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        // Set description
        Description description = new Description();
//        description.setText("Quiz Performance Breakdown");
        description.setTextSize(12f);
        pieChart.setDescription(description);

        pieChart.setUsePercentValues(false); // Show percentage values
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelColor(Color.BLACK);

        // Remove the legend at the bottom
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false); // Disable the legend

        pieChart.invalidate(); // Refresh chart
    }



    private int getAverage(List<Integer> scores) {
        int sum = 0;
        for (int score : scores) {
            sum += score;
        }
        return scores.isEmpty() ? 0 : sum / scores.size();
    }
}