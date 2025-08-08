package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.UserQuizSubmission;
import com.exammate.exammate_backend.services.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quiz")
public class QuizController {
    private final QuizService quizService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<QuizResponse> findAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    @GetMapping("/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    public QuizResponse findQuizById(@PathVariable String quizId) {
        return quizService.getQuizById(quizId);
    }

    @PostMapping("/submit/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    public QuizResultResponse submitQuiz(@PathVariable String quizId, @RequestBody UserQuizSubmission userQuizSubmission) {
        return quizService.submitQuiz(quizId, userQuizSubmission);
    }
}
