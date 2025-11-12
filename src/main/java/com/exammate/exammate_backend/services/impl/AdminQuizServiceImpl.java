package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.*;
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
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminQuizServiceImpl implements AdminQuizService {

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
        return questionRepository.findAll().stream()
                .map(q -> modelMapper.map(q, AdminQuestionResponse.class))
                .toList();
    }

    @Override
    public AdminQuizResponse createQuiz(QuizRequest request) {
        if (quizRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Quiz with the same name already exists");
        }

        Quiz quiz = new Quiz();
        quiz.setName(request.getName());
        quiz.setTimeLimit(Duration.ofMinutes(request.getTimeLimitMinutes()));
        quiz.setQuestionLimit(request.getQuestionLimit());

        Quiz saved = quizRepository.save(quiz);
        return modelMapper.map(saved, AdminQuizResponse.class);
    }

    // ───────────────────────────────
    // SINGLE QUESTION CREATE / UPDATE
    // ───────────────────────────────
    @Override
    public AdminQuestionResponse createQuestion(UUID quizId, QuestionRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        Question question;

        if (request.getId() != null) {
            // Existing question (PUT-style overwrite)
            question = questionRepository.findById(request.getId())
                    .orElseThrow(() -> new BadRequestException("Question not found"));
            question.setText(request.getText());
            question.setOptions(request.getOptions());
            question.setCorrectAnswer(request.getCorrectAnswer());
        } else {
            // New question
            question = new Question();
            question.setText(request.getText());
            question.setOptions(request.getOptions());
            question.setCorrectAnswer(request.getCorrectAnswer());
        }

        // Attach quiz if not already linked
        List<Quiz> categories = question.getCategories();
        if (categories == null) categories = new ArrayList<>();
        if (!categories.contains(quiz)) {
            categories.add(quiz);
            question.setCategories(categories);
        }

        // Only save if new (Hibernate auto-flush handles existing)
        Question saved = (question.getId() == null)
                ? questionRepository.save(question)
                : question;

        return modelMapper.map(saved, AdminQuestionResponse.class);
    }

    // ───────────────────────────────
    // BULK QUESTION CREATE / UPDATE
    // ───────────────────────────────
    @Override
    public List<AdminQuestionResponse> createQuestions(UUID quizId, List<QuestionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Question requests cannot be empty");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        List<Question> newQuestions = new ArrayList<>();
        List<Question> resultList = new ArrayList<>();

        for (QuestionRequest r : requests) {
            Question q;

            if (r.getId() != null) {
                q = questionRepository.findById(r.getId())
                        .orElseThrow(() -> new BadRequestException("Question with id " + r.getId() + " not found"));
                q.setText(r.getText());
                q.setOptions(r.getOptions());
                q.setCorrectAnswer(r.getCorrectAnswer());
            } else {
                q = new Question();
                q.setText(r.getText());
                q.setOptions(r.getOptions());
                q.setCorrectAnswer(r.getCorrectAnswer());
                newQuestions.add(q);
            }

            // Attach quiz
            List<Quiz> cats = q.getCategories();
            if (cats == null) cats = new ArrayList<>();
            if (!cats.contains(quiz)) cats.add(quiz);
            q.setCategories(cats);

            resultList.add(q);
        }

        // Only save newly created questions
        if (!newQuestions.isEmpty()) {
            questionRepository.saveAll(newQuestions);
        }

        // Hibernate auto-flushes updates for existing ones
        return resultList.stream()
                .map(q -> modelMapper.map(q, AdminQuestionResponse.class))
                .toList();
    }


    // ───────────────────────────────
    // DETACH & DELETE OPERATIONS
    // ───────────────────────────────
    @Override
    public MessageResponse detachQuestionFromQuiz(UUID quizId, UUID questionId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        List<Quiz> cats = question.getCategories();
        if (cats != null && cats.removeIf(q -> q.getId().equals(quiz.getId()))) {
            question.setCategories(cats);
            // Hibernate auto-flush will persist this change
            return new MessageResponse("Question detached from quiz");
        }

        return new MessageResponse("Question was not attached to the quiz");
    }

    @Override
    public MessageResponse deleteQuizSafely(UUID quizId) {
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
        return new MessageResponse("Quiz deleted");
    }

    @Override
    public MessageResponse deleteQuestionSafely(UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // Clear associations before deleting
        List<Quiz> cats = question.getCategories();
        if (cats != null && !cats.isEmpty()) {
            question.setCategories(new ArrayList<>());
        }

        questionRepository.delete(question);
        return new MessageResponse("Question deleted");
    }
}
