package com.saa.quizapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddNewDepartmentFromAdmin extends AppCompatActivity {
    private EditText departmentName, semesterName, subjectName, chapterName;
    private Button addQuizButton;
    private DatabaseReference databaseRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_department_from_admin);

        // Initialize UI elements
        departmentName = findViewById(R.id.department_name);
        semesterName = findViewById(R.id.semester_name);
        subjectName = findViewById(R.id.subject_name);
        chapterName = findViewById(R.id.chapter_name);
        addQuizButton = findViewById(R.id.add_quiz_btn);

        // Initialize Firebase references
        databaseRef = FirebaseDatabase.getInstance().getReference().child("SelectQuiz").child("Departments");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        addQuizButton.setOnClickListener(v -> addQuizToFirebase());
    }

    private void addQuizToFirebase() {
        String department = departmentName.getText().toString().trim();
        String semester = semesterName.getText().toString().trim();
        String subject = subjectName.getText().toString().trim();
        String chapter = chapterName.getText().toString().trim();

        if (TextUtils.isEmpty(department) || TextUtils.isEmpty(semester) ||
                TextUtils.isEmpty(subject) || TextUtils.isEmpty(chapter)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference chapterRef = databaseRef.child(department)
                .child("Semester").child(semester)
                .child("Subjects").child(subject)
                .child("Chapters");

        // Get the current number of chapters to determine the next key
        chapterRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                long nextChapterNumber = task.getResult().getChildrenCount() + 1; // Get count and add 1
                chapterRef.child(String.valueOf(nextChapterNumber)).setValue(chapter)
                        .addOnCompleteListener(storeTask -> {
                            if (storeTask.isSuccessful()) {
                                Toast.makeText(AddNewDepartmentFromAdmin.this, "Chapter added successfully", Toast.LENGTH_SHORT).show();
                                clearFields();
                            } else {
                                Toast.makeText(AddNewDepartmentFromAdmin.this, "Failed to add chapter", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // If no chapters exist, start from 1
                chapterRef.child("1").setValue(chapter)
                        .addOnCompleteListener(storeTask -> {
                            if (storeTask.isSuccessful()) {
                                Toast.makeText(AddNewDepartmentFromAdmin.this, "Chapter added successfully", Toast.LENGTH_SHORT).show();
                                clearFields();
                            } else {
                                Toast.makeText(AddNewDepartmentFromAdmin.this, "Failed to add chapter", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }


    private void clearFields() {
        departmentName.setText("");
        semesterName.setText("");
        subjectName.setText("");
        chapterName.setText("");
    }
}
