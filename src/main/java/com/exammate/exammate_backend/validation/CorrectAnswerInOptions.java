package com.exammate.exammate_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = CorrectAnswerInOptionsValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface CorrectAnswerInOptions {
    String message() default "correctAnswer must be one of the options";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

