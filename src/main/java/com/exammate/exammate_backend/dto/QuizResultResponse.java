package com.exammate.exammate_backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class QuizResultResponse {
    private List<QuestionResultResponse> questionResultResponse;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;

}
