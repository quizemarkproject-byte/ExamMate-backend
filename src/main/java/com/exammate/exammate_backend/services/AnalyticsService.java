package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.QuizAnalyticsResponse;
import com.exammate.exammate_backend.dto.UserAnalyticsResponse;

import java.util.UUID;

public interface AnalyticsService {
    QuizAnalyticsResponse quizAnalytics(UUID quizId);
    UserAnalyticsResponse userAnalytics(String userId);
}
