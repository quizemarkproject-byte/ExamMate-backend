package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserQuizSubmissionRequest {
    private String userId;
    private UUID quizSessionId;
    private List<QuestionAnswerSubmission> answerSubmissions;
}
