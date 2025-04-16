package com.saa.quizapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ImageView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        // 🔹 VideoView setup
        videoView = findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bgvideo);
        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        // 🔹 Set Welcome Message
        SharedPreferences sharedPreferences = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Guest");
        TextView welcomeText = findViewById(R.id.WelcomeText);
//        welcomeText.setText("Welcome, " + username);

        if (username.length() >6) {
            welcomeText.setText("Hi, " + username);
        } else {
            welcomeText.setText("Welcome, " + username);
        }

        // 🔹 Profile Image Click
        ImageView profileImage = findViewById(R.id.profileImageView);
        profileImage.setOnClickListener(view -> {
            Log.d("WelcomeActivity", "Opening profile for username: " + username);
            startActivity(new Intent(this, Profile.class));
        });



        Button topScorersBtn = findViewById(R.id.btnTopScorers);
        topScorersBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, TopScorersActivity.class));
        });

        // 🔹 Start Quiz Button
        Button startBtn = findViewById(R.id.btnstartquiz);
        startBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, activity_select_department.class));
        });
    }
    @Override
    protected void onPostResume() {
        videoView.resume();
        super.onPostResume();
    }

    @Override
    protected void onRestart() {
        videoView.start();
        super.onRestart();

    }

    @Override
    protected void onDestroy() {
        videoView.stopPlayback();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        videoView.suspend();
        super.onPause();
    }
}
