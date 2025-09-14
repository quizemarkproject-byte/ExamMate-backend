package com.exammate.exammate_backend.config;

import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import com.exammate.exammate_backend.repositories.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;


    @Override
    public void run(String... args) {
        if (quizRepository.count() == 0) {
            List<Question> questions = List.of(
                    Question.builder()
                            .text("What is the capital of France?")
                            .options(List.of("Berlin", "Madrid", "Paris", "Rome"))
                            .correctAnswer("Paris")
                            .build(),
                    Question.builder()
                            .text("Which planet is known as the Red Planet?")
                            .options(List.of("Earth", "Mars", "Jupiter", "Saturn"))
                            .correctAnswer("Mars")
                            .build(),
                    Question.builder()
                            .text("Who wrote 'Romeo and Juliet'?")
                            .options(List.of("William Shakespeare", "Charles Dickens", "Leo Tolstoy", "Mark Twain"))
                            .correctAnswer("William Shakespeare")
                            .build(),
                    Question.builder()
                            .text("Which gas do plants primarily absorb for photosynthesis?")
                            .options(List.of("Oxygen", "Carbon Dioxide", "Nitrogen", "Helium"))
                            .correctAnswer("Carbon Dioxide")
                            .build(),
                    Question.builder()
                            .text("What is the largest ocean on Earth?")
                            .options(List.of("Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Arctic Ocean"))
                            .correctAnswer("Pacific Ocean")
                            .build()
            );

            Quiz quiz1 = Quiz.builder()
                    .title("General Knowledge Quiz")
                    .timeLimit(Duration.ofMinutes(3))
                    .questionLimit(5)
                    .questions(questions)
                    .build();

            setQuizToQuestions(quiz1);

            List<Question> questions2 = List.of(
                    Question.builder()
                            .text("What is 1 + 1?")
                            .options(List.of("2", "1", "-1", "4"))
                            .correctAnswer("2")
                            .build(),
                    Question.builder()
                            .text("What is 5 - 3?")
                            .options(List.of("2", "3", "1", "4"))
                            .correctAnswer("2")
                            .build(),
                    Question.builder()
                            .text("What is 3 * 4?")
                            .options(List.of("12", "7", "9", "15"))
                            .correctAnswer("12")
                            .build(),
                    Question.builder()
                            .text("What is 10 / 2?")
                            .options(List.of("5", "2", "10", "20"))
                            .correctAnswer("5")
                            .build(),
                    Question.builder()
                            .text("What is the square of 3?")
                            .options(List.of("9", "6", "3", "12"))
                            .correctAnswer("9")
                            .build()
            );

            Quiz quiz2 = Quiz.builder()
                    .title("Advanced Calculus")
                    .timeLimit(Duration.ofHours(5))
                    .questionLimit(5)
                    .questions(questions2)
                    .build();

            setQuizToQuestions(quiz2);
            quizRepository.saveAll(List.of(quiz1, quiz2));
        }
    }

    private void setQuizToQuestions(Quiz quiz) {
        for (Question q : quiz.getQuestions()) {
            q.setQuiz(quiz);
        }
    }

}
