package com.exammate.exammate_backend.config;

import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.repositories.QuizRepository;
import com.exammate.exammate_backend.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final QuizRepository quizRepository;
        private final QuestionRepository questionRepository;

        @Override
        public void run(String... args) {

                if (quizRepository.count() == 0) {
                        Quiz general = new Quiz();
                        general.setName("General Knowledge");
                        general.setTimeLimit(Duration.ofMinutes(1));
                        general.setQuestionLimit(3);

                        Quiz math = new Quiz();
                        math.setName("Math");
                        math.setTimeLimit(Duration.ofMinutes(15));
                        math.setQuestionLimit(3);

                        Quiz science = new Quiz();
                        science.setName("Science");
                        science.setTimeLimit(Duration.ofMinutes(12));
                        science.setQuestionLimit(2);

                        Quiz history = new Quiz();
                        history.setName("History");
                        history.setTimeLimit(Duration.ofMinutes(10));
                        history.setQuestionLimit(2);

                        Quiz geography = new Quiz();
                        geography.setName("Geography");
                        geography.setTimeLimit(Duration.ofMinutes(10));
                        geography.setQuestionLimit(2);

                        Quiz technology = new Quiz();
                        technology.setName("Technology");
                        technology.setTimeLimit(Duration.ofMinutes(8));
                        technology.setQuestionLimit(2);

                        List<Question> generalQuestions = List.of(
                                        Question.builder()
                                                        .text("What is the capital of France?")
                                                        .options(List.of("Berlin", "Madrid", "Paris", "Rome"))
                                                        .correctAnswer("Paris")
                                                        .categories(List.of(general))
                                                        .build(),
                                        Question.builder()
                                                        .text("Who wrote 'Romeo and Juliet'?")
                                                        .options(List.of("William Shakespeare", "Charles Dickens",
                                                                        "Leo Tolstoy", "Mark Twain"))
                                                        .correctAnswer("William Shakespeare")
                                                        .categories(List.of(general))
                                                        .build(),
                                        Question.builder()
                                                        .text("What is the largest ocean on Earth?")
                                                        .options(List.of("Atlantic Ocean", "Indian Ocean",
                                                                        "Pacific Ocean", "Arctic Ocean"))
                                                        .correctAnswer("Pacific Ocean")
                                                        .categories(List.of(general))
                                                        .build());

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
                                                        .build());

                        List<Question> scienceQuestions = List.of(
                                        Question.builder()
                                                        .text("Which planet is known as the Red Planet?")
                                                        .options(List.of("Earth", "Mars", "Jupiter", "Saturn"))
                                                        .correctAnswer("Mars")
                                                        .categories(List.of(science))
                                                        .build(),
                                        Question.builder()
                                                        .text("Which gas do plants primarily absorb for photosynthesis?")
                                                        .options(List.of("Oxygen", "Carbon Dioxide", "Nitrogen",
                                                                        "Helium"))
                                                        .correctAnswer("Carbon Dioxide")
                                                        .categories(List.of(science))
                                                        .build());

                        List<Question> historyQuestions = List.of(
                                        Question.builder()
                                                        .text("Who was the first President of the United States?")
                                                        .options(List.of("George Washington", "Thomas Jefferson",
                                                                        "Abraham Lincoln", "John Adams"))
                                                        .correctAnswer("George Washington")
                                                        .categories(List.of(history))
                                                        .build(),
                                        Question.builder()
                                                        .text("In which year did World War II end?")
                                                        .options(List.of("1945", "1939", "1918", "1965"))
                                                        .correctAnswer("1945")
                                                        .categories(List.of(history))
                                                        .build());

                        List<Question> geographyQuestions = List.of(
                                        Question.builder()
                                                        .text("Which is the largest continent by area?")
                                                        .options(List.of("Africa", "Asia", "Europe", "Antarctica"))
                                                        .correctAnswer("Asia")
                                                        .categories(List.of(geography))
                                                        .build(),
                                        Question.builder()
                                                        .text("Which river is the longest in the world?")
                                                        .options(List.of("Nile", "Amazon", "Yangtze", "Mississippi"))
                                                        .correctAnswer("Nile")
                                                        .categories(List.of(geography))
                                                        .build());

                        List<Question> technologyQuestions = List.of(
                                        Question.builder()
                                                        .text("What does CPU stand for?")
                                                        .options(List.of("Central Processing Unit",
                                                                        "Computer Personal Unit",
                                                                        "Central Performance Unit",
                                                                        "Control Processing Unit"))
                                                        .correctAnswer("Central Processing Unit")
                                                        .categories(List.of(technology))
                                                        .build(),
                                        Question.builder()
                                                        .text("Which company developed the Java programming language?")
                                                        .options(List.of("Microsoft", "Sun Microsystems", "Apple",
                                                                        "IBM"))
                                                        .correctAnswer("Sun Microsystems")
                                                        .categories(List.of(technology))
                                                        .build());

                        // Collect all questions and persist via QuestionRepository (Question is the owning side)
                        List<Question> all = new ArrayList<>();
                        all.addAll(generalQuestions);
                        all.addAll(mathQuestions);
                        all.addAll(scienceQuestions);
                        all.addAll(historyQuestions);
                        all.addAll(geographyQuestions);
                        all.addAll(technologyQuestions);

                        questionRepository.saveAll(all);
                }
        }

}
