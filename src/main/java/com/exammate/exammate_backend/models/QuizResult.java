package com.exammate.exammate_backend.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID quizId;
    private String userId;
    @ElementCollection
    @CollectionTable(
            name = "submitted_answers",
            joinColumns = @JoinColumn(name = "quiz_result_id")
    )
    @MapKeyColumn(name = "question_id")
    @Column(name = "answer")
    private Map<UUID, String> submittedAnswers;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;

}
