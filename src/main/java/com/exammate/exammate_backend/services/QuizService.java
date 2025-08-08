package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.UserQuizSubmission;

import java.util.List;

public interface QuizService {
    QuizResponse getQuizById(String id);
    List<QuizResponse> getAllQuizzes();

    QuizResultResponse submitQuiz(String quizId, UserQuizSubmission userQuizSubmission);
}
