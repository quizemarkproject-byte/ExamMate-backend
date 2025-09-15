package com.exammate.exammate_backend.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "quizsession_questions",
            joinColumns = @JoinColumn(name = "quizsession_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions;
    @OneToOne(mappedBy = "quizSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private QuizResult result;
    private String userId;
    @Builder.Default
    private Instant startTime = Instant.now();
    private long totalTimeSeconds;
    private boolean expired;

}
