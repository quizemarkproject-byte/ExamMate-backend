package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    boolean existsByName(String name);
}
