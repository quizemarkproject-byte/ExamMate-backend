package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.CategoryResponse;
import com.exammate.exammate_backend.dto.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    List<QuestionResponse> getQuestionsByCategory(UUID categoryId);
}
