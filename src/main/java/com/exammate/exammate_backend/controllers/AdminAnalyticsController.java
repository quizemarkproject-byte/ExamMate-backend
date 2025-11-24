package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.AnalyticsResponse;
import com.exammate.exammate_backend.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/quizzes/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get quiz analytics (JSON)")
    public AnalyticsResponse getQuizAnalytics(@PathVariable UUID quizId) {
        return analyticsService.quizAnalytics(quizId);
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get user analytics (JSON)")
    public AnalyticsResponse getUserAnalytics(@PathVariable UUID userId) {
        return analyticsService.userAnalytics(userId);
    }
}

