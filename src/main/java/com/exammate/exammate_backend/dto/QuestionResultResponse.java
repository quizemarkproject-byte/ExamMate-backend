package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionResultResponse {
    private String text;
    private List<String> options;
    private String correctAnswer;
    private boolean isCorrect;

    public QuestionResultResponse(String text, List<String> options, String correctAnswer, boolean isCorrect) {
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
    }
}
