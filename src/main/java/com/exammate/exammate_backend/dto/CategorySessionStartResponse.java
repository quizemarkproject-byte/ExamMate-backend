package com.exammate.exammate_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CategorySessionStartResponse {
    private UUID sessionId;
    private List<QuestionResponse> questions;
}
