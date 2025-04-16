package com.saa.quizapplication.login_signup;

public class SignUpHelper {
    public String fullname, email, username, userType;

    // Default constructor required for Firebase
    public SignUpHelper() {
    }

    // Parameterized constructor
    public SignUpHelper(String fullname, String email, String username, String userType) {
        this.fullname = fullname;
        this.email = email;
        this.username = username;
        this.userType = userType;
    }

    // Getter and Setter methods (optional)
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
