package com.exammate.exammate_backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class StartQuizResponse {
    private UUID id;
    private String title;
    private Duration timeLimit;
    private List<QuestionResponse> questions;
    private UserTimeRemainingResponse timeRemaining;
}
