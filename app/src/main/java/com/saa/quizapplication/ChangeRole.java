package com.saa.quizapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class ChangeRole extends AppCompatActivity {

    private EditText fullName;
    private Button fetchRole, updateRole;
    private Spinner roleSpinner;
    private DatabaseReference usersRef;
    private String selectedUserKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_role);

        fullName = findViewById(R.id.full_name_input);
        fetchRole = findViewById(R.id.fetch_role_btn);
        updateRole = findViewById(R.id.update_role_btn);
        roleSpinner = findViewById(R.id.role_spinner);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        List<String> roles = Arrays.asList("Admin", "Teacher", "Student", "Guest");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        fetchRole.setOnClickListener(v -> fetchUserRole());
        updateRole.setOnClickListener(v -> updateUserRole());
    }

    private void fetchUserRole() {
        String userFullName = fullName.getText().toString().trim();
        if (TextUtils.isEmpty(userFullName)) {
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.orderByChild("fullname").equalTo(userFullName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                selectedUserKey = userSnapshot.getKey();
                                String role = userSnapshot.child("userType").getValue(String.class);
                                if (role != null) {
                                    int position = ((ArrayAdapter<String>) roleSpinner.getAdapter()).getPosition(role);
                                    roleSpinner.setSelection(position);
                                    Toast.makeText(ChangeRole.this, "User role: " + role, Toast.LENGTH_SHORT).show();
                                }
                                return;
                            }
                        } else {
                            Toast.makeText(ChangeRole.this, "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChangeRole.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserRole() {
        if (selectedUserKey == null) {
            Toast.makeText(this, "Please fetch user role first", Toast.LENGTH_SHORT).show();
            return;
        }

        String newRole = roleSpinner.getSelectedItem().toString();
        usersRef.child(selectedUserKey).child("userType").setValue(newRole)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangeRole.this, "Role updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChangeRole.this, "Failed to update role", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
