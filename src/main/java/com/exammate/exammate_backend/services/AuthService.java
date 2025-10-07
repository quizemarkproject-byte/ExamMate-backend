package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.AuthResponse;
import com.exammate.exammate_backend.dto.AuthRequest;
import com.exammate.exammate_backend.dto.SignupRequest;

import jakarta.servlet.http.HttpServletRequest;

import com.exammate.exammate_backend.dto.ResetPasswordRequest;

public interface AuthService {
    void signup(SignupRequest req, HttpServletRequest request);

    AuthResponse login(AuthRequest req);

    void createPasswordResetToken(String email);

    void resetPassword(ResetPasswordRequest req);

    String verifyEmail(String token);
}
