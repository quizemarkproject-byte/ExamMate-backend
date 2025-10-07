package com.exammate.exammate_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;
    @NotBlank(message = "Confirm password is required")
    @Size(min = 8, max = 20, message = "Confirm password must be between 8 and 20 characters long")
    private String confirmPassword;
}
