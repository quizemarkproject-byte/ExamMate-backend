package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.CategoryResponse;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Endpoints for managing and retrieving categories and their questions.")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve a list of all available categories.")
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{categoryId}/questions")
    @Operation(summary = "Get questions by category", description = "Retrieve all questions for a specific category by its ID.")
    @ResponseStatus(HttpStatus.OK)
    public List<QuestionResponse> getQuestionsByCategory(
        @Parameter(description = "UUID of the category", required = true)
        @PathVariable UUID categoryId) {
    return categoryService.getQuestionsByCategory(categoryId);
    }
}
