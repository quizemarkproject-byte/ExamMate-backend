package com.exammate.exammate_backend.services.impl;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.exammate.exammate_backend.dto.AuthRequest;
import com.exammate.exammate_backend.dto.AuthResponse;
import com.exammate.exammate_backend.dto.ResetPasswordRequest;
import com.exammate.exammate_backend.dto.SignupRequest;
import com.exammate.exammate_backend.exception.BadRequestException;
import com.exammate.exammate_backend.exception.InvalidCredentialsException;
import com.exammate.exammate_backend.models.User;
import com.exammate.exammate_backend.models.VerificationToken;
import com.exammate.exammate_backend.models.VerificationToken.TokenType;
import com.exammate.exammate_backend.repositories.UserRepository;
import com.exammate.exammate_backend.repositories.VerificationTokenRepository;
import com.exammate.exammate_backend.security.JwtUtil;
import com.exammate.exammate_backend.services.AuthService;
import com.exammate.exammate_backend.services.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    
    @Value("${app.allowed-origin.hosted}")
    private String hostedFrontendUrl;

    @Override
    public void signup(SignupRequest req, HttpServletRequest request) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
        throw new BadRequestException("Username already taken");
    }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
    User u = User.builder()
        .email(req.getEmail())
        .password(passwordEncoder.encode(req.getPassword()))
        .fullName(req.getFullName())
        .username(req.getUsername())
        .build();
        userRepository.save(u);
        VerificationToken evt = VerificationToken.create(u, TokenType.EMAIL_VERIFICATION, 24);
        verificationTokenRepository.save(evt);
    String subject = "Verify your ExamMate account";
    String verificationLink = hostedFrontendUrl + "/verify-email?token=" + evt.getToken();
    String body = "Hello,\n\nPlease verify your email by clicking the link below:\n" + verificationLink + "\n\nIf you did not sign up, please ignore this email.";
    emailService.sendEmail(u.getEmail(), subject, body);
    }

    public void verifyEmail(String token) {
        VerificationToken evt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (evt.getExpiryDate().before(new Date()) || evt.getType() != TokenType.EMAIL_VERIFICATION) {
            throw new BadRequestException("Verification token expired or invalid type");
        }

        User user = evt.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.deleteByUserAndType(user, TokenType.EMAIL_VERIFICATION);
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
            String token = jwtUtil.generateToken(req.getUsername(), user.getId(), user.getRole());
            return new AuthResponse(token);
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        } catch (DisabledException ex) {
        throw new BadRequestException("Account not verified. Please check your email.");
    }
    }

    @Override
    public void createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(user -> {
            verificationTokenRepository.deleteByUserAndType(user, TokenType.PASSWORD_RESET);
            VerificationToken prt = VerificationToken.create(user, TokenType.PASSWORD_RESET, 2);
            verificationTokenRepository.save(prt);

            String subject = "ExamMate Password Reset";
            String body = String.format("Hello,\n\nYou requested a password reset. Use the following token to reset your password: %s\n\nThis token will expire in 2 hours.", prt.getToken());
            emailService.sendEmail(user.getEmail(), subject, body);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest req) {
         if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        VerificationToken prt = verificationTokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid token"));
        if (prt.getExpiryDate().before(new Date()) || prt.getType() != TokenType.PASSWORD_RESET) throw new BadRequestException("Token expired or invalid type");
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        verificationTokenRepository.deleteByUserAndType(user, TokenType.PASSWORD_RESET);
    }
}
