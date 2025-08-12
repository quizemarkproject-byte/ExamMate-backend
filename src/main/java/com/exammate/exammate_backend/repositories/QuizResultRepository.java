package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.dto.ResultResponse;
import com.exammate.exammate_backend.models.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {
    Optional<QuizResult> findByIdAndUserId(UUID resultId, String userId);
    List<QuizResult> findAllByUserId(String userId);
}
