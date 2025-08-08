package com.exammate.exammate_backend.repositories.impl;

import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.repositories.QuizRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StubQuizRepository implements QuizRepository {
    private final Map<String, Quiz> quizzes = new HashMap<>();

    public StubQuizRepository(ModelMapper modelMapper) {

        List<Question> questions = List.of(
                new Question("q1", "What is the capital of France?",
                        List.of("Berlin", "Madrid", "Paris", "Rome"), "Paris"),

                new Question("q2", "Which planet is known as the Red Planet?",
                        List.of("Earth", "Mars", "Jupiter", "Saturn"), "Mars"),

                new Question("q3", "Who wrote 'Romeo and Juliet'?",
                        List.of("William Shakespeare", "Charles Dickens", "Leo Tolstoy", "Mark Twain"), "William Shakespeare"),

                new Question("q4", "Which gas do plants primarily absorb for photosynthesis?",
                        List.of("Oxygen", "Carbon Dioxide", "Nitrogen", "Helium"), "Carbon Dioxide"),

                new Question("q5", "What is the largest ocean on Earth?",
                        List.of("Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Arctic Ocean"), "Pacific Ocean"),

                new Question("q6", "Which country hosted the 2016 Summer Olympics?",
                        List.of("China", "Brazil", "UK", "Russia"), "Brazil"),

                new Question("q7", "What is the chemical symbol for Gold?",
                        List.of("Ag", "Au", "Gd", "Go"), "Au"),

                new Question("q8", "How many continents are there on Earth?",
                        List.of("5", "6", "7", "8"), "7"),

                new Question("q9", "Which animal is known as the 'Ship of the Desert'?",
                        List.of("Horse", "Camel", "Donkey", "Elephant"), "Camel"),

                new Question("q10", "What is the hardest natural substance on Earth?",
                        List.of("Gold", "Iron", "Diamond", "Platinum"), "Diamond")
        );

        Quiz quiz = new Quiz("quiz1", "General Knowledge Quiz", Duration.ofMinutes(3), questions);
        quizzes.put("quiz1", quiz);
    }


    public Quiz findQuizById(String quizId) {
        Quiz quiz = quizzes.get(quizId);
        if (quiz == null) {
            return null;
        }
        return quiz;
    }

    public List<Quiz> findAllQuizzes() {
        return quizzes.values().stream()
                .toList();
    }
}
