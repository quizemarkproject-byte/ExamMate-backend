package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.AdminQuestionResponse;
import com.exammate.exammate_backend.dto.AdminQuizResponse;
import com.exammate.exammate_backend.dto.MessageResponse;
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

    @Override
    public List<AdminQuizResponse> getAllQuizzesAdmin() {
        List<Quiz> quizzes = quizRepository.findAllByOrderByCreatedAtDesc();
        List<AdminQuizResponse> response = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            AdminQuizResponse aq = modelMapper.map(quiz, AdminQuizResponse.class);
            List<Question> qs = questionRepository.findByCategories_Id(quiz.getId()).orElse(List.of());
            AdminQuestionResponse[] arr = qs.stream()
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
        Long minutes = request.getTimeLimitMinutes();
        quiz.setTimeLimit(Duration.ofMinutes(minutes));
        quiz.setQuestionLimit(request.getQuestionLimit());
        Quiz saved = quizRepository.save(quiz);
        return modelMapper.map(saved, AdminQuizResponse.class);
    }

    @Override
    public AdminQuestionResponse createQuestion(UUID quizId, QuestionRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        // If existingQuestionId is provided, attach that question to this quiz
        if (request.getId() != null) {
            UUID existingId = request.getId();
            Question existing = questionRepository.findById(existingId)
                    .orElseThrow(() -> new BadRequestException("Question with id " + existingId + " not found"));

            // attach to quiz if not already attached
            attachQuestionsToQuiz(quiz, List.of(existing));

            // also ensure the question's categories contains this quiz (maintain bidirectional relation)
            List<Quiz> cats = existing.getCategories();
            if (cats == null) cats = new ArrayList<>();
            if (!cats.contains(quiz)) cats.add(quiz);
            existing.setCategories(cats);
            questionRepository.save(existing);

            return modelMapper.map(existing, AdminQuestionResponse.class);
        }

        // Otherwise create a new question - validate required fields
        validateQuestionRequest(request);

        Question q = new Question();
        q.setText(request.getText());
        q.setOptions(request.getOptions());
        q.setCorrectAnswer(request.getCorrectAnswer());
        q.setCategories(List.of(quiz));
        Question saved = questionRepository.save(q);
        // attach newly created question to quiz
        attachQuestionsToQuiz(quiz, List.of(saved));
        return modelMapper.map(saved, AdminQuestionResponse.class);
    }

    @Override
    public List<AdminQuestionResponse> createQuestions(UUID quizId, List<QuestionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Question requests cannot be empty");
        }

        // Load quiz once
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        List<Question> toSave = new ArrayList<>();
        List<Question> toAttachExisting = new ArrayList<>();

        for (QuestionRequest r : requests) {
            if (r.getId() != null) {
                UUID existingId = r.getId();
                Question existing = questionRepository.findById(existingId)
                        .orElseThrow(() -> new BadRequestException("Question with id " + existingId + " not found"));
                toAttachExisting.add(existing);
            } else {
                // validate
                validateQuestionRequest(r);
                Question q = new Question();
                q.setText(r.getText());
                q.setOptions(r.getOptions());
                q.setCorrectAnswer(r.getCorrectAnswer());
                // do not set categories yet; we'll attach after saving
                toSave.add(q);
            }
        }

        // Persist new questions in batch
        List<Question> savedNew = new ArrayList<>();
        if (!toSave.isEmpty()) {
            savedNew = questionRepository.saveAll(toSave);
        }

        // Combine all questions to attach
        List<Question> allToAttach = new ArrayList<>();
        allToAttach.addAll(toAttachExisting);
        allToAttach.addAll(savedNew);

        // Attach to quiz (owner side) and save quiz once
        attachQuestionsToQuiz(quiz, allToAttach);

        // Ensure question.categories include this quiz and persist changes
        List<Question> updatedQuestions = new ArrayList<>();
        for (Question q : allToAttach) {
            List<Quiz> cats = q.getCategories();
            if (cats == null) cats = new ArrayList<>();
            if (!cats.contains(quiz)) cats.add(quiz);
            q.setCategories(cats);
            updatedQuestions.add(q);
        }
        if (!updatedQuestions.isEmpty()) {
            questionRepository.saveAll(updatedQuestions);
        }

        // Map responses in the same order as requests
        List<AdminQuestionResponse> responses = new ArrayList<>();
        int createdIndex = 0;
        for (QuestionRequest r : requests) {
            if (r.getId() != null) {
                Question existing = questionRepository.findById(r.getId())
                        .orElseThrow(() -> new BadRequestException("Question with id " + r.getId() + " not found"));
                responses.add(modelMapper.map(existing, AdminQuestionResponse.class));
            } else {
                // consume in order from savedNew
                Question created = savedNew.get(createdIndex++);
                responses.add(modelMapper.map(created, AdminQuestionResponse.class));
            }
        }

        return responses;
    }

    private void validateQuestionRequest(QuestionRequest r) {
        if (r.getText() == null || r.getText().isBlank()) {
            throw new BadRequestException("Question text is required");
        }
        if (r.getOptions() == null || r.getOptions().isEmpty()) {
            throw new BadRequestException("Question options are required");
        }
        if (r.getCorrectAnswer() == null || r.getCorrectAnswer().isBlank()) {
            throw new BadRequestException("Question correctAnswer is required");
        }
    }

    // small helper to attach one or more questions to a quiz and persist changes on the owning side (Question)
    private void attachQuestionsToQuiz(Quiz quiz, List<Question> toAttach) {
        if (toAttach == null || toAttach.isEmpty()) return;
        // Update owning side: ensure each Question.categories contains this quiz, persist those questions
        List<Question> toSave = new ArrayList<>();
        for (Question q : toAttach) {
            List<Quiz> cats = q.getCategories();
            if (cats == null) cats = new ArrayList<>();
            if (!cats.contains(quiz)) {
                cats.add(quiz);
                q.setCategories(cats);
                toSave.add(q);
            }
        }

        if (!toSave.isEmpty()) {
            questionRepository.saveAll(toSave);
        }
    }

    @Override
    public MessageResponse detachQuestionFromQuiz(UUID quizId, UUID questionId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new NotFoundException("Quiz not found"));
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new NotFoundException("Question not found"));

        List<Quiz> cats = question.getCategories();
        if (cats != null && cats.removeIf(q -> q.getId().equals(quiz.getId()))) {
            question.setCategories(cats);
            questionRepository.save(question);
            return new MessageResponse("Question detached from quiz");
        }

        return new MessageResponse("Question was not attached to the quiz");
    }

    @Override
    public MessageResponse deleteQuizSafely(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new NotFoundException("Quiz not found"));

        // detach quiz from all questions that reference it
        List<Question> questions = questionRepository.findByCategories_Id(quizId).orElse(List.of());
        if (!questions.isEmpty()) {
            List<Question> toSave = new ArrayList<>();
            for (Question q : questions) {
                List<Quiz> cats = q.getCategories();
                if (cats != null && cats.removeIf(c -> c.getId().equals(quiz.getId()))) {
                    q.setCategories(cats);
                    toSave.add(q);
                }
            }
            if (!toSave.isEmpty()) questionRepository.saveAll(toSave);
        }

        quizRepository.delete(quiz);
        return new MessageResponse("Quiz deleted");
    }

    @Override
    public MessageResponse deleteQuestionSafely(UUID questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new NotFoundException("Question not found"));

        // clear associations first (explicit) then delete
        List<Quiz> cats = question.getCategories();
        if (cats != null && !cats.isEmpty()) {
            question.setCategories(new ArrayList<>());
            questionRepository.save(question);
        }

        questionRepository.delete(question);
        return new MessageResponse("Question deleted");
    }
}
