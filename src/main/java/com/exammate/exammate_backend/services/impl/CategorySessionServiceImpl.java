package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.CategoryResultResponse;
import com.exammate.exammate_backend.dto.CategorySessionStartRequest;
import com.exammate.exammate_backend.dto.CategorySessionStartResponse;
import com.exammate.exammate_backend.dto.CategorySessionSubmissionRequest;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.exception.ApiException;
import com.exammate.exammate_backend.exception.BadRequestException;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.models.Category;
import com.exammate.exammate_backend.models.CategoryResult;
import com.exammate.exammate_backend.models.CategorySession;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.repositories.CategoryRepository;
import com.exammate.exammate_backend.repositories.CategoryResultRepository;
import com.exammate.exammate_backend.repositories.CategorySessionRepository;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import com.exammate.exammate_backend.services.CategorySessionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategorySessionServiceImpl implements CategorySessionService {
    private final CategoryRepository categoryRepository;
    private final CategorySessionRepository sessionRepository;
    private final CategoryResultRepository resultRepository;
    private final QuestionRepository questionRepository;
    private final ModelMapper modelMapper;

    @Override
    public CategorySessionStartResponse startSession(CategorySessionStartRequest request) {
        Optional<Category> categoryOpt = categoryRepository.findById(request.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new ApiException("Category not found");
        }
        Category category = categoryOpt.get();

        CategorySession inProgress = sessionRepository.findFirstByUserIdAndCategory_IdAndExpiredFalse(request.getUserId(), category.getId());
        if (inProgress != null) {
            return mapSessionToStartResponse(inProgress);
        }

        List<Question> allQuestions = questionRepository.findByCategories_Id(request.getCategoryId()).orElse(List.of());
        Collections.shuffle(allQuestions);
        int limit = Math.min(category.getQuestionLimit(), allQuestions.size());
        List<Question> limitedQuestions = allQuestions.subList(0, limit);

        CategorySession session = CategorySession.builder()
            .category(category)
            .userId(request.getUserId())
            .expired(false)
            .questions(limitedQuestions)
            .build();
        session = sessionRepository.save(session);
        return mapSessionToStartResponse(session);
    }

    private CategorySessionStartResponse mapSessionToStartResponse(CategorySession session) {
        List<Question> questions = session.getQuestions();
        Category category = session.getCategory();
        long totalTimeInSeconds = category.getTimeLimit() != null ? category.getTimeLimit().getSeconds() : 0L;
        long remainingSeconds = totalTimeInSeconds;
        if (session.getStartedAt() != null && category.getTimeLimit() != null) {
            long elapsed = Duration.between(session.getStartedAt(), Instant.now()).getSeconds();
            remainingSeconds = Math.max(0, totalTimeInSeconds - elapsed);
        }
        return CategorySessionStartResponse.builder()
            .sessionId(session.getId())
            .questions(questions.stream()
                .map(q -> modelMapper.map(q, QuestionResponse.class))
                .toList())
            .totalTimeInSeconds(totalTimeInSeconds)
            .remainingSeconds(remainingSeconds)
            .build();
    }

    @Override
    public CategoryResultResponse submitSession(CategorySessionSubmissionRequest request) {
        CategorySession session = sessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new NotFoundException("Session not found"));
        if (session.isExpired()) {
            throw new BadRequestException("This session has already been submitted.");
        }
        List<Question> questions = session.getQuestions();
        int score = 0;
        for (CategorySessionSubmissionRequest.AnswerSubmission ans : request.getAnswers()) {
            for (Question q : questions) {
                if (q.getId().equals(ans.getQuestionId()) && q.getCorrectAnswer().equals(ans.getAnswer())) {
                    score++;
                }
            }
        }
        CategoryResult result = new CategoryResult();
        result.setCategorySession(session);
        result.setUserId(request.getUserId());
        result.setScore(score);
        result.setTotalQuestions(questions.size());
        resultRepository.save(result);
        session.setExpired(true);
        sessionRepository.save(session);
        return CategoryResultResponse.builder()
            .id(result.getId())
            .userId(result.getUserId())
            .score(result.getScore())
            .totalQuestions(result.getTotalQuestions())
            .build();
    }

    @Override
    public List<CategoryResultResponse> getAllResultsForUser(String userId) {
        List<CategoryResult> categoryResults = resultRepository.findByUserId(userId);
        if (categoryResults.isEmpty()) {
        return Collections.emptyList();
    }
        return categoryResults.stream()
            .map(result -> CategoryResultResponse.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .score(result.getScore())
                .totalQuestions(result.getTotalQuestions())
                .questions(result.getCategorySession().getQuestions())
                .build())
            .toList();
    }

    @Override
    public CategoryResultResponse getResultById(UUID resultId) {
        CategoryResult result = resultRepository.findById(resultId).orElse(null);
        
        return CategoryResultResponse.builder()
            .id(result.getId())
                .userId(result.getUserId())
                .score(result.getScore())
                .totalQuestions(result.getTotalQuestions())
                .questions(result.getCategorySession().getQuestions())
        .build();
    }
}
