package com.saa.quizapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SelectDetailsActivity extends AppCompatActivity {

    private Button btnDepartment, btnSemester, btnSubject, btnChapter, btnNext, btnCreateQuiz;
    private DatabaseReference databaseReference;
    private String selectedDepartment, selectedSemester, selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_details);

        databaseReference = FirebaseDatabase.getInstance().getReference("SelectQuiz").child("Departments");

        btnDepartment = findViewById(R.id.button_department);
        btnSemester = findViewById(R.id.button_semester);
        btnSubject = findViewById(R.id.button_subject);
        btnChapter = findViewById(R.id.button_chapter);
        btnNext = findViewById(R.id.btn_next);
        btnCreateQuiz = findViewById(R.id.createQuizRedirectBtn);

        btnDepartment.setOnClickListener(v -> showDepartmentMenu());
        btnSemester.setOnClickListener(v -> showSemesterMenu());
        btnSubject.setOnClickListener(v -> showSubjectMenu());
        btnChapter.setOnClickListener(v -> showChapterMenu());
        btnCreateQuiz.setOnClickListener(view -> {
            if (selectedDepartment == null || selectedSemester == null || selectedSubject == null) {
                showToast("Please select all details before proceeding.");
                return;
            }

            Intent redirectToCreateQuiz = new Intent(SelectDetailsActivity.this, CreateQuiz.class);
            redirectToCreateQuiz.putExtra("department", selectedDepartment);
            redirectToCreateQuiz.putExtra("semester", selectedSemester);
            redirectToCreateQuiz.putExtra("subject", selectedSubject);
            redirectToCreateQuiz.putExtra("chapter", btnChapter.getText().toString());
            redirectToCreateQuiz.putExtra("SelectedDetailsheading",selectedDepartment+" "+selectedSemester+" "+selectedSubject+" "+btnChapter.getText().toString());// Store selected chapter text

            startActivity(redirectToCreateQuiz);
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedDepartment == null || selectedSemester == null || selectedSubject == null || btnChapter.getText().toString().equals("Select Chapter")) {
                    showToast("Please select all details before proceeding.");
                    return;
                }

                Intent listOfAvailableQuizzesRedirect = new Intent(SelectDetailsActivity.this, ListofallAvailableQuizzes.class);
                listOfAvailableQuizzesRedirect.putExtra("department", selectedDepartment);
                listOfAvailableQuizzesRedirect.putExtra("semester", selectedSemester);
                listOfAvailableQuizzesRedirect.putExtra("subject", selectedSubject);
                listOfAvailableQuizzesRedirect.putExtra("chapter", btnChapter.getText().toString());

                startActivity(listOfAvailableQuizzesRedirect);
            }
        });


    }

    private void showDepartmentMenu() {
        PopupMenu popupMenu = new PopupMenu(this, btnDepartment);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popupMenu.getMenu().clear();
                for (DataSnapshot deptSnapshot : snapshot.getChildren()) {
                    popupMenu.getMenu().add(deptSnapshot.getKey());
                }
                popupMenu.setOnMenuItemClickListener(item -> {
                    selectedDepartment = item.getTitle().toString();
                    btnDepartment.setText(selectedDepartment);
                    resetSemesterAndBelow();
                    return true;
                });
                popupMenu.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load departments.");
            }
        });
    }

    private void showSemesterMenu() {
        if (selectedDepartment == null) {
            showToast("Please select a department first.");
            return;
        }

        PopupMenu popupMenu = new PopupMenu(this, btnSemester);
        databaseReference.child(selectedDepartment).child("Semester").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popupMenu.getMenu().clear();
                for (DataSnapshot semSnapshot : snapshot.getChildren()) {
                    popupMenu.getMenu().add(semSnapshot.getKey());
                }
                popupMenu.setOnMenuItemClickListener(item -> {
                    selectedSemester = item.getTitle().toString();
                    btnSemester.setText("Semester " + selectedSemester);
                    resetSubjectAndChapter();
                    return true;
                });
                popupMenu.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load semesters.");
            }
        });
    }

    private void showSubjectMenu() {
        if (selectedDepartment == null || selectedSemester == null) {
            showToast("Please select a department and semester first.");
            return;
        }

        PopupMenu popupMenu = new PopupMenu(this, btnSubject);
        databaseReference.child(selectedDepartment).child("Semester").child(selectedSemester).child("Subjects")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        popupMenu.getMenu().clear();
                        for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                            popupMenu.getMenu().add(subjectSnapshot.getKey());
                        }
                        popupMenu.setOnMenuItemClickListener(item -> {
                            selectedSubject = item.getTitle().toString();
                            btnSubject.setText(selectedSubject);
                            resetChapter();
                            return true;
                        });
                        popupMenu.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showToast("Failed to load subjects.");
                    }
                });
    }

    private void showChapterMenu() {
        if (selectedDepartment == null || selectedSemester == null || selectedSubject == null) {
            showToast("Please select a department, semester, and subject first.");
            return;
        }

        PopupMenu popupMenu = new PopupMenu(this, btnChapter);
        databaseReference.child(selectedDepartment).child("Semester").child(selectedSemester).child("Subjects")
                .child(selectedSubject).child("Chapters")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        popupMenu.getMenu().clear();
                        for (DataSnapshot chapterSnapshot : snapshot.getChildren()) {
                            String chapterNumber = chapterSnapshot.getKey();
                            String chapterName = chapterSnapshot.getValue(String.class);
                            popupMenu.getMenu().add(chapterName != null ? chapterName : chapterNumber);
                        }
                        popupMenu.setOnMenuItemClickListener(item -> {
                            btnChapter.setText(item.getTitle().toString());
                            btnNext.setVisibility(View.VISIBLE);
                            return true;
                        });
                        popupMenu.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showToast("Failed to load chapters.");
                    }
                });
    }

    private void resetSemesterAndBelow() {
        selectedSemester = null;
        selectedSubject = null;
        btnSemester.setText("Select Semester");
        btnSemester.setVisibility(View.VISIBLE);
        resetSubjectAndChapter();
    }

    private void resetSubjectAndChapter() {
        selectedSubject = null;
        btnSubject.setText("Select Subject");
        btnSubject.setVisibility(View.VISIBLE);
        resetChapter();
    }

    private void resetChapter() {
        btnChapter.setText("Select Chapter");
        btnChapter.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
