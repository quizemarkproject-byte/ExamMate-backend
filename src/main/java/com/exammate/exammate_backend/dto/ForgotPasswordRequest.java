package com.exammate.exammate_backend.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @Email(message = "Invalid email format")
    private String email;
}
