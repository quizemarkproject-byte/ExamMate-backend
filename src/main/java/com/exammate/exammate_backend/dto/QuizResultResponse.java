package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class QuizResultResponse {
    private List<QuestionResultResponse> questionResultResponse;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;

    public QuizResultResponse(List<QuestionResultResponse> questionResultResponse, int totalQuestions, int correctAnswers, double scorePercentage) {
        this.questionResultResponse = questionResultResponse;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.scorePercentage = scorePercentage;
    }
}
