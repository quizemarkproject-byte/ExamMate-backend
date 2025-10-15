package com.exammate.exammate_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailOtpRequest {
    @Email
    @NotBlank
    private String email;
}

