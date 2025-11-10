package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.AdminQuestionResponse;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.QuizRequest;
import com.exammate.exammate_backend.dto.QuestionRequest;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    List<QuizResponse> getAllQuizzes();
    List<QuestionResponse> getQuestionsByQuiz(UUID quizId);
    // admin: returns answers including correctAnswer
    List<AdminQuestionResponse> getAllQuestions();

    // Create a new quiz from a request DTO and return the created quiz DTO
    QuizResponse createQuiz(QuizRequest request);

    // Create a new question under a quiz and return the created question DTO
    AdminQuestionResponse createQuestion(UUID quizId, QuestionRequest request);

    // Create or attach multiple questions in bulk; returns list of created/attached question responses
    List<AdminQuestionResponse> createQuestions(UUID quizId, List<QuestionRequest> requests);
}
