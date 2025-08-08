package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionAnswerSubmission {
    private String questionId;
    private String selectedAnswer;
}
