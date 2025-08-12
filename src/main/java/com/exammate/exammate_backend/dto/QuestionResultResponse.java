package com.exammate.exammate_backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class QuestionResultResponse {
    private String text;
    private List<String> options;
    private String correctAnswer;
    private String chosenAnswer;
    private boolean isCorrect;

}
