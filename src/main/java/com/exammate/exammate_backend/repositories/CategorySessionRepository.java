package com.exammate.exammate_backend.repositories;

import com.exammate.exammate_backend.models.CategorySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategorySessionRepository extends JpaRepository<CategorySession, UUID> {
    List<CategorySession> findByUserId(String userId);

    // Find in-progress session for a user and category
    CategorySession findFirstByUserIdAndCategory_IdAndExpiredFalse(String userId, UUID categoryId);
}
