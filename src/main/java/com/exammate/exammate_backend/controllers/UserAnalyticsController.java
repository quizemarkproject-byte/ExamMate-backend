package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.AnalyticsResponse;
import com.exammate.exammate_backend.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user/analytics")
@RequiredArgsConstructor
public class UserAnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get analytics for user's quiz attempts (JSON)", description = "Returns analytics for only quizzes the specified user has taken")
    public AnalyticsResponse getUserAnalytics(
            @Parameter(description = "User ID (UUID)", required = true) @PathVariable UUID userId) {
        return analyticsService.userAnalytics(userId);
    }

    @GetMapping("/{userId}/quiz/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get analytics for user's attempts on a specific quiz (JSON)", description = "Returns analytics for a specific user's attempts on a specific quiz")
    public AnalyticsResponse getUserAnalyticsByQuiz(
            @Parameter(description = "User ID (UUID)", required = true) @PathVariable UUID userId,
            @Parameter(description = "Quiz ID (UUID)", required = true) @PathVariable UUID quizId) {
        return analyticsService.userAnalyticsByQuiz(userId, quizId);
    }
}

