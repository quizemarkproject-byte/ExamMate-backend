package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserQuizSubmission {
    private String userId;
    private List<QuestionAnswerSubmission> answerSubmissions;
}
