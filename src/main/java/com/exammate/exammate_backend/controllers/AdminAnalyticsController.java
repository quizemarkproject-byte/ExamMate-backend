package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.QuizAnalyticsResponse;
import com.exammate.exammate_backend.dto.UserAnalyticsResponse;
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
    public QuizAnalyticsResponse getQuizAnalytics(@PathVariable UUID quizId) {
        return analyticsService.quizAnalytics(quizId);
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get user analytics (JSON)")
    public UserAnalyticsResponse getUserAnalytics(@PathVariable String userId) {
        return analyticsService.userAnalytics(userId);
    }
}

