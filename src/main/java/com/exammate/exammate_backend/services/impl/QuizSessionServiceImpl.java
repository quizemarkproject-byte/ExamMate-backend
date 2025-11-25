package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.CountResponse;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.exammate.exammate_backend.models.QuizAnswer;

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
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ApiException("Quiz not found"));

        QuizSession inProgress = sessionRepository.findFirstByUserIdAndQuiz_IdAndExpiredFalse(request.getUserId(),
                quiz.getId());
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
        long elapsed = Duration.between(session.getStartedAt(), Instant.now()).getSeconds();
        long remainingSeconds = Math.max(0, totalTimeInSeconds - elapsed);
        return QuizSessionStartResponse.builder()
                .sessionId(session.getId())
                .quizTitle(quiz.getName())
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
                .orElseThrow(() -> new NotFoundException("Session has ended"));
        if (session.isExpired()) {
            throw new BadRequestException("This session has already been submitted.");
        }

        List<Question> questions = session.getQuestions();
        Map<UUID, Question> questionById = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        QuizResult result = new QuizResult();
        result.setQuizSession(session);
        result.setUserId(request.getUserId());
        result.setTotalQuestions(questions.size());

        int score = 0;
        if (request.getAnswers() != null) {
            for (QuizSessionSubmissionRequest.AnswerSubmission ans : request.getAnswers()) {
                Question q = questionById.get(ans.getQuestionId());
                if (q == null)
                    continue;

                boolean correct = q.getCorrectAnswer() != null && q.getCorrectAnswer().equals(ans.getAnswer());
                if (correct)
                    score++;

                QuizAnswer qa = QuizAnswer.builder()
                        .question(q)
                        .chosenAnswer(ans.getAnswer())
                        .correct(correct)
                        .build();
                result.addAnswer(qa);
            }
        }

        result.setScore(score);
        resultRepository.save(result);

        session.setExpired(true);
        sessionRepository.save(session);

        return QuizResultResponse.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .score(result.getScore())
                .totalQuestions(result.getTotalQuestions())
                .quizTitle(result.getQuizSession().getQuiz().getName())
                .completedAt(result.getCompletedAt())
                .build();
    }

    private QuizResultResponse mapResultToDto(QuizResult result) {
        List<QuizResultResponse.Question> questionResultResponse = Collections.emptyList();

        Map<UUID, QuizAnswer> submittedMap = result.getAnswers().stream()
                .collect(Collectors.toMap(sa -> sa.getQuestion().getId(), sa -> sa));

        questionResultResponse = result.getQuizSession().getQuestions().stream()
                .map(question -> {
                    QuizAnswer submitted = submittedMap.get(question.getId());
                    String chosenAnswer = submitted != null ? submitted.getChosenAnswer() : null;

                    return QuizResultResponse.Question.builder()
                            .id(question.getId())
                            .text(question.getText())
                            .options(question.getOptions())
                            .chosenAnswer(chosenAnswer)
                            .correctAnswer(question.getCorrectAnswer())
                            .isCorrect(submitted != null && submitted.isCorrect())
                            .build();
                })
                .toList();

        return QuizResultResponse.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .score(result.getScore())
                .totalQuestions(result.getTotalQuestions())
                .quizTitle(result.getQuizSession().getQuiz().getName())
                .completedAt(result.getCompletedAt())
                .questions(questionResultResponse)
                .build();
    }

    @Override
    public List<QuizResultResponse> getAllResultsForUser(UUID userId) {
        List<QuizResult> quizResults = resultRepository.findByUserId(userId);
        if (quizResults.isEmpty()) {
            return Collections.emptyList();
        }
        return quizResults.stream()
                .sorted(Comparator.comparing(QuizResult::getCompletedAt).reversed())
                .map(this::mapResultToDto)
                .toList();
    }

    @Override
    public QuizResultResponse getResultById(UUID resultId, UUID userId) {
        QuizResult result = resultRepository.findByIdAndQuizSessionUserId(resultId, userId)
                .orElseThrow(() -> new NotFoundException("Result not found"));
        return mapResultToDto(result);
    }

    @Override
    public CountResponse countResultsForUser(UUID userId) {
        long count = resultRepository.countByUserId(userId);
        return new CountResponse(count);
    }
}
