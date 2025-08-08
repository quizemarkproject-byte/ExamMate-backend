package com.exammate.exammate_backend.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResult {
    private String id;
    private String quizId;
    private String userId;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;

    public QuizResult(String quizId, int totalQuestions, int correctAnswers) {
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.scorePercentage = (totalQuestions == 0) ? 0 : (correctAnswers * 100.0 / totalQuestions);
    }
}
