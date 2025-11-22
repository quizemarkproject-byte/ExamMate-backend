package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.AdminQuestionResponse;
import com.exammate.exammate_backend.dto.AdminQuizResponse;
import com.exammate.exammate_backend.dto.QuestionRequest;
import com.exammate.exammate_backend.dto.QuizRequest;

import java.util.List;
import java.util.UUID;

public interface AdminQuizService {
    List<AdminQuizResponse> getAllQuizzesAdmin();
    List<AdminQuestionResponse> getAllQuestions();
    AdminQuizResponse createQuiz(QuizRequest request);
    AdminQuestionResponse createQuestion(QuestionRequest request);
    AdminQuestionResponse updateQuestion(UUID questionId, QuestionRequest request);
    List<AdminQuestionResponse> updateQuizQuestions(UUID quizId, List<QuestionRequest> requests);
    void detachQuestionFromQuiz(UUID quizId, UUID questionId);
    void deleteQuizSafely(UUID quizId);
    void deleteQuestionSafely(UUID questionId);
}
