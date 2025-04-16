package com.saa.quizapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.saa.quizapplication.AddNewDepartmentFromAdmin;
import com.saa.quizapplication.ChangeRole;
import com.saa.quizapplication.R;
import com.saa.quizapplication.ScoreAnalysisActivity;
import com.saa.quizapplication.SelectDetailsActivity;
import com.saa.quizapplication.UserDisplay;

public class AdminDashBoard extends AppCompatActivity {

    MaterialCardView createquiz, changerole, adddepartment, userlist, showAnalysissbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dash_board);

        createquiz = findViewById(R.id.createQuiz);
        changerole = findViewById(R.id.changeRole);
        adddepartment = findViewById(R.id.addDepartment);
        userlist = findViewById(R.id.userList);
        showAnalysissbtn = findViewById(R.id.showanalysis);

        createquiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent redirectToCreateQuiz = new Intent(AdminDashBoard.this, SelectDetailsActivity.class);
                startActivity(redirectToCreateQuiz);
            }
        });

        changerole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent redirectTochangerole = new Intent(AdminDashBoard.this, ChangeRole.class);
                startActivity(redirectTochangerole);
            }
        });

        adddepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent redirectToAddnewDepartmentFromAdminSide = new Intent(AdminDashBoard.this, AddNewDepartmentFromAdmin.class);
                startActivity(redirectToAddnewDepartmentFromAdminSide);
            }
        });

        userlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent redirectToUserDisplay = new Intent(AdminDashBoard.this, UserDisplay.class);
                startActivity(redirectToUserDisplay);
            }
        });

        showAnalysissbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent redirecttoScoreAnalysis = new Intent(AdminDashBoard.this, ScoreAnalysisActivity.class);
                startActivity(redirecttoScoreAnalysis);
            }
        });
    }
}
