package com.exammate.exammate_backend.validation;

import com.exammate.exammate_backend.dto.QuestionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class CorrectAnswerInOptionsValidator implements ConstraintValidator<CorrectAnswerInOptions, QuestionRequest> {

    @Override
    public boolean isValid(QuestionRequest value, ConstraintValidatorContext context) {
        if (value == null) return true; // other annotations handle null
        List<String> opts = value.getOptions();
        String correct = value.getCorrectAnswer();
        if (opts == null || correct == null) return false;
        return opts.contains(correct);
    }
}

