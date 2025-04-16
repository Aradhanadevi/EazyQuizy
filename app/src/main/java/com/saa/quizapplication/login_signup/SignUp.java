package com.saa.quizapplication.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saa.quizapplication.R;

public class SignUp extends AppCompatActivity {
    TextView SignupRedirect;
    String userType;
    EditText fullname, email, username, password, confirmpassword;
    RadioGroup rg_user_type;
    RadioButton rb_student, rb_faculty;
    Button btn_signup;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize UI elements
        SignupRedirect = findViewById(R.id.signinredirecttxt);
        fullname = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmpassword = findViewById(R.id.confirmpassword);
        rg_user_type = findViewById(R.id.rg_user_type);
        rb_student = findViewById(R.id.rb_student);
        rb_faculty = findViewById(R.id.rb_faculty);
        btn_signup = findViewById(R.id.btn_signup);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");

        SignupRedirect.setOnClickListener(view -> {
            Intent SigninRedirect = new Intent(SignUp.this, SignIn.class);
            startActivity(SigninRedirect);
        });

        btn_signup.setOnClickListener(view -> {
            String Fullname = fullname.getText().toString().trim();
            String Email = email.getText().toString().trim();
            String Username = username.getText().toString().trim();
            String Password = password.getText().toString().trim();
            String ConfirmPassword = confirmpassword.getText().toString().trim();
            userType = "";

            if (Fullname.isEmpty() || Email.isEmpty() || Username.isEmpty() || Password.isEmpty() || ConfirmPassword.isEmpty()) {
                Toast.makeText(SignUp.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Password.equals(ConfirmPassword)) {
                Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rg_user_type.getCheckedRadioButtonId();
            if (selectedId == R.id.rb_student) {
                userType = "Student";
            } else if (selectedId == R.id.rb_faculty) {
                userType = "Faculty";
            } else {
                Toast.makeText(SignUp.this, "Please select a user type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register user with Firebase Authentication
            auth.createUserWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();

                                // Save additional user data in Firebase Database
                                SignUpHelper helperClass = new SignUpHelper(Fullname, Email, Username, userType);
                                databaseReference.child(Fullname).setValue(helperClass)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SignUp.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                            Log.d("SignUpActivity", "User data saved to Firebase");

                                            // Redirect to sign-in page
                                            Intent intent = new Intent(SignUp.this, SignIn.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SignUp.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(SignUp.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
