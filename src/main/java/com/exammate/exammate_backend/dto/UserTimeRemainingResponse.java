package com.exammate.exammate_backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
public class UserTimeRemainingResponse {
    private UUID quizSessionId;
    private long remainingSeconds;
    private long totalTimeSeconds;
}
