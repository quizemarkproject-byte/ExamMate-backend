package com.exammate.exammate_backend.controllers;

import com.exammate.exammate_backend.dto.*;
import com.exammate.exammate_backend.services.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Signup, login, and password reset endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Sign up", description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid request or email already in use", content = @Content)
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody SignupRequest req, HttpServletRequest request) {
        authService.signup(req, request);
    }

    @Operation(summary = "Verify email", description = "Verify user email with token from email link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content)
    })
    @GetMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public String verifyEmail(@RequestParam(required = true) String token) {
        return authService.verifyEmail(token);
    }

    @Operation(summary = "Login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody AuthRequest req) {
        return authService.login(req);
    }

    @Operation(summary = "Forgot password", description = "Create a password reset token and (optionally) email it to the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "If email exists, a reset token will be created", content = @Content)
    })
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.createPasswordResetToken(req.getEmail());
    }

    @Operation(summary = "Reset password", description = "Reset a user's password using a valid reset token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content)
    })
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
    }
}
