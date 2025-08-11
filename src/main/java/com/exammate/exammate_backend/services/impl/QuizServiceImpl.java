package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.QuestionAnswerSubmission;
import com.exammate.exammate_backend.dto.QuestionResultResponse;
import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.ResultResponse;
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
                .peek(quiz -> quiz.setQuestions(null))
                .map(quiz -> modelMapper.map(quiz, QuizResponse.class))
                .toList();
    }

    @Override
    public QuizResponse getQuizById(UUID quizId) {
        Quiz quiz = getQuizOrThrow(quizId);
        quiz.setQuestions(null);
        return modelMapper.map(quiz, QuizResponse.class);
    }

    @Override
    public ResultResponse submitQuiz(UUID quizId, UserQuizSubmissionRequest submission) {
        Quiz quiz = getQuizOrThrow(quizId);
        final int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = 0;

        Map<UUID, String> submittedAnswers = submission.getAnswerSubmissions().stream()
                .collect(Collectors.toMap(
                        QuestionAnswerSubmission::getQuestionId,
                        QuestionAnswerSubmission::getSelectedAnswer
                ));

        for (Question question : quiz.getQuestions()) {
            String chosenAnswer = submittedAnswers.getOrDefault(question.getId(), null);
            if (question.getCorrectAnswer().equalsIgnoreCase(chosenAnswer)) {
                correctAnswers++;
            }
        }

        QuizSession quizSession = quizSessionRepository.findByIdAndQuizIdAndUserId(submission.getQuizSessionId(), quizId, submission.getUserId())
                .orElseThrow();
        quizSession.setExpired(true);

        double scorePercentage = ((double) correctAnswers / (double) totalQuestions) * 100;

        QuizResult quizResult = QuizResult.builder()
                .userId(submission.getUserId())
                .quizId(quizId)
                .submittedAnswers(submittedAnswers)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .scorePercentage(scorePercentage)
                .build();

        quizSessionRepository.save(quizSession);
        quizResultRepository.save(quizResult);

        return ResultResponse.builder()
                .id(quizResult.getId())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .scorePercentage(scorePercentage)
                .build();
    }

    @Override
    public QuizResponse startQuiz(UUID quizId, String userId) {
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

        QuizResponse response = modelMapper.map(quiz, QuizResponse.class);
        response.setTimeRemaining(quizTime);
        return response;
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

    @Override
    public ResultResponse getUserQuizResult(UUID quizId, UUID resultId, String userId) {
        Quiz quiz = getQuizOrThrow(quizId);
        QuizResult quizResult = quizResultRepository.findByIdAndQuizIdAndUserId(resultId, quizId, userId).orElseThrow();

        Map<UUID, String> submittedAnswers = quizResult.getSubmittedAnswers();

        List<QuestionResultResponse> questionResults = new ArrayList<>();
        for (Question question : quiz.getQuestions()) {
            String chosenAnswer = submittedAnswers.getOrDefault(question.getId(), null);

            questionResults.add(QuestionResultResponse.builder()
                    .text(question.getText())
                    .options(question.getOptions())
                    .correctAnswer(question.getCorrectAnswer())
                    .isCorrect(question.getCorrectAnswer().equalsIgnoreCase(chosenAnswer))
                    .build());
        }
        return ResultResponse.builder()
                .id(quizResult.getId())
                .questionResultResponse(questionResults)
                .totalQuestions(quizResult.getTotalQuestions())
                .correctAnswers(quizResult.getCorrectAnswers())
                .scorePercentage(quizResult.getScorePercentage())
                .build();
    }

    private Quiz getQuizOrThrow(UUID quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
    }
}
