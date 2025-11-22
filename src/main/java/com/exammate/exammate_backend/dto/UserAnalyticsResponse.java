package com.exammate.exammate_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnalyticsResponse {
    private long totalAttempts;
    private double averageScore; // percent
    private double medianScore; // percent
    private Map<String, Long> scoreDistribution; // e.g. "0-10": 3
    private Map<String, Long> attemptsByDay; // yyyy-MM-dd -> count
}

