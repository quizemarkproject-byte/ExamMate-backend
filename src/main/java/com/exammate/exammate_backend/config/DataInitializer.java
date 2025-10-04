package com.exammate.exammate_backend.config;

import com.exammate.exammate_backend.models.Category;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.repositories.CategoryRepository;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final CategoryRepository categoryRepository;
        private final QuestionRepository questionRepository;


    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            Category general = new Category();
            general.setName("General Knowledge");

            Category math = new Category();
            math.setName("Math");

            Category science = new Category();
            science.setName("Science");

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

            categoryRepository.save(general);
            categoryRepository.save(math);
            categoryRepository.save(science);
        }
    }

}
