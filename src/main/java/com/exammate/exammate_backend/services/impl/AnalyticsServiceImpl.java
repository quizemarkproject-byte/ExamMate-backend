package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.AnalyticsResponse;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.models.QuizResult;
import com.exammate.exammate_backend.repositories.QuizRepository;
import com.exammate.exammate_backend.repositories.QuizResultRepository;
import com.exammate.exammate_backend.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository resultRepository;

    @Override
    public AnalyticsResponse quizAnalytics(UUID quizId) {
        // verify quiz exists
        quizRepository.findById(quizId).orElseThrow(() -> new NotFoundException("Quiz not found"));

        // load results for quiz by traversing results that belong to quiz sessions
        List<QuizResult> allResults = resultRepository.findAll().stream()
                .filter(r -> r.getQuizSession() != null && r.getQuizSession().getQuiz() != null && r.getQuizSession().getQuiz().getId().equals(quizId))
                .toList();

        return buildQuizAnalytics(allResults);
    }

    @Override
    public AnalyticsResponse userAnalytics(UUID userId) {
        // find results by user id
        List<QuizResult> allResults = resultRepository.findByUserId(userId);
        if (allResults == null) allResults = List.of();

        // build same stats
        return buildQuizAnalytics(allResults);
    }

    @Override
    public AnalyticsResponse userAnalyticsByQuiz(UUID userId, UUID quizId) {
        // verify quiz exists
        quizRepository.findById(quizId).orElseThrow(() -> new NotFoundException("Quiz not found"));

        // find results by user id and filter by quiz id through quiz session
        List<QuizResult> allResults = resultRepository.findByUserId(userId).stream()
                .filter(r -> r.getQuizSession() != null 
                        && r.getQuizSession().getQuiz() != null 
                        && r.getQuizSession().getQuiz().getId().equals(quizId))
                .toList();

        return buildQuizAnalytics(allResults);
    }

    private AnalyticsResponse buildQuizAnalytics(List<QuizResult> allResults) {
        long total = allResults.size();
        List<Double> percents = allResults.stream()
                .map(r -> {
                    if (r.getTotalQuestions() <= 0) return 0.0;
                    return (r.getScore() * 100.0) / r.getTotalQuestions();
                })
                .sorted()
                .toList();

        double avg = percents.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double median = 0.0;
        if (!percents.isEmpty()) {
            int m = percents.size() / 2;
            median = (percents.size() % 2 == 1) ? percents.get(m) : (percents.get(m - 1) + percents.get(m)) / 2.0;
        }

        // distribution buckets 0-10,10-20,...90-100
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            int low = i * 10;
            int high = (i + 1) * 10;
            String key = low + "-" + high;
            final int lowF = low, highF = high;
            long count = percents.stream().filter(p -> p >= lowF && p <= highF).count();
            distribution.put(key, count);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        Map<String, Long> byDay = allResults.stream()
                .collect(Collectors.groupingBy(r -> r.getCompletedAt().toLocalDate().format(fmt), Collectors.counting()));

        return AnalyticsResponse.builder()
                .totalAttempts(total)
                .averageScore(round2(avg))
                .medianScore(round2(median))
                .scoreDistribution(distribution)
                .attemptsByDay(byDay)
                .build();
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
