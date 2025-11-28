package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.AnalyticsResponse;

import java.util.UUID;

public interface AnalyticsService {
    AnalyticsResponse quizAnalytics(UUID quizId);
    AnalyticsResponse userAnalytics(UUID userId);
    AnalyticsResponse userAnalyticsByQuiz(UUID userId, UUID quizId);
}
