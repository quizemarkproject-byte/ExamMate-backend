package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository  extends JpaRepository<Question, UUID> {
	Optional<List<Question>> findByCategories_Id(UUID quizId);
}
