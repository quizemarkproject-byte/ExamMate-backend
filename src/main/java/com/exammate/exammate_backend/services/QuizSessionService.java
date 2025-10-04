package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.QuizSessionStartRequest;
import com.exammate.exammate_backend.dto.QuizSessionSubmissionRequest;

import java.util.List;
import java.util.UUID;

public interface QuizSessionService {
    com.exammate.exammate_backend.dto.QuizSessionStartResponse startSession(QuizSessionStartRequest request);
    QuizResultResponse submitSession(QuizSessionSubmissionRequest request);
    List<QuizResultResponse> getAllResultsForUser(String userId);
    QuizResultResponse getResultById(UUID resultId);
}
