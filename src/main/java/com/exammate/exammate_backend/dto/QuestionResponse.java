package com.exammate.exammate_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionResponse {
    private String text;
    private List<String> options;
}
