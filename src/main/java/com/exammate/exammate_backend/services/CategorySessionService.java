package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.CategoryResultResponse;
import com.exammate.exammate_backend.dto.CategorySessionStartRequest;
import com.exammate.exammate_backend.dto.CategorySessionSubmissionRequest;

import java.util.List;
import java.util.UUID;

public interface CategorySessionService {
    com.exammate.exammate_backend.dto.CategorySessionStartResponse startSession(CategorySessionStartRequest request);
    CategoryResultResponse submitSession(CategorySessionSubmissionRequest request);
    List<CategoryResultResponse> getAllResultsForUser(String userId);
    CategoryResultResponse getResultById(UUID resultId);
}
