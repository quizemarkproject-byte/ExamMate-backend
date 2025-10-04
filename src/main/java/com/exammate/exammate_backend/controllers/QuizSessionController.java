package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.QuizSessionStartRequest;
import com.exammate.exammate_backend.dto.QuizSessionStartResponse;
import com.exammate.exammate_backend.dto.QuizSessionSubmissionRequest;
import com.exammate.exammate_backend.services.QuizSessionService;
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
@RequestMapping("/api/v1/quiz-sessions")
@Tag(name = "Quiz Sessions", description = "Endpoints for starting sessions, submitting answers, and retrieving results for quiz sessions.")
@RequiredArgsConstructor
public class QuizSessionController {
    private final QuizSessionService quizSessionService;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Start a quiz session", description = "Start a new session for a user in a specific quiz and retrieve questions.")
    public QuizSessionStartResponse startSession(@RequestBody QuizSessionStartRequest request) {
        return quizSessionService.startSession(request);
    }

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Submit answers for a session", description = "Submit answers for a session and store the result.")
    public QuizResultResponse submitSession(@RequestBody QuizSessionSubmissionRequest request) {
        return quizSessionService.submitSession(request);
    }

    @GetMapping("/results/{userId}")
    @Operation(summary = "Get all results for a user", description = "Retrieve all quiz results for a specific user.")
    public List<QuizResultResponse> getAllResultsForUser(
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        return quizSessionService.getAllResultsForUser(userId);
    }

    @GetMapping("/result/{resultId}/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get result by ID", description = "Retrieve a specific quiz result by its ID.")
    public QuizResultResponse getResultById(
            @Parameter(description = "Result ID (UUID)", required = true) @PathVariable UUID resultId,
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        return quizSessionService.getResultById(resultId, userId);
    }
}
