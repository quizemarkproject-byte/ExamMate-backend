package com.exammate.exammate_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {
    @NotBlank
    private String name;

    /** time limit in minutes; null or 0 means no limit */
    @NotNull
    @Min(5)
    private Long timeLimitMinutes;

    @NotNull
    @Min(2)
    private Integer questionLimit;
}

