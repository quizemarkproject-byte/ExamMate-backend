package com.exammate.exammate_backend.services.impl;

import com.exammate.exammate_backend.dto.*;
import com.exammate.exammate_backend.models.PasswordResetToken;
import com.exammate.exammate_backend.models.Role;
import com.exammate.exammate_backend.models.User;
import com.exammate.exammate_backend.repositories.PasswordResetTokenRepository;
import com.exammate.exammate_backend.repositories.UserRepository;
import com.exammate.exammate_backend.security.JwtUtil;
import com.exammate.exammate_backend.services.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import com.exammate.exammate_backend.exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public void signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User u = new User(req.getEmail(), passwordEncoder.encode(req.getPassword()), Role.USER);
        userRepository.save(u);
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
            User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
            String token = jwtUtil.generateToken(req.getEmail(), user.getId(), user.getRole());
            return new AuthResponse(token);
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public void createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(user -> {
            // remove previous tokens for user
            tokenRepository.deleteAllByUser(user);
            String token = UUID.randomUUID().toString();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 2); // 2 hours expiry
            PasswordResetToken prt = new PasswordResetToken(token, cal.getTime(), user);
            tokenRepository.save(prt);
            // TODO: send token by email. For now token is persisted and can be returned by a dev endpoint if needed.
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetToken prt = tokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (prt.getExpiryDate().before(new Date())) throw new IllegalArgumentException("Token expired");
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        tokenRepository.deleteByToken(req.getToken());
    }
}
