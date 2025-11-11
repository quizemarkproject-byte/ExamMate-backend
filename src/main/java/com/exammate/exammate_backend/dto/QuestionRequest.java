package com.exammate.exammate_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    /** If present, attach this existing question (by id) to the quiz instead of creating a new one. */
    private UUID id;

    // When creating a new question these fields are required; service layer will validate accordingly
    private String text;

    private List<String> options;

    private String correctAnswer;
}
