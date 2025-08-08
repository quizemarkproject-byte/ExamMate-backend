package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
public class QuizResponse {
    private String id;
    private String title;
    private Duration timeLimit;
    private List<QuestionResponse> questions;
}
