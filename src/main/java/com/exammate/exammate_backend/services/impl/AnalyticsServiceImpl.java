package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.AnalyticsResponse;
import com.exammate.exammate_backend.dto.QuestionStat;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.models.QuizResult;
import com.exammate.exammate_backend.models.QuizAnswer;
import com.exammate.exammate_backend.models.Question;
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

        // distribution buckets 0-9, 10-19, 20-29, ..., 90-100 (non-overlapping)
        // Uses [low, high) for most buckets and [90, 100] for the last bucket
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            int low = i * 10;
            int high = low + 9;
            final double lowF = low;
            final double highF = (i + 1) * 10.0; // exclusive upper bound
            final boolean isLastBucket = (i == 9);
            
            String key;
            long count;
            if (isLastBucket) {
                // Last bucket "90-100": includes scores from 90.0 to 100.0 (inclusive)
                key = low + "-100";
                count = percents.stream().filter(p -> p >= lowF && p <= 100.0).count();
            } else {
                // Other buckets "0-9", "10-19", etc.: includes scores >= low and < high
                // e.g., "0-9" includes 0.0 to 9.999..., "10-19" includes 10.0 to 19.999...
                key = low + "-" + high;
                count = percents.stream().filter(p -> p >= lowF && p < highF).count();
            }
            distribution.put(key, count);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        Map<String, Long> byDay = allResults.stream()
                .collect(Collectors.groupingBy(r -> r.getCompletedAt().toLocalDate().format(fmt), Collectors.counting()));

        // Build per-question stats
        Map<UUID, QuestionAggregate> map = new HashMap<>();
        for (QuizResult result : allResults) {
            if (result.getAnswers() == null) continue;
            for (QuizAnswer ans : result.getAnswers()) {
                if (ans.getQuestion() == null) continue;
                UUID qid = ans.getQuestion().getId();
                QuestionAggregate agg = map.get(qid);
                if (agg == null) {
                    String text = ans.getQuestion().getText();
                    agg = new QuestionAggregate(qid, text);
                    map.put(qid, agg);
                }
                agg.total++;
                if (ans.isCorrect()) agg.correct++;
            }
        }

        List<QuestionStat> questionStats = map.values().stream()
                .map(a -> QuestionStat.builder()
                        .id(a.id)
                        .text(a.text)
                        .pctCorrect(round2(a.total == 0 ? 0.0 : ((double) a.correct) / a.total))
                        .build())
                .sorted(Comparator.comparingDouble(QuestionStat::getPctCorrect)) // hardest first
                .toList();

        return AnalyticsResponse.builder()
                .totalAttempts(total)
                .averageScore(round2(avg))
                .medianScore(round2(median))
                .scoreDistribution(distribution)
                .attemptsByDay(byDay)
                .questionStats(questionStats)
                .build();
    }

    private static final class QuestionAggregate {
        UUID id;
        String text;
        long total = 0;
        long correct = 0;

        QuestionAggregate(UUID id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
