package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, UUID> {
}
