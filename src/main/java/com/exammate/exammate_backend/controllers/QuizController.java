package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.AdminQuestionResponse;
import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.dto.QuizRequest;
import com.exammate.exammate_backend.dto.QuestionRequest;
import com.exammate.exammate_backend.services.QuizService;
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
@RequestMapping("/quizzes")
@Tag(name = "Quizzes", description = "Endpoints for managing and retrieving quizzes and their questions.")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping
    @Operation(summary = "Get all quizzes", description = "Retrieve a list of all available quizzes.")
    public List<QuizResponse> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    @GetMapping("/{quizId}/questions")
    @Operation(summary = "Get questions by quiz", description = "Retrieve all questions for a specific quiz by its ID.")
    @ResponseStatus(HttpStatus.OK)
    public List<QuestionResponse> getQuestionsByQuiz(
        @Parameter(description = "UUID of the quiz", required = true)
        @PathVariable UUID quizId) {
        return quizService.getQuestionsByQuiz(quizId);
    }

    // admin endpoints
    @GetMapping("/questions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all questions", description = "Retrieve a list of all questions in the system")
    public List<AdminQuestionResponse> getAllQuestions() {
        return quizService.getAllQuestions();
    }

    // admin endpoints
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a quiz", description = "Create a new quiz")
    public QuizResponse createQuiz(@Valid @RequestBody QuizRequest request) {
        return quizService.createQuiz(request);
    }

    // admin endpoints
    @PostMapping("/{quizId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a question", description = "Create a new question under the specified quiz")
    public AdminQuestionResponse createQuestion(
            @Parameter(description = "UUID of the quiz", required = true)
            @PathVariable UUID quizId,
            @Valid @RequestBody QuestionRequest request) {
        return quizService.createQuestion(quizId, request);
    }

    // admin endpoints
    @PostMapping("/{quizId}/questions/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create or attach multiple questions", description = "Create or attach multiple questions to the specified quiz in one request")
    public List<AdminQuestionResponse> createQuestionsInBulk(
             @Parameter(description = "UUID of the quiz", required = true)
             @PathVariable UUID quizId,
             @Valid @RequestBody List<QuestionRequest> requests) {
        return quizService.createQuestions(quizId, requests);
    }
}
