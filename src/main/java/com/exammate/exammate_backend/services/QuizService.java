package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.dto.QuizResponse;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    List<QuizResponse> getAllQuizzes();
    List<QuestionResponse> getQuestionsByQuiz(UUID quizId);
}
