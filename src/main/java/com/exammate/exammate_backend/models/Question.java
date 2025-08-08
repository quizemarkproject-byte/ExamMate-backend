package com.exammate.exammate_backend.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Question {
    private String id;
    private String text;
    private List<String> options;
    private String correctAnswer;

    public Question(String id, String text, List<String> options, String correctAnswer) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
}
