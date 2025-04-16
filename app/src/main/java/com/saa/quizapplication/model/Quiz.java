package com.saa.quizapplication.model;

public class Quiz {
    private String quizHeading;
    private String quizId; // Add this field for quiz ID

    public Quiz() {
        // Default constructor required for Firebase
    }

    public Quiz(String quizHeading) {
        this.quizHeading = quizHeading;
    }

    public String getQuizHeading() {
        return quizHeading;
    }

    public void setQuizHeading(String quizHeading) {
        this.quizHeading = quizHeading;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }
}
