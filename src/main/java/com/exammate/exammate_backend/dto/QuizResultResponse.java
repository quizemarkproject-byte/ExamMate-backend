package com.exammate.exammate_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class QuizResultResponse {
    private UUID id;
    private String userId;
    private int score;
    private int totalQuestions;
    private String quizTitle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Question> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Question {
    private UUID id;
    private String text;
    private List<String> options;
    private String chosenAnswer;
    private boolean isCorrect;
    }
}
