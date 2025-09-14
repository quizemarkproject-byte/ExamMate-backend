package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.dto.QuestionResultResponse;
import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.ResultResponse;
import com.exammate.exammate_backend.dto.UserQuizSubmissionRequest;
import com.exammate.exammate_backend.dto.UserTimeRemainingResponse;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.models.QuizResult;
import com.exammate.exammate_backend.models.QuizSession;
import com.exammate.exammate_backend.models.SubmittedAnswer;
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
import java.util.Collections;
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

        QuizSession quizSession = findAndExpireSession(
                submission.getQuizSessionId(), quizId, submission.getUserId()
        );

        QuizResult quizResult = buildQuizResult(quiz, submission);

        quizResult.setQuizSession(quizSession);
        quizSession.setResult(quizResult);

        quizResultRepository.save(quizResult);

        return mapToResultResponse(quizResult, false);
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
            List<Question> quizQuestions = new ArrayList<>(quiz.getQuestions());
            Collections.shuffle(quizQuestions);

            int limit = Math.min(quiz.getQuestionLimit(), quizQuestions.size());
            List<Question> selectedQuestions = quizQuestions.subList(0, limit);

            selectedQuestions.forEach(q -> {
                List<String> shuffledOptions = new ArrayList<>(q.getOptions());
                Collections.shuffle(shuffledOptions);
                q.setOptions(shuffledOptions);
            });

            quizSession = QuizSession.builder()
                    .quiz(quiz)
                    .userId(userId)
                    .totalTimeSeconds(totalTimeSeconds)
                    .questions(selectedQuestions)
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
        response.setQuestions(
                quizSession.getQuestions()
                        .stream()
                        .map(q -> modelMapper.map(q, QuestionResponse.class))
                        .toList()
        );
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
    public ResultResponse getUserQuizResult(UUID resultId, String userId) {
        QuizResult quizResult = quizResultRepository.findByIdAndUserId(resultId, userId)
                .orElseThrow();
        return mapToResultResponse(quizResult, true);
    }

    @Override
    public List<ResultResponse> getAllUserQuizResults(String userId) {
        return quizResultRepository.findAllByUserId(userId).stream()
                .map(result -> mapToResultResponse(result, false))
                .toList();
    }

    private Quiz getQuizOrThrow(UUID quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
    }

    private QuizResult buildQuizResult(Quiz quiz, UserQuizSubmissionRequest submission) {
        Map<UUID, Question> questionMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        QuizResult quizResult = QuizResult.builder()
                .userId(submission.getUserId())
                .quiz(quiz)
                .build();

        int correctAnswers = 0;
        List<SubmittedAnswer> submittedAnswers = new ArrayList<>();

        for (var s : submission.getAnswerSubmissions()) {
            Question question = questionMap.get(s.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException("Invalid question ID: " + s.getQuestionId());
            }

            if (s.getSelectedAnswer() != null &&
                    question.getCorrectAnswer().equalsIgnoreCase(s.getSelectedAnswer())) {
                correctAnswers++;
            }

            submittedAnswers.add(SubmittedAnswer.builder()
                    .quizResult(quizResult)
                    .question(question)
                    .answer(s.getSelectedAnswer())
                    .build());
        }

        double scorePercentage = ((double) correctAnswers / quiz.getQuestions().size()) * 100;

        quizResult.setTotalQuestions(quiz.getQuestions().size());
        quizResult.setCorrectAnswers(correctAnswers);
        quizResult.setScorePercentage(scorePercentage);
        quizResult.setSubmittedAnswers(submittedAnswers);

        return quizResult;
    }

    private QuizSession findAndExpireSession(UUID sessionId, UUID quizId, String userId) {
        QuizSession session = quizSessionRepository
                .findByIdAndQuizIdAndUserId(sessionId, quizId, userId)
                .orElseThrow();
        session.setExpired(true);
        return session;
    }

    private ResultResponse mapToResultResponse(QuizResult quizResult, boolean showResultResponse) {
        List<QuestionResultResponse> questionResultResponse = Collections.emptyList();

        if (showResultResponse) {
            Map<UUID, SubmittedAnswer> submittedMap = quizResult.getSubmittedAnswers().stream()
                    .collect(Collectors.toMap(sa -> sa.getQuestion().getId(), sa -> sa));

            questionResultResponse = quizResult.getQuizSession().getQuestions().stream()
                    .map(question -> {
                        SubmittedAnswer submitted = submittedMap.get(question.getId());
                        String chosenAnswer = submitted != null ? submitted.getAnswer() : null;

                        return QuestionResultResponse.builder()
                                .text(question.getText())
                                .correctAnswer(question.getCorrectAnswer())
                                .options(question.getOptions())
                                .chosenAnswer(chosenAnswer)
                                .isCorrect(chosenAnswer != null && chosenAnswer.equals(question.getCorrectAnswer()))
                                .build();
                    })
                    .toList();
        }

        return ResultResponse.builder()
                .id(quizResult.getId())
                .quizTitle(quizResult.getQuiz().getTitle())
                .totalQuestions(quizResult.getTotalQuestions())
                .questionResultResponse(questionResultResponse)
                .correctAnswers(quizResult.getCorrectAnswers())
                .scorePercentage(quizResult.getScorePercentage())
                .build();
    }

}
