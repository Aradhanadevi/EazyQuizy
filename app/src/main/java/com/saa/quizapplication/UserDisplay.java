package com.saa.quizapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class UserDisplay extends AppCompatActivity {
    ListView userListView;
    DatabaseReference databaseReference;
    ArrayList<String> userList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_display);

        // Initialize UI elements
        userListView = findViewById(R.id.userListView);
        userList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        userListView.setAdapter(adapter);

        // Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Fetch Users from Firebase
        fetchUsers();

        // Handle item clicks
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUsername = userList.get(position).split(" \\(")[0]; // Extract username

                // Store selected username in SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", selectedUsername);
                editor.apply();

                // Redirect to Profile.java
                Intent intent = new Intent(UserDisplay.this, Profile.class);
                startActivity(intent);
            }
        });
    }

    private void fetchUsers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    String fullname = userSnapshot.child("fullname").getValue(String.class);

                    // Debugging Log
                    Log.d("FirebaseData", "Username: " + username + ", Fullname: " + fullname);

                    if (username != null && fullname != null) {
                        userList.add(username + " (" + fullname + ")");
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database Error: " + error.getMessage());
            }
        });
    }
}
