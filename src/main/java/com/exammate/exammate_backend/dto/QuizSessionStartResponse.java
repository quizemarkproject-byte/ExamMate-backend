package com.exammate.exammate_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class QuizSessionStartResponse {
    private UUID sessionId;
    private List<QuestionResponse> questions;
    private long totalTimeInSeconds;
    private long remainingSeconds;
}
