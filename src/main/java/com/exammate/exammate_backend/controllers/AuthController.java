package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.AuthResponse;
import com.exammate.exammate_backend.dto.EmailOtpRequest;
import com.exammate.exammate_backend.dto.VerifyOtpRequest;
import com.exammate.exammate_backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Email OTP authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Request OTP", description = "Submit email to receive an OTP")
    @PostMapping("/request-otp")
    @ResponseStatus(HttpStatus.OK)
    public void requestOtp(@Valid @RequestBody EmailOtpRequest req) {
        authService.requestOtp(req.getEmail());
    }

    @Operation(summary = "Verify OTP", description = "Verify OTP and obtain access token")
    @PostMapping("/verify-otp")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        return authService.verifyOtp(req.getEmail(), req.getOtp());
    }
}
