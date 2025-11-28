package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name); // Case-insensitive existence check
    Optional<Quiz> findByNameIgnoreCase(String name); // Find quiz by name ignoring case
    long countByNameIgnoreCase(String name); // Count quizzes ignoring case
    List<Quiz> findAllByNameIgnoreCase(String name); // Return all quizzes matching name ignoring case
    List<Quiz> findAllByOrderByCreatedAtDesc();
}
