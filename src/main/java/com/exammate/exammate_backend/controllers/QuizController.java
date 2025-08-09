package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.QuizResponse;
import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.StartQuizResponse;
import com.exammate.exammate_backend.dto.UserQuizSubmissionRequest;
import com.exammate.exammate_backend.dto.UserTimeRemainingResponse;
import com.exammate.exammate_backend.services.QuizService;
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
    public QuizResponse findQuizById(@PathVariable UUID quizId) {
        return quizService.getQuizById(quizId);
    }

    @PostMapping("/{quizId}/{userId}/start")
    @ResponseStatus(HttpStatus.OK)
    public StartQuizResponse startQuiz(@PathVariable UUID quizId, @PathVariable String userId) {
        return quizService.startQuiz(quizId, userId);
    }

    @GetMapping("/{quizId}/{userId}/{quizSessionId}/time")
    @ResponseStatus(HttpStatus.OK)
    public UserTimeRemainingResponse getRemainingTime(@PathVariable UUID quizId, @PathVariable String userId, @PathVariable UUID quizSessionId) {
        return quizService.getRemainingTime(quizId, userId, quizSessionId);
    }

    @PostMapping("/submit/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    public QuizResultResponse submitQuiz(@PathVariable UUID quizId, @RequestBody UserQuizSubmissionRequest userQuizSubmissionRequest) {
        return quizService.submitQuiz(quizId, userQuizSubmissionRequest);
    }
}
