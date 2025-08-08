package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.models.Quiz;

import java.util.List;

public interface QuizRepository {
    Quiz findQuizById(String quizId);
    List<Quiz> findAllQuizzes();
}
