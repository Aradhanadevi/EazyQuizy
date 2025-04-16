package com.saa.quizapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView nameTextView, emailTextView, userTypeTextView, usernameTextView;
    private DatabaseReference databaseReference;
    private String username;
    Button showhistory,showAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        showhistory = findViewById(R.id.historyButton);

        if (username == null || username.isEmpty()) {
            Log.e("ProfileActivity", "No username found in SharedPreferences!");
            return;
        }

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Link UI elements
        nameTextView = findViewById(R.id.nameText);
        emailTextView = findViewById(R.id.emailText);
        userTypeTextView = findViewById(R.id.userTypeText);
        usernameTextView = findViewById(R.id.usernameText);
        showAnalysis=findViewById(R.id.showAnalysisButton);

        // Fetch user details
        fetchUserInfo();

        showhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, HistoryActivity.class);
                intent.putExtra("username", username); // Pass the username to the next activity
                startActivity(intent);
            }
        });

        //redirect to Analysis activity to show analysis.
        showAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent redirecttoAnalysis=new Intent(Profile.this,AnalysisActivity.class);
                startActivity(redirecttoAnalysis);
            }
        });



    }

    private void fetchUserInfo() {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullname = snapshot.child("fullname").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String userType = snapshot.child("userType").getValue(String.class);

                    // Update UI
                    nameTextView.setText(fullname);
                    emailTextView.setText(email);
                    userTypeTextView.setText(userType);
                    usernameTextView.setText(username);
                } else {
                    Log.d("ProfileActivity", "User data not found!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileActivity", "Error fetching data", error.toException());
            }
        });
    }
}
