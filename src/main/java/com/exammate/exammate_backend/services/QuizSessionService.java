package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.CountResponse;
import com.exammate.exammate_backend.dto.QuizResultResponse;
import com.exammate.exammate_backend.dto.QuizSessionStartRequest;
import com.exammate.exammate_backend.dto.QuizSessionStartResponse;
import com.exammate.exammate_backend.dto.QuizSessionSubmissionRequest;

import java.util.List;
import java.util.UUID;

public interface QuizSessionService {
    QuizSessionStartResponse startSession(QuizSessionStartRequest request);
    QuizResultResponse submitSession(QuizSessionSubmissionRequest request);
    List<QuizResultResponse> getAllResultsForUser(UUID userId);
    QuizResultResponse getResultById(UUID resultId, UUID userId);
    CountResponse countResultsForUser(UUID userId);
}
