package com.exammate.exammate_backend.services;

import com.exammate.exammate_backend.dto.AuthResponse;

public interface AuthService {
    void requestOtp(String email);
    AuthResponse verifyOtp(String email, String otp);
}
