package com.saa.quizapplication.model;

import java.util.List;

public class Question {
    private String question;
    private List<String> options; // Ensure this matches your Firebase structure
    private String correctOption;
    private String explanation;

    public Question() {
        // Default constructor required for Firebase
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public String getExplanation() {
        return explanation;
    }
}
