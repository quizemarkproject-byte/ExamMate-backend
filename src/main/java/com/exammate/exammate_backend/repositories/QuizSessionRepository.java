package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {
    List<QuizSession> findByUserId(String userId);

    QuizSession findFirstByUserIdAndQuiz_IdAndExpiredFalse(String userId, UUID quizId);
}
