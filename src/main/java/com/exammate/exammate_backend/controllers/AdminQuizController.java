package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.AdminQuestionResponse;
import com.exammate.exammate_backend.dto.AdminQuizResponse;
import com.exammate.exammate_backend.dto.QuestionRequest;
import com.exammate.exammate_backend.dto.QuizRequest;
import com.exammate.exammate_backend.services.AdminQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/quizzes")
@Tag(name = "Admin - Quizzes", description = "Admin endpoints for managing quizzes and questions")
@RequiredArgsConstructor
public class AdminQuizController {
    private final AdminQuizService adminQuizService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Admin: Get all quizzes", description = "Retrieve a list of all quizzes with questions (admin view)")
    public List<AdminQuizResponse> getAllQuizzes() {
        return adminQuizService.getAllQuizzesAdmin();
    }

    @GetMapping("/questions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Admin: Get all questions", description = "Retrieve a list of all questions in the system (includes correctAnswer)")
    public List<AdminQuestionResponse> getAllQuestions() {
        return adminQuizService.getAllQuestions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Admin: Create a quiz", description = "Create a new quiz")
    public AdminQuizResponse createQuiz(@Valid @RequestBody QuizRequest request) {
        return adminQuizService.createQuiz(request);
    }

    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Admin: Create a question", description = "Create a new standalone question")
    public AdminQuestionResponse createQuestion(
            @Valid @RequestBody QuestionRequest request) {
        return adminQuizService.createQuestion(request);
    }

    @PostMapping("/{quizId}/questions/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Admin: Bulk create/attach questions", description = "Create or attach multiple questions to the specified quiz in one request")
    public List<AdminQuestionResponse> createQuestionsInBulk(
            @Parameter(description = "UUID of the quiz", required = true)
            @PathVariable UUID quizId,
            @Valid @RequestBody List<@Valid QuestionRequest> requests) {
        return adminQuizService.updateQuizQuestions(quizId, requests);
    }

    @PutMapping("/questions/{questionId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Admin: Update question", description = "Update an existing question and return the updated question")
    public AdminQuestionResponse updateQuestion(
            @Parameter(description = "UUID of the question", required = true) @PathVariable UUID questionId,
            @Valid @RequestBody QuestionRequest request) {
        return adminQuizService.updateQuestion(questionId, request);
    }

    // NOTE: not used currently
    @DeleteMapping("/{quizId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Admin: Detach question", description = "Remove a question's association with a quiz without deleting the question")
    public void detachQuestion(
            @Parameter(description = "UUID of the quiz", required = true) @PathVariable UUID quizId,
            @Parameter(description = "UUID of the question", required = true) @PathVariable UUID questionId) {
        adminQuizService.detachQuestionFromQuiz(quizId, questionId);
    }

    @DeleteMapping("/{quizId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Admin: Delete quiz", description = "Delete a quiz and detach it from its questions")
    public void deleteQuiz(@Parameter(description = "UUID of the quiz", required = true) @PathVariable UUID quizId) {
        adminQuizService.deleteQuizSafely(quizId);
    }

    @DeleteMapping("/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Admin: Delete question", description = "Delete a question and remove it from any quizzes")
    public void deleteQuestion(@Parameter(description = "UUID of the question", required = true) @PathVariable UUID questionId) {
        adminQuizService.deleteQuestionSafely(questionId);
    }
}
