package com.exammate.exammate_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private UUID id;
    private String title;
    private Duration timeLimit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<QuestionResponse> questions;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserTimeRemainingResponse timeRemaining;
}
