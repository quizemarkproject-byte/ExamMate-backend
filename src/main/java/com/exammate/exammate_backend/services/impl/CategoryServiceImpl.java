package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.CategoryResponse;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.exception.NotFoundException;
import com.exammate.exammate_backend.models.Category;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.repositories.CategoryRepository;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import com.exammate.exammate_backend.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .build())
                .toList();
    }

    @Override
    public List<QuestionResponse> getQuestionsByCategory(UUID categoryId) {
        categoryRepository.findById(categoryId).orElseThrow(
            () -> new NotFoundException("Category not found")
        );
        List<Question> questions = questionRepository.findByCategories_Id(categoryId).orElse(List.of());
        return questions.stream()
        .map(q -> QuestionResponse.builder()
            .id(q.getId())
            .text(q.getText())
            .options(q.getOptions())
            .build())
        .toList();
    }
}
