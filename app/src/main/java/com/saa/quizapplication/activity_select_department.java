package com.saa.quizapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class activity_select_department extends AppCompatActivity {

    private Button buttonDepartment, buttonSemester, buttonProceedNext;
    private DatabaseReference databaseReference;
    private String selectedDepartment, selectedSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_department);

        databaseReference = FirebaseDatabase.getInstance().getReference("SelectQuiz").child("Departments");

        buttonDepartment = findViewById(R.id.buttonDepartment);
        buttonSemester = findViewById(R.id.buttonSemester);
        buttonProceedNext = findViewById(R.id.buttonProceedNext);

        buttonSemester.setEnabled(false);
        buttonProceedNext.setEnabled(false);

        // Select Department
        buttonDepartment.setOnClickListener(v -> fetchDepartments());

        // Select Semester (only after selecting Department)
        buttonSemester.setOnClickListener(v -> {
            if (selectedDepartment != null) {
                fetchSemesters(selectedDepartment);
            } else {
                Toast.makeText(this, "Please select a department first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Proceed to Subject Selection
        buttonProceedNext.setOnClickListener(v -> {
            if (selectedSemester != null) {
                Intent intent = new Intent(activity_select_department.this, SubjectSelectionActivity.class);
                intent.putExtra("department", selectedDepartment);
                intent.putExtra("semester", selectedSemester);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a semester first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch departments dynamically
    // Fetch departments dynamically
    private void fetchDepartments() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {  // Check if departments exist
                    Toast.makeText(activity_select_department.this, "No departments found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> departments = new ArrayList<>();
                for (DataSnapshot deptSnapshot : snapshot.getChildren()) {
                    String departmentName = deptSnapshot.getKey();
                    if (departmentName != null) {
                        departments.add(departmentName);
                    }
                }

                if (!departments.isEmpty()) {
                    showDialog("Select Department", departments, selected -> {
                        selectedDepartment = selected;
                        buttonDepartment.setText(selected);

                        selectedSemester = null;
                        buttonSemester.setText("Select Semester");
                        buttonSemester.setEnabled(true);
                        buttonProceedNext.setEnabled(false);
                    });
                } else {
                    Toast.makeText(activity_select_department.this, "No departments found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_select_department.this, "Failed to load departments!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Fetch semesters dynamically based on selected department
    private void fetchSemesters(String department) {
        databaseReference.child(department).child("Semester").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> semesters = new ArrayList<>();
                for (DataSnapshot semSnapshot : snapshot.getChildren()) {
                    semesters.add(semSnapshot.getKey());
                }

                if (!semesters.isEmpty()) {
                    showDialog("Select Semester", semesters, selected -> {
                        selectedSemester = selected;
                        buttonSemester.setText("Semester " + selected);
                        buttonProceedNext.setEnabled(true);
                    });
                } else {
                    Toast.makeText(activity_select_department.this, "No semesters found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_select_department.this, "Failed to load semesters!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Show selection dialog
    private void showDialog(String title, List<String> items, OnItemSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(items.toArray(new String[0]), (dialog, which) -> {
            listener.onItemSelected(items.get(which));
        });
        builder.show();
    }

    // Functional interface to replace Consumer<String>
    interface OnItemSelectedListener {
        void onItemSelected(String item);
    }
}
