package com.exammate.exammate_backend.dto;

import com.exammate.exammate_backend.models.Question;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CategoryResultResponse {
    private UUID id;
    private String userId;
    private int score;
    private int totalQuestions;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Question> questions;
}