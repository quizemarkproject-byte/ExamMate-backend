package com.exammate.exammate_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse {
    private UUID id;
    private String quizTitle;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<QuestionResultResponse> questionResultResponse;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;

}
