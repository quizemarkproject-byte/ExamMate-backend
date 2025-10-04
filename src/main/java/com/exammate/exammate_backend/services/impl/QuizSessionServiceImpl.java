package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.QuizSessionStartRequest;
import com.exammate.exammate_backend.dto.QuizSessionStartResponse;
import com.exammate.exammate_backend.dto.QuizSessionSubmissionRequest;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.exception.ApiException;
import com.exammate.exammate_backend.exception.BadRequestException;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.models.QuizResult;
import com.exammate.exammate_backend.models.QuizSession;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.repositories.QuizRepository;
import com.exammate.exammate_backend.repositories.QuizResultRepository;
import com.exammate.exammate_backend.repositories.QuizSessionRepository;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import com.exammate.exammate_backend.services.QuizSessionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizSessionServiceImpl implements QuizSessionService {
    private final QuizRepository quizRepository;
    private final QuizSessionRepository sessionRepository;
    private final QuizResultRepository resultRepository;
    private final QuestionRepository questionRepository;
    private final ModelMapper modelMapper;

    @Override
    public QuizSessionStartResponse startSession(QuizSessionStartRequest request) {
        Optional<Quiz> quizOpt = quizRepository.findById(request.getQuizId());
        if (quizOpt.isEmpty()) {
            throw new ApiException("Quiz not found");
        }
        Quiz quiz = quizOpt.get();

        QuizSession inProgress = sessionRepository.findFirstByUserIdAndQuiz_IdAndExpiredFalse(request.getUserId(), quiz.getId());
        if (inProgress != null) {
            return mapSessionToStartResponse(inProgress);
        }

        List<Question> allQuestions = questionRepository.findByCategories_Id(request.getQuizId()).orElse(List.of());
        Collections.shuffle(allQuestions);
        int limit = Math.min(quiz.getQuestionLimit(), allQuestions.size());
        List<Question> limitedQuestions = allQuestions.subList(0, limit);

        QuizSession session = QuizSession.builder()
            .quiz(quiz)
            .userId(request.getUserId())
            .expired(false)
            .questions(limitedQuestions)
            .build();
        session = sessionRepository.save(session);
        return mapSessionToStartResponse(session);
    }

    private QuizSessionStartResponse mapSessionToStartResponse(QuizSession session) {
        List<Question> questions = session.getQuestions();
        Quiz quiz = session.getQuiz();
        long totalTimeInSeconds = quiz.getTimeLimit() != null ? quiz.getTimeLimit().getSeconds() : 0L;
        long remainingSeconds = totalTimeInSeconds;
        if (session.getStartedAt() != null && quiz.getTimeLimit() != null) {
            long elapsed = Duration.between(session.getStartedAt(), Instant.now()).getSeconds();
            remainingSeconds = Math.max(0, totalTimeInSeconds - elapsed);
        }
        return QuizSessionStartResponse.builder()
            .sessionId(session.getId())
            .questions(questions.stream()
                .map(q -> modelMapper.map(q, QuestionResponse.class))
                .toList())
            .totalTimeInSeconds(totalTimeInSeconds)
            .remainingSeconds(remainingSeconds)
            .build();
    }

    @Override
    public QuizResultResponse submitSession(QuizSessionSubmissionRequest request) {
        QuizSession session = sessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new NotFoundException("Session not found"));
        if (session.isExpired()) {
            throw new BadRequestException("This session has already been submitted.");
        }
        List<Question> questions = session.getQuestions();
        int score = 0;
        for (QuizSessionSubmissionRequest.AnswerSubmission ans : request.getAnswers()) {
            for (Question q : questions) {
                if (q.getId().equals(ans.getQuestionId()) && q.getCorrectAnswer().equals(ans.getAnswer())) {
                    score++;
                }
            }
        }
    QuizResult result = new QuizResult();
    result.setQuizSession(session);
        result.setUserId(request.getUserId());
        result.setScore(score);
        result.setTotalQuestions(questions.size());
        resultRepository.save(result);
        session.setExpired(true);
        sessionRepository.save(session);
        return QuizResultResponse.builder()
            .id(result.getId())
            .userId(result.getUserId())
            .score(result.getScore())
            .totalQuestions(result.getTotalQuestions())
            .build();
    }

    @Override
    public List<QuizResultResponse> getAllResultsForUser(String userId) {
    List<QuizResult> quizResults = resultRepository.findByUserId(userId);
        if (quizResults.isEmpty()) {
        return Collections.emptyList();
    }
        return quizResults.stream()
                .map(result -> QuizResultResponse.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .score(result.getScore())
                .totalQuestions(result.getTotalQuestions())
                    .questions(result.getQuizSession().getQuestions())
                .build())
            .toList();
    }

    @Override
    public QuizResultResponse getResultById(UUID resultId) {
        QuizResult result = resultRepository.findById(resultId).orElse(null);
        
        return QuizResultResponse.builder()
            .id(result.getId())
                .userId(result.getUserId())
                .score(result.getScore())
                .totalQuestions(result.getTotalQuestions())
                .questions(result.getQuizSession().getQuestions())
        .build();
    }
}
