package com.exammate.exammate_backend.dto;

import com.exammate.exammate_backend.validation.CorrectAnswerInOptions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@CorrectAnswerInOptions
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    private UUID id;
    @NotBlank
    private String text;
    @NotEmpty(message = "Options list cannot be empty")
    @Size(min = 2, message = "There must be at least two options")
    private List<@NotBlank(message = "Option cannot be blank") String> options;
    @NotBlank
    private String correctAnswer;
}
