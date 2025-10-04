package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.QuestionResponse;
import com.exammate.exammate_backend.services.QuizService;
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
@RequestMapping("/api/v1/quizzes")
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
}
