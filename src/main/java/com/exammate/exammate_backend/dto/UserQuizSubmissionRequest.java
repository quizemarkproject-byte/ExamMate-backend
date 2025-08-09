package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserQuizSubmission {
    private String userId;
    private List<QuestionAnswerSubmission> answerSubmissions;
}
