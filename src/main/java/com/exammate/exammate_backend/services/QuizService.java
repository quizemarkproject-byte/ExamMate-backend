package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.ResultResponse;
import com.exammate.exammate_backend.dto.UserQuizSubmissionRequest;
import com.exammate.exammate_backend.dto.UserTimeRemainingResponse;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    QuizResponse getQuizById(UUID id);
    List<QuizResponse> getAllQuizzes();

    ResultResponse submitQuiz(UUID quizId, UserQuizSubmissionRequest userQuizSubmissionRequest);

    QuizResponse startQuiz(UUID quizId, String userId);

    UserTimeRemainingResponse getRemainingTime(UUID quizId, String userId, UUID quizSessionId);

    ResultResponse getUserQuizResult(UUID quizId, UUID resultId, String userId);
}
