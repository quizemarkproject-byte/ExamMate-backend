package com.exammate.exammate_backend.models;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Setter
@Getter
public class Quiz {
    private String id;
    private String title;
    private Duration timeLimit;
    private List<Question> questions;

    public Quiz(String id, String title, Duration timeLimit, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.timeLimit = timeLimit;
        this.questions = questions;
    }
}
