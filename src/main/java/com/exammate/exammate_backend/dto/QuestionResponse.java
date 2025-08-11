package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class QuestionResponse {
    private UUID id;
    private String text;
    private List<String> options;
}
