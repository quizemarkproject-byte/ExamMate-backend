package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import com.exammate.exammate_backend.repositories.QuizRepository;
import com.exammate.exammate_backend.services.QuizService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
        List<Quiz> quizzes = quizRepository.findAllByOrderByCreatedAtDesc();
        List<QuizResponse> responses = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            List<Question> questions = questionRepository.findByCategories_Id(quiz.getId()).orElse(List.of());
            if (questions.isEmpty()) continue;
            responses.add(modelMapper.map(quiz, QuizResponse.class));
        }
        return responses;
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
}
