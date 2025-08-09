package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuestionAnswerSubmission {
    private UUID questionId;
    private String selectedAnswer;
}
