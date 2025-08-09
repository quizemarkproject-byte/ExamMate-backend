package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {
    Optional<QuizSession> findByIdAndQuizIdAndUserId(UUID id, UUID quizId, String userId);
    Optional<QuizSession> findByIdAndQuizIdAndUserIdAndExpired(UUID id, UUID quizId, String userId, boolean expired);
    Optional<QuizSession> findByQuizIdAndUserId(UUID quizId, String userId);
    Optional<QuizSession> findByQuizIdAndUserIdAndExpired(UUID quizId, String userId, boolean expired);
}
