package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.*;
import com.exammate.exammate_backend.models.Question;
import com.exammate.exammate_backend.models.Quiz;
import com.exammate.exammate_backend.models.QuizResult;
import com.exammate.exammate_backend.repositories.QuizRepository;
import com.exammate.exammate_backend.services.QuizService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final ModelMapper modelMapper;
    private final QuizRepository quizRepository;

    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAllQuizzes().stream()
                .map(quiz -> modelMapper.map(quiz, QuizResponse.class))
                .toList();
    }

    public QuizResponse getQuizById(String quizId) {
        return modelMapper.map(quizRepository.findQuizById(quizId), QuizResponse.class);
    }

    public QuizResultResponse submitQuiz(String quizId, UserQuizSubmission userQuizSubmission) {
        Quiz quiz = quizRepository.findQuizById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz not found: " + quizId);
        }

        int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = 0;

        Map<String, String> submittedAnswers = userQuizSubmission.getAnswerSubmissions().stream()
                .collect(Collectors.toMap(
                        QuestionAnswerSubmission::getQuestionId,
                        QuestionAnswerSubmission::getSelectedAnswer
                ));

        List<QuestionResultResponse> questionResults = new ArrayList<>();
        for (Question question : quiz.getQuestions()) {
            String chosenAnswer = submittedAnswers.get(question.getId());
            boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(chosenAnswer);
            if (isCorrect) {
                correctAnswers++;
            }

            questionResults.add(new QuestionResultResponse(
                    question.getId(),
                    question.getOptions(),
                    question.getCorrectAnswer(),
                    question.getCorrectAnswer().equalsIgnoreCase(chosenAnswer)
            ));
        }

        QuizResult quizResult = new QuizResult(quizId, totalQuestions, correctAnswers);
        quizResult.setUserId(userQuizSubmission.getUserId());
        quizResult.setId(UUID.randomUUID().toString());

        return new QuizResultResponse(
                questionResults,
                totalQuestions,
                correctAnswers,
                quizResult.getScorePercentage()
        );
    }

}
