package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.AdminQuestionResponse;
import com.exammate.exammate_backend.dto.AdminQuizResponse;
import com.exammate.exammate_backend.dto.MessageResponse;
import com.exammate.exammate_backend.dto.QuestionRequest;
import com.exammate.exammate_backend.dto.QuizRequest;

import java.util.List;
import java.util.UUID;

public interface AdminQuizService {
    List<AdminQuizResponse> getAllQuizzesAdmin();
    List<AdminQuestionResponse> getAllQuestions();
    AdminQuizResponse createQuiz(QuizRequest request);
    AdminQuestionResponse createQuestion(UUID quizId, QuestionRequest request);
    List<AdminQuestionResponse> updateQuizQuestions(UUID quizId, List<QuestionRequest> requests);
    MessageResponse detachQuestionFromQuiz(UUID quizId, UUID questionId);
    MessageResponse deleteQuizSafely(UUID quizId);
    MessageResponse deleteQuestionSafely(UUID questionId);
}
