package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.QuestionAnswerSubmission;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.dto.QuestionResultResponse;
import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.StartQuizResponse;
import com.exammate.exammate_backend.dto.UserQuizSubmissionRequest;
import com.exammate.exammate_backend.dto.UserTimeRemainingResponse;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.models.QuizResult;
import com.exammate.exammate_backend.models.QuizSession;
import com.exammate.exammate_backend.repositories.QuizRepository;
import com.exammate.exammate_backend.repositories.QuizResultRepository;
import com.exammate.exammate_backend.repositories.QuizSessionRepository;
import com.exammate.exammate_backend.services.QuizService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final ModelMapper modelMapper;
    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizSessionRepository quizSessionRepository;

    @Override
    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(quiz -> modelMapper.map(quiz, QuizResponse.class))
                .toList();
    }

    @Override
    public QuizResponse getQuizById(UUID quizId) {
        Quiz quiz = getQuizOrThrow(quizId);
        return modelMapper.map(quiz, QuizResponse.class);
    }

    @Override
    public QuizResultResponse submitQuiz(UUID quizId, UserQuizSubmissionRequest submission) {
        Quiz quiz = getQuizOrThrow(quizId);

        final int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = 0;

        Map<UUID, String> submittedAnswers = submission.getAnswerSubmissions().stream()
                .collect(Collectors.toMap(
                        QuestionAnswerSubmission::getQuestionId,
                        QuestionAnswerSubmission::getSelectedAnswer
                ));

        List<QuestionResultResponse> questionResults = new ArrayList<>();
        for (Question question : quiz.getQuestions()) {
            String chosenAnswer = submittedAnswers.getOrDefault(question.getId(), null);
            boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(chosenAnswer);
            if (isCorrect) {
                correctAnswers++;
            }

            questionResults.add(QuestionResultResponse.builder()
                    .options(question.getOptions())
                    .correctAnswer(question.getCorrectAnswer())
                    .isCorrect(isCorrect)
                    .build());
        }

        QuizSession quizSession = quizSessionRepository.findByIdAndQuizIdAndUserId(submission.getQuizSessionId(), quizId, submission.getUserId())
                .orElseThrow();
        quizSession.setExpired(true);

        QuizResult quizResult = QuizResult.builder()
                .quizId(quizId)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .build();

        quizSessionRepository.save(quizSession);
        quizResultRepository.save(quizResult);

        return QuizResultResponse.builder()
                .questionResultResponse(questionResults)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .scorePercentage(quizResult.getScorePercentage())
                .build();
    }

    @Override
    public StartQuizResponse startQuiz(UUID quizId, String userId) {
        Quiz quiz = getQuizOrThrow(quizId);
        long totalTimeSeconds = quiz.getTimeLimit().getSeconds();

        Optional<QuizSession> existingSessionOpt =
                quizSessionRepository.findByQuizIdAndUserIdAndExpired(quizId, userId, false);

        QuizSession quizSession;
        if (existingSessionOpt.isPresent()) {
            quizSession = existingSessionOpt.get();
        } else {
            quizSession = QuizSession.builder()
                    .quizId(quizId)
                    .userId(userId)
                    .totalTimeSeconds(totalTimeSeconds)
                    .build();
            quizSessionRepository.save(quizSession);
        }

        long elapsedSeconds = Duration.between(quizSession.getStartTime(), Instant.now()).getSeconds();
        long remainingSeconds = Math.max(totalTimeSeconds - elapsedSeconds, 0);

        UserTimeRemainingResponse quizTime = UserTimeRemainingResponse.builder()
                .quizSessionId(quizSession.getId())
                .remainingSeconds(remainingSeconds)
                .totalTimeSeconds(totalTimeSeconds)
                .build();

        return StartQuizResponse.builder()
                .id(quizId)
                .title(quiz.getTitle())
                .timeLimit(quiz.getTimeLimit())
                .questions(quiz.getQuestions().stream()
                        .map(q -> modelMapper.map(q, QuestionResponse.class))
                        .toList())
                .timeRemaining(quizTime)
                .build();
    }

    @Override
    public UserTimeRemainingResponse getRemainingTime(UUID quizId, String userId, UUID quizSessionId) {
        Quiz quiz = getQuizOrThrow(quizId);
        long totalTimeSeconds = quiz.getTimeLimit().getSeconds();

        QuizSession quizSession = quizSessionRepository.findByIdAndQuizIdAndUserIdAndExpired(quizSessionId, quizId, userId, false).orElseThrow();
        long elapsedSeconds = Duration.between(quizSession.getStartTime(), Instant.now()).getSeconds();
        long remainingSeconds = Math.max(totalTimeSeconds - elapsedSeconds, 0);

        return UserTimeRemainingResponse.builder()
                .quizSessionId(quizSession.getId())
                .remainingSeconds(remainingSeconds)
                .totalTimeSeconds(totalTimeSeconds)
                .build();
    }

    private Quiz getQuizOrThrow(UUID quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
    }
}
