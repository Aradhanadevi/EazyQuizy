package com.saa.quizapplication.login_signup;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.saa.quizapplication.R;
import com.saa.quizapplication.Welcome;
import com.saa.quizapplication.admin.AdminDashBoard;

public class SignIn extends AppCompatActivity {
    TextView SignUpRedirect;
    Button loginbtn;
    EditText username, password;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // ✅ Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // ✅ Fetch FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM_TOKEN", "Token: " + token);
                    // You can store this token in DB if needed
                });

        // Initialize UI elements
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        SignUpRedirect = findViewById(R.id.signupredirecttxt);
        loginbtn = findViewById(R.id.loginbtn);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");

        // Redirect to SignUp activity
        SignUpRedirect.setOnClickListener(view -> {
            Intent signUpRedirect = new Intent(SignIn.this, SignUp.class);
            startActivity(signUpRedirect);
        });

        // Handle login
        loginbtn.setOnClickListener(v -> {
            String Username = username.getText().toString().trim();
            String Password = password.getText().toString().trim();

            if (Username.isEmpty() || Password.isEmpty()) {
                Toast.makeText(SignIn.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseReference.orderByChild("username").equalTo(Username)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            if (datasnapshot.exists()) {
                                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                                    String storedEmail = snapshot.child("email").getValue(String.class);
                                    String userType = snapshot.child("userType").getValue(String.class);

                                    if (storedEmail != null) {
                                        auth.signInWithEmailAndPassword(storedEmail, Password)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        FirebaseUser user = auth.getCurrentUser();

                                                        if (user != null) {
                                                            SharedPreferences sharedPreferences = getSharedPreferences("QuizAppPrefs", MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.putString("username", Username);
                                                            editor.apply();

                                                            // Save FCM token to database for current user
                                                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                                                                if (tokenTask.isSuccessful()) {
                                                                    String token = tokenTask.getResult();
                                                                    database.getReference("users").child(user.getUid()).child("fcmToken").setValue(token);
                                                                }
                                                            });





                                                            Toast.makeText(SignIn.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                            Log.d("SignInActivity", "User logged in successfully");

                                                            Intent intent = "admin".equalsIgnoreCase(userType) ?
                                                                    new Intent(SignIn.this, AdminDashBoard.class) :
                                                                    new Intent(SignIn.this, Welcome.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }

                                                    }else {
                                                        Toast.makeText(SignIn.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                        return;
                                    }
                                }
                            } else {
                                Toast.makeText(SignIn.this, "No account exists with this username", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SignIn.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISSION", "Notification permission granted");
            } else {
                Log.d("PERMISSION", "Notification permission denied");
            }
        }
    }
}
