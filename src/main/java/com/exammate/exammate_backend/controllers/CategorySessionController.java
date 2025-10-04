package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.CategoryResultResponse;
import com.exammate.exammate_backend.dto.CategorySessionStartRequest;
import com.exammate.exammate_backend.dto.CategorySessionStartResponse;
import com.exammate.exammate_backend.dto.CategorySessionSubmissionRequest;
import com.exammate.exammate_backend.services.CategorySessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/category-sessions")
@Tag(name = "Category Sessions", description = "Endpoints for starting sessions, submitting answers, and retrieving results for category-based quizzes.")
@RequiredArgsConstructor
public class CategorySessionController {
    private final CategorySessionService categorySessionService;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Start a session for a category", description = "Start a new session for a user in a specific category and retrieve questions.")
    public CategorySessionStartResponse startSession(@RequestBody CategorySessionStartRequest request) {
        return categorySessionService.startSession(request);
    }

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Submit answers for a session", description = "Submit answers for a session and store the result.")
    public CategoryResultResponse submitSession(@RequestBody CategorySessionSubmissionRequest request) {
        return categorySessionService.submitSession(request);
    }

    @GetMapping("/results/{userId}")
    @Operation(summary = "Get all results for a user", description = "Retrieve all quiz results for a specific user.")
    public List<CategoryResultResponse> getAllResultsForUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable String userId) {
        return categorySessionService.getAllResultsForUser(userId);
    }

    @GetMapping("/result/{resultId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get result by ID", description = "Retrieve a specific quiz result by its ID.")
    public CategoryResultResponse getResultById(
            @Parameter(description = "Result ID (UUID)", required = true)
            @PathVariable UUID resultId) {
        return categorySessionService.getResultById(resultId);
    }
}
