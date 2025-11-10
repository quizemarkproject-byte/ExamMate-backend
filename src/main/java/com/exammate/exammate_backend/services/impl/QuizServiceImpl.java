package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.QuestionRequest;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.dto.QuizRequest;
import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.exception.BadRequestException;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.repositories.QuizRepository;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import com.exammate.exammate_backend.services.QuizService;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(quiz -> modelMapper.map(quiz, QuizResponse.class))
                .toList();
    }

    @Override
    public List<QuestionResponse> getQuestionsByQuiz(UUID quizId) {
        quizRepository.findById(quizId).orElseThrow(
            () -> new NotFoundException("Quiz not found")
        );
        List<Question> questions = questionRepository.findByCategories_Id(quizId).orElse(List.of());
        return questions.stream()
        .map(q -> modelMapper.map(q, QuestionResponse.class))
        .toList();
    }

    @Override
    public List<QuestionResponse> getAllQuestions(){
        return questionRepository.findAll().stream()
                .map(q -> modelMapper.map(q, QuestionResponse.class))
                .toList();
    }

    @Override
    public QuizResponse createQuiz(QuizRequest request) {
        if (quizRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Quiz with the same name already exists");
        }
        Quiz quiz = new Quiz();
        quiz.setName(request.getName());
        Long minutes = request.getTimeLimitMinutes();
        quiz.setTimeLimit(Duration.ofMinutes(minutes));
        quiz.setQuestionLimit(request.getQuestionLimit());
        quiz.setQuestions(new ArrayList<>());
        Quiz saved = quizRepository.save(quiz);
        return modelMapper.map(saved, QuizResponse.class);
    }

    @Override
    public QuestionResponse createQuestion(UUID quizId, QuestionRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        // If existingQuestionId is provided, attach that question to this quiz
        if (request.getExistingQuestionId() != null) {
            UUID existingId = request.getExistingQuestionId();
            Question existing = questionRepository.findById(existingId)
                    .orElseThrow(() -> new BadRequestException("Question with id " + existingId + " not found"));

            // attach to quiz if not already attached
            List<Question> qs = quiz.getQuestions();
            if (qs == null) qs = new ArrayList<>();
            if (!qs.contains(existing)) qs.add(existing);
            quiz.setQuestions(qs);
            quizRepository.save(quiz);

            // also ensure the question's categories contains this quiz (maintain bidirectional relation)
            List<Quiz> cats = existing.getCategories();
            if (cats == null) cats = new ArrayList<>();
            if (!cats.contains(quiz)) cats.add(quiz);
            existing.setCategories(cats);
            questionRepository.save(existing);

            return modelMapper.map(existing, QuestionResponse.class);
        }

        // Otherwise create a new question - validate required fields
        if (request.getText() == null || request.getText().isBlank()) {
            throw new BadRequestException("Question text is required");
        }
        if (request.getOptions() == null || request.getOptions().isEmpty()) {
            throw new BadRequestException("Question options are required");
        }
        if (request.getCorrectAnswer() == null || request.getCorrectAnswer().isBlank()) {
            throw new BadRequestException("Question correctAnswer is required");
        }

        Question q = new Question();
        q.setText(request.getText());
        q.setOptions(request.getOptions());
        q.setCorrectAnswer(request.getCorrectAnswer());
        q.setCategories(List.of(quiz));
        Question saved = questionRepository.save(q);
        // attach to quiz if not already
        List<Question> qs = quiz.getQuestions();
        if (qs == null) {
            qs = new ArrayList<>();
        }
        if (!qs.contains(saved)) qs.add(saved);
        quiz.setQuestions(qs);
        quizRepository.save(quiz);
        return modelMapper.map(saved, QuestionResponse.class);
    }

    @Override
    public List<QuestionResponse> createQuestions(UUID quizId, List<QuestionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Question requests cannot be empty");
        }

        // Load quiz once
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        List<Question> toSave = new ArrayList<>();
        List<Question> toAttachExisting = new ArrayList<>();

        for (QuestionRequest r : requests) {
            if (r.getExistingQuestionId() != null) {
                UUID existingId = r.getExistingQuestionId();
                Question existing = questionRepository.findById(existingId)
                        .orElseThrow(() -> new BadRequestException("Question with id " + existingId + " not found"));
                toAttachExisting.add(existing);
            } else {
                // validate
                if (r.getText() == null || r.getText().isBlank()) {
                    throw new BadRequestException("Question text is required");
                }
                if (r.getOptions() == null || r.getOptions().isEmpty()) {
                    throw new BadRequestException("Question options are required");
                }
                if (r.getCorrectAnswer() == null || r.getCorrectAnswer().isBlank()) {
                    throw new BadRequestException("Question correctAnswer is required");
                }
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
        List<Question> qs = quiz.getQuestions();
        if (qs == null) qs = new ArrayList<>();
        for (Question q : allToAttach) {
            if (!qs.contains(q)) qs.add(q);
        }
        quiz.setQuestions(qs);
        quizRepository.save(quiz);

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
        List<QuestionResponse> responses = new ArrayList<>();
        for (QuestionRequest r : requests) {
            if (r.getExistingQuestionId() != null) {
                Question existing = questionRepository.findById(r.getExistingQuestionId()).get();
                responses.add(modelMapper.map(existing, QuestionResponse.class));
            } else {
                // consume in order from savedNew
                Question created = savedNew.removeFirst();
                responses.add(modelMapper.map(created, QuestionResponse.class));
            }
        }

        return responses;
    }
}
