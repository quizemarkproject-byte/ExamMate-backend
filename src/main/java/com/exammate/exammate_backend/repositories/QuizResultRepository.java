package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {
    Optional<QuizResult> findByIdAndQuizIdAndUserId(UUID resultId, UUID quizId, String userId);
}
