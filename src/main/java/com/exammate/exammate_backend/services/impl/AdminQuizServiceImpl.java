package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.AdminQuestionResponse;
import com.exammate.exammate_backend.dto.AdminQuizResponse;
import com.exammate.exammate_backend.dto.QuestionRequest;
import com.exammate.exammate_backend.dto.QuizRequest;
import com.exammate.exammate_backend.exception.BadRequestException;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import com.exammate.exammate_backend.repositories.QuizRepository;
import com.exammate.exammate_backend.services.AdminQuizService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminQuizServiceImpl implements AdminQuizService {

    private static final Logger log = LoggerFactory.getLogger(AdminQuizServiceImpl.class);

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ModelMapper modelMapper;

    // ───────────────────────────────
    // QUIZ CRUD
    // ───────────────────────────────
    @Override
    public List<AdminQuizResponse> getAllQuizzesAdmin() {
        List<Quiz> quizzes = quizRepository.findAllByOrderByCreatedAtDesc();
        List<AdminQuizResponse> response = new ArrayList<>();

        for (Quiz quiz : quizzes) {
            AdminQuizResponse aq = modelMapper.map(quiz, AdminQuizResponse.class);
            List<Question> questions = questionRepository.findByCategories_Id(quiz.getId()).orElse(List.of());
            AdminQuestionResponse[] arr = questions.stream()
                    .map(q -> modelMapper.map(q, AdminQuestionResponse.class))
                    .toArray(AdminQuestionResponse[]::new);
            aq.setQuestions(arr);
            response.add(aq);
        }
        return response;
    }

    @Override
    public List<AdminQuestionResponse> getAllQuestions() {
        return questionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(q -> modelMapper.map(q, AdminQuestionResponse.class))
                .toList();
    }

    @Override
    public AdminQuizResponse createQuiz(QuizRequest request) {
        // Use raw incoming name (no trim/no blank check)
        String incomingName = request.getName();

        if (quizRepository.existsByNameIgnoreCase(incomingName)) {
            throw new BadRequestException("Quiz with the same name already exists");
        }

        Quiz quiz = new Quiz();
        quiz.setName(incomingName);
        quiz.setTimeLimit(Duration.ofMinutes(request.getTimeLimitMinutes()));
        quiz.setQuestionLimit(request.getQuestionLimit());

        Quiz saved = quizRepository.save(quiz);
        return modelMapper.map(saved, AdminQuizResponse.class);
    }

    @Override
    public AdminQuestionResponse createQuestion(QuestionRequest request) {
        Question question = Question.builder()
                .text(request.getText())
                .options(request.getOptions())
                .correctAnswer(request.getCorrectAnswer())
                .categories(new ArrayList<>())
                .build();
        Question saved = questionRepository.save(question);
        return modelMapper.map(saved, AdminQuestionResponse.class);
    }

    @Override
    public AdminQuestionResponse updateQuestion(UUID questionId, QuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // Update fields
        question.setText(request.getText());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());

        // Ensure categories list exists (do not modify associations here)
        List<Quiz> cats = question.getCategories();
        if (cats == null) question.setCategories(new ArrayList<>());

        // Save and return
        Question saved = questionRepository.save(question);
        return modelMapper.map(saved, AdminQuestionResponse.class);
    }

    // ───────────────────────────────
    // BULK QUESTION CREATE / UPDATE
    // ───────────────────────────────
    @Override
    public List<AdminQuestionResponse> updateQuizQuestions(UUID quizId, List<QuestionRequest> requests) {

        if (requests == null)
            throw new BadRequestException("Requests cannot be null");

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        List<Question> current = questionRepository.findByCategories_Id(quizId).orElse(List.of());

        // Build desired final state
        List<Question> desired = buildDesiredQuestions(requests, quiz);

        // Persist desired questions
        questionRepository.saveAll(desired);

        // Remove associations missing from request
        removeMissingAssociations(quiz, current, desired);

        // Return final state
        List<Question> finalList = questionRepository.findByCategories_Id(quizId).orElse(List.of());
        return finalList.stream()
                .map(q -> modelMapper.map(q, AdminQuestionResponse.class))
                .toList();
    }

    private void removeMissingAssociations(
            Quiz quiz,
            List<Question> current,
            List<Question> desired
    ) {
        Set<UUID> desiredIds = desired.stream()
                .map(Question::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Question q : current) {
            if (!desiredIds.contains(q.getId())) {
                List<Quiz> cats = q.getCategories();
                if (cats != null) {
                    boolean removed = cats.removeIf(c -> c.getId().equals(quiz.getId()));
                    if (removed) {
                        q.setCategories(cats);
                        questionRepository.save(q);
                    }
                }
            }
        }
    }

    private List<Question> buildDesiredQuestions(List<QuestionRequest> requests, Quiz quiz) {
        List<Question> result = new ArrayList<>();

        for (QuestionRequest r : requests) {
            Question q;

            if (r.getId() != null) {
                q = questionRepository.findById(r.getId())
                        .orElseThrow(() -> new NotFoundException("Question not found: " + r.getId()));
            } else {
                q = new Question();
            }

            q.setText(r.getText());
            q.setOptions(r.getOptions());
            q.setCorrectAnswer(r.getCorrectAnswer());

            // Always ensure association
            List<Quiz> cats = q.getCategories();
            if (cats == null) {
                cats = new ArrayList<>();
            }
            boolean already = cats.stream().anyMatch(c -> c.getId() != null && c.getId().equals(quiz.getId()));
            if (!already) {
                cats.add(quiz);
            }
            q.setCategories(cats);

            result.add(q);
        }

        return result;
    }


    // ───────────────────────────────
    // DETACH & DELETE OPERATIONS
    // ───────────────────────────────
    @Override
    public void detachQuestionFromQuiz(UUID quizId, UUID questionId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        List<Quiz> cats = question.getCategories();
        if (cats != null && cats.removeIf(q -> q.getId().equals(quiz.getId()))) {
            question.setCategories(cats);
            // Hibernate auto-flush will persist this change
        }
    }

    @Override
    public void deleteQuizSafely(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        // Detach quiz from all related questions
        List<Question> questions = questionRepository.findByCategories_Id(quizId).orElse(List.of());
        for (Question q : questions) {
            List<Quiz> cats = q.getCategories();
            if (cats != null) cats.removeIf(c -> c.getId().equals(quizId));
            q.setCategories(cats);
        }

        // No need for manual saveAll — @Transactional auto flushes before delete
        quizRepository.delete(quiz);
    }

    @Override
    public void deleteQuestionSafely(UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // Clear associations before deleting
        List<Quiz> cats = question.getCategories();
        if (cats != null && !cats.isEmpty()) {
            question.setCategories(new ArrayList<>());
        }

        questionRepository.delete(question);
    }

    @Override
    public AdminQuizResponse updateQuiz(UUID quizId, QuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        // Use raw incoming name (no trim/no blank check)
        String incomingName = request.getName();

        // If incoming name equals the current name ignoring case, allow update without DB duplicate checks
        String currentName = quiz.getName() == null ? "" : quiz.getName();
        if (!currentName.equalsIgnoreCase(incomingName)) {
            // Check for an existing quiz with the same name (case-insensitive)
            Optional<Quiz> existing = quizRepository.findByNameIgnoreCase(incomingName);
            if (existing.isPresent() && !existing.get().getId().equals(quiz.getId())) {
                String msg = String.format("Quiz with the same name already exists (incoming='%s', current='%s', conflictingId=%s)", incomingName, currentName, existing.get().getId());
                log.warn(msg);
                throw new BadRequestException("Quiz with the same name already exists");
            }
        }

        // Safe to update
        quiz.setName(incomingName);
        quiz.setTimeLimit(Duration.ofMinutes(request.getTimeLimitMinutes()));
        quiz.setQuestionLimit(request.getQuestionLimit());

        Quiz saved = quizRepository.save(quiz);

        AdminQuizResponse resp = modelMapper.map(saved, AdminQuizResponse.class);
        List<Question> questions = questionRepository.findByCategories_Id(saved.getId()).orElse(List.of());
        AdminQuestionResponse[] arr = questions.stream()
                .map(q -> modelMapper.map(q, AdminQuestionResponse.class))
                .toArray(AdminQuestionResponse[]::new);
        resp.setQuestions(arr);

        return resp;
    }
}
