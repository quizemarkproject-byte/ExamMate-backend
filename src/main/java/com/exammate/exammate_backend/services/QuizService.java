package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.StartQuizResponse;
import com.exammate.exammate_backend.dto.UserQuizSubmissionRequest;
import com.exammate.exammate_backend.dto.UserTimeRemainingResponse;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    QuizResponse getQuizById(UUID id);
    List<QuizResponse> getAllQuizzes();

    QuizResultResponse submitQuiz(UUID quizId, UserQuizSubmissionRequest userQuizSubmissionRequest);

    StartQuizResponse startQuiz(UUID quizId, String userId);

    UserTimeRemainingResponse getRemainingTime(UUID quizId, String userId, UUID quizSessionId);
}
