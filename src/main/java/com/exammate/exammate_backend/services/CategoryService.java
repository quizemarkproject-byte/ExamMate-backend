package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.models.Category;
import com.exammate.exammate_backend.models.Question;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<Category> getAllCategories();
    List<Question> getQuestionsByCategory(UUID categoryId);
}
