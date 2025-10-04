package com.exammate.exammate_backend.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
    LocalDateTime timestamp,
    String error,
    int status
) {}
