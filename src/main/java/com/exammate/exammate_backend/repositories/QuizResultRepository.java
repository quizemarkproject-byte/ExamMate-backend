package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {
    List<QuizResult> findByUserId(UUID userId);
    Optional<QuizResult> findByIdAndQuizSessionUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}
