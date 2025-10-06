package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.AuthResponse;
import com.exammate.exammate_backend.dto.AuthRequest;
import com.exammate.exammate_backend.dto.SignupRequest;
import com.exammate.exammate_backend.dto.ResetPasswordRequest;

public interface AuthService {
    void signup(SignupRequest req);
    AuthResponse login(AuthRequest req);
    void createPasswordResetToken(String email);
    void resetPassword(ResetPasswordRequest req);
    void verifyEmail(String token);
}
