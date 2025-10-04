package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.CategoryResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryResultRepository extends JpaRepository<CategoryResult, UUID> {
    List<CategoryResult> findByUserId(String userId);
}
