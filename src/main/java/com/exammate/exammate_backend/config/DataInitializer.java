package com.exammate.exammate_backend.config;

import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.models.Question;
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


    @Override
    public void run(String... args) {

                if (quizRepository.count() == 0) {

                        Quiz general = new Quiz();
                        general.setName("General Knowledge");
                        general.setTimeLimit(Duration.ofMinutes(1));
                        general.setQuestionLimit(5);

                        Quiz math = new Quiz();
                        math.setName("Math");
                        math.setTimeLimit(Duration.ofMinutes(15));
                        math.setQuestionLimit(7);

                        Quiz science = new Quiz();
                        science.setName("Science");
                        science.setTimeLimit(Duration.ofMinutes(12));
                        science.setQuestionLimit(6);

            // General Knowledge Questions
            List<Question> generalQuestions = List.of(
                Question.builder()
                        .text("What is the capital of France?")
                        .options(List.of("Berlin", "Madrid", "Paris", "Rome"))
                        .correctAnswer("Paris")
                        .categories(List.of(general))
                        .build(),
                Question.builder()
                        .text("Who wrote 'Romeo and Juliet'?")
                        .options(List.of("William Shakespeare", "Charles Dickens", "Leo Tolstoy", "Mark Twain"))
                        .correctAnswer("William Shakespeare")
                        .categories(List.of(general))
                        .build(),
                Question.builder()
                        .text("What is the largest ocean on Earth?")
                        .options(List.of("Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Arctic Ocean"))
                        .correctAnswer("Pacific Ocean")
                        .categories(List.of(general))
                        .build()
            );

            // Math Questions
            List<Question> mathQuestions = List.of(
                Question.builder()
                        .text("What is 1 + 1?")
                        .options(List.of("2", "1", "-1", "4"))
                        .correctAnswer("2")
                        .categories(List.of(math))
                        .build(),
                Question.builder()
                        .text("What is 5 - 3?")
                        .options(List.of("2", "3", "1", "4"))
                        .correctAnswer("2")
                        .categories(List.of(math))
                        .build(),
                Question.builder()
                        .text("What is 3 * 4?")
                        .options(List.of("12", "7", "9", "15"))
                        .correctAnswer("12")
                        .categories(List.of(math))
                        .build()
            );

            // Science Questions
            List<Question> scienceQuestions = List.of(
                Question.builder()
                        .text("Which planet is known as the Red Planet?")
                        .options(List.of("Earth", "Mars", "Jupiter", "Saturn"))
                        .correctAnswer("Mars")
                        .categories(List.of(science))
                        .build(),
                Question.builder()
                        .text("Which gas do plants primarily absorb for photosynthesis?")
                        .options(List.of("Oxygen", "Carbon Dioxide", "Nitrogen", "Helium"))
                        .correctAnswer("Carbon Dioxide")
                        .categories(List.of(science))
                        .build()
            );

                        general.setQuestions(generalQuestions);
                        math.setQuestions(mathQuestions);
                        science.setQuestions(scienceQuestions);

                        quizRepository.save(general);
                        quizRepository.save(math);
                        quizRepository.save(science);
        }
    }

}
