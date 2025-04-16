package com.saa.quizapplication.model;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    private String subjectName;
    private List<String> chapters;

    // Empty constructor required for Firebase
    public Subject() {
        this.chapters = new ArrayList<>();  // Avoid NullPointerException
    }

    public Subject(String subjectName, List<String> chapters) {
        this.subjectName = subjectName;
        this.chapters = (chapters != null) ? chapters : new ArrayList<>();
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public List<String> getChapters() {
        return chapters;
    }

    public void setChapters(List<String> chapters) {
        this.chapters = (chapters != null) ? chapters : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Subject{" +
                "subjectName='" + subjectName + '\'' +
                ", chapters=" + chapters +
                '}';
    }
}
